package com.mindtree.bluenoid.filters;

import java.util.Properties;

import com.mindtree.bluenoid.util.FilterUtil;
import com.mindtree.models.vo.ExtensionFilterVO;
import com.mindtree.core.service.AppContext;
import com.mindtree.core.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.MigrationUtil;
import com.mindtree.utils.service.AbstractFilter;

public class BluenoidFileTypeFilter extends AbstractFilter {
	
	public BluenoidFileTypeFilter() {
	}

	public BluenoidFileTypeFilter(ExtensionFilterVO filterVO) {
		super(filterVO);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean apply() {
		boolean eligibleToMigrate = false;
		String assetPath = filterVO.s3Asset.getKey().trim();
		Properties prop = null;
		prop = AppContext.getAppConfig();
		Long assetSize = filterVO.s3Asset.getValue();
		String fileExtension = MigrationUtil.getFileExtension(assetPath);
		String extensionFilterFlag = prop.getProperty(filterVO.brandPrefix + ""
				+ MigratorConstants.FILTER_ASSET_BY_EXTENSIONS);
		String blankAssetFilterFlag = prop.getProperty(filterVO.brandPrefix + ""
				+ MigratorConstants.FILTER_BLANK_ASSET_LESS_THAN_1MB);
		fileExtension = MigrationUtil.getFileExtension(assetPath);
		if (fileExtension != null && !fileExtension.isEmpty()) {
			if (extensionFilterFlag != null && extensionFilterFlag.equalsIgnoreCase(MigratorConstants.ON)) {
				eligibleToMigrate = FilterUtil.filterAssetsByToBeMigratedFileTypes(assetPath, assetSize, fileExtension,
						filterVO.migratedAssetsMap, filterVO.nonMigratedAssetsMap);
			}
		} else {
			if (blankAssetFilterFlag != null && blankAssetFilterFlag.equalsIgnoreCase(MigratorConstants.ON)) {
				eligibleToMigrate = FilterUtil.filterBlankExtensionAssets(assetPath, assetSize,
						filterVO.migratedAssetsMap, filterVO.nonMigratedAssetsMap);
			}
		}
		return eligibleToMigrate;
	}

}
