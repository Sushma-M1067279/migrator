package com.mindtree.holoxo.rules.metadata;

import com.mindtree.holoxo.util.HoloxoMetadataUtil;
import com.mindtree.models.vo.XLSMetadataRuleVO;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.service.AbstractXLSMetadataRule;

/**
 * This rule extracts fields which will be used to construct new folder path for
 * the asset.
 * 
 * @author M1032046
 *
 */
public class HoloxoMetadataFolderMappingRule extends AbstractXLSMetadataRule {
	

	public HoloxoMetadataFolderMappingRule(XLSMetadataRuleVO ruleVO) {
		super(ruleVO);
	}

	@Override
	public void apply() {
		
//		LOGGER.info("HoloxoMetadataFolderMappingRule brandMetadataHeader: "+ruleVO.brandMetadataHeader);
//		LOGGER.info("HoloxoMetadataFolderMappingRule brandMetadataValue: "+ruleVO.brandMetadataValue);


		if (ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.PATH_COLUMN)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.ASSET_TYPE)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.REGION)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.LANGUAGE)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.COUNTRY)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.CALENDAR_SEASON_YEAR)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.USAGE)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.PPSN)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.COLUMN_FILE_NAME)) {
			HoloxoMetadataUtil.folderMappingMap.put(ruleVO.brandMetadataHeader, ruleVO.brandMetadataValue);
		}

	}

}
