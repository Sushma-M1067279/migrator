package com.mindtree.bluenoid.filters;

import java.util.Properties;

import com.mindtree.bluenoid.util.FilterUtil;
import com.mindtree.models.vo.ExtensionFilterVO;
import com.mindtree.core.service.AppContext;
import com.mindtree.core.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.service.AbstractFilter;

public class BluenoidEmptyAssetFilter extends AbstractFilter {

	public BluenoidEmptyAssetFilter() {
	}
	
	public BluenoidEmptyAssetFilter(ExtensionFilterVO filterVO) {
		super(filterVO);
	}

	@Override
	public boolean apply() {
		/**
		 * Applying Size Filter Rule : Ignore files if size is 0 bytes.
		 */
		boolean eligibleToMigrate = true;
		Properties prop = null;
		prop = AppContext.getAppConfig();
		String emptyAssetFilterFlag = prop.getProperty(filterVO.brandPrefix + ""
				+ MigratorConstants.FILTER_EMPTY_ASSET_ZERO_BYTE);
		String assetPath = filterVO.s3Asset.getKey().trim();
		Long assetSize = filterVO.s3Asset.getValue();
		LOGGER.info("File Size : " + assetSize);
		if (emptyAssetFilterFlag != null && emptyAssetFilterFlag.equalsIgnoreCase(MigratorConstants.ON)) {
			eligibleToMigrate = FilterUtil.filterEmptyAssets(assetPath, assetSize, filterVO.nonMigratedAssetsMap);
		}
		return eligibleToMigrate;
	}

}
