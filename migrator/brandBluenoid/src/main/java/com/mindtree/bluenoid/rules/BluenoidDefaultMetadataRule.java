package com.mindtree.bluenoid.rules;

import com.mindtree.models.vo.DriveBasedMetadataRuleVO;
import com.mindtree.transformer.service.AppContext;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.service.AbstractDriveBasedRule;

public class BluenoidDefaultMetadataRule extends AbstractDriveBasedRule {

	public BluenoidDefaultMetadataRule(DriveBasedMetadataRuleVO ruleVO) {
		super(ruleVO);
	}

	@Override
	public void apply() {
		/**
		 * Apply Default/common metadata values.
		 */
		
		String assetPath = ruleVO.s3Asset.getKey().trim();
		String fileName = AppContext.getStorage().getFileName(assetPath);
		ruleVO.assetMetadataMap.put(MigratorConstants.EXCEL_COLUMN_DRIVE_PATH, assetPath);
		if (fileName.startsWith(MigratorConstants.SPECIAL_CHARACTER_SINGLE_HASH)) {
			fileName = fileName.replaceFirst("#", "");
		}
		ruleVO.assetMetadataMap.put(MigratorConstants.DC_TITLE, fileName);
		ruleVO.assetMetadataMap.put(MigratorConstants.COLUMN_FILE_NAME, fileName);
		ruleVO.assetMetadataMap.put(MigratorConstants.BRAND, MigratorConstants.BRAND_BLUENOID);

		if (!ruleVO.migratedAssetsMap.containsKey(assetPath)) {
			ruleVO.missingAssets.add(assetPath);
		}
		
		LOGGER.info("ruleVO.assetMetadataMap size : "+ruleVO.assetMetadataMap.size());
	}

}
