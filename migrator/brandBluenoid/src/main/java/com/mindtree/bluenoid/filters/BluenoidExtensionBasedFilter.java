package com.mindtree.bluenoid.filters;

import java.util.Map;
import java.util.Properties;

import com.mindtree.bluenoid.config.BluenoidReqConfigurationLoader;
import com.mindtree.bluenoid.util.FilterUtil;
import com.mindtree.models.vo.ExtensionFilterVO;
import com.mindtree.core.service.AppContext;
import com.mindtree.core.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.S3Utility;
import com.mindtree.utils.service.AbstractFilter;

public class BluenoidExtensionBasedFilter extends AbstractFilter {
	
	public BluenoidExtensionBasedFilter() {
	}

	public BluenoidExtensionBasedFilter(ExtensionFilterVO filterVO) {
		super(filterVO);
	}

	@Override
	public boolean apply() {

		/**
		 * Implementation of Extensions related filter rules.
		 */

		/**
		 * If asset(with blank extension) is eligible for Migration then apply
		 * Rule - If file extension is blank then consider custom extensions
		 * defined by OA in sheet 'Optimity File Extension'.
		 */
		boolean eligibleToMigrate = true;
		Properties prop = null;
		prop = AppContext.getAppConfig();
		String sourceAssetPath = filterVO.s3Asset.getKey().trim();
		Long assetSize = filterVO.s3Asset.getValue();
		String applyCustomExtensionsFlag = prop.getProperty(filterVO.brandPrefix + ""
				+ MigratorConstants.APPLY_CUSTOM_EXTENSIONS);
		if (applyCustomExtensionsFlag != null && applyCustomExtensionsFlag.equalsIgnoreCase(MigratorConstants.ON)) {
			if (BluenoidReqConfigurationLoader.customExtensionsMap.keySet().contains(sourceAssetPath)) {
				Map<String, String> customExtensionMap = BluenoidReqConfigurationLoader.customExtensionsMap
						.get(sourceAssetPath);
				if (customExtensionMap != null) {
					String mainFileExtension = customExtensionMap.get("Main FileType");
					String customExtension = customExtensionMap.get("File Type");
					if (customExtension != null) {
						if (customExtension.equalsIgnoreCase("delete")) {
							eligibleToMigrate = false;
							filterVO.nonMigratedAssetsMap.put(sourceAssetPath,
									MigratorConstants.ASSET_NOT_ELIGIBLE_FOR_MIGRATION + sourceAssetPath + ":Asset Size is:"
											+ S3Utility.format(assetSize, 2)
											+ ":Blank File Extension - Marked as Delete");
						} else {
							filterVO.destinationAssetPath = FilterUtil.applyCustomExtensions(sourceAssetPath,
									mainFileExtension, customExtension);
							eligibleToMigrate = true;
							filterVO.migratedAssetsMap.put(sourceAssetPath, MigratorConstants.ASSET_ELIGIBLE_FOR_MIGRATION
									+ sourceAssetPath + ":Asset Size is:" + S3Utility.format(assetSize, 2) + ":"
									+ customExtension + ":Custom File Extension");
						}
					}

				}

			}
		}

		return eligibleToMigrate;
	}

}
