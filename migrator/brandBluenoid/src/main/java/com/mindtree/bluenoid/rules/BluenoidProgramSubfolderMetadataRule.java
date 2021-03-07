package com.mindtree.bluenoid.rules;

import java.util.Properties;

import com.mindtree.bluenoid.util.MetadataUtil;
import com.mindtree.models.vo.DriveBasedMetadataRuleVO;
import com.mindtree.transformer.service.AppContext;
import com.mindtree.transformer.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.service.AbstractDriveBasedRule;

public class BluenoidProgramSubfolderMetadataRule extends AbstractDriveBasedRule {

	public BluenoidProgramSubfolderMetadataRule(DriveBasedMetadataRuleVO ruleVO) {
		super(ruleVO);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void apply() {
		/**
		 * Apply metadata rule : Assets included in the online/2018 folders will
		 * have subfolders within it all nmed by program name. Use the folder
		 * name as the program name Metadata Value
		 */
		Properties prop = null;
		prop = AppContext.getAppConfig();
		String programSubFolderRulePath = prop.getProperty(MigratorConstants.BRAND_BLUENOID_PREFIX + ""
				+ MigratorConstants.PROGRAM_SUB_FOLDER_RULE_PATH);
		if (programSubFolderRulePath != null && !programSubFolderRulePath.isEmpty()) {
			MetadataUtil.applyProgramSubFolderMetadataRule(ruleVO.masterMetadataMap, ruleVO.destinationAssetPath,
					ruleVO.assetMetadataMap, programSubFolderRulePath);
		}

	}

}
