package com.mindtree.bluenoid.rules;

import com.mindtree.bluenoid.config.BluenoidReqConfigurationLoader;
import com.mindtree.bluenoid.util.MetadataUtil;
import com.mindtree.models.vo.DriveBasedMetadataRuleVO;
import com.mindtree.utils.service.AbstractDriveBasedRule;

public class BluenoidProgramFolderMetadataRule extends AbstractDriveBasedRule {

	public BluenoidProgramFolderMetadataRule(DriveBasedMetadataRuleVO ruleVO) {
		super(ruleVO);
	}

	@Override
	public void apply() {
		/**
		 * Apply metadata rule : Assets included in the online/2018 folders will
		 * have subfolders within it all nmed by program name. Use the folder
		 * name as the program name Metadata Value
		 */
		if (BluenoidReqConfigurationLoader.programFolderPathList != null
				&& BluenoidReqConfigurationLoader.programFolderPathList.size() > 0) {
			MetadataUtil.applyProgramFolderMetadataRule(ruleVO.masterMetadataMap, ruleVO.destinationAssetPath,
					ruleVO.assetMetadataMap);
		}

	}

}
