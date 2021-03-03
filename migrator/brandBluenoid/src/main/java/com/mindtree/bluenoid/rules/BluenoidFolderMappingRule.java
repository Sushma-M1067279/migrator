package com.mindtree.bluenoid.rules;

import com.mindtree.bluenoid.util.MetadataUtil;
import com.mindtree.models.vo.DriveBasedMetadataRuleVO;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.service.AbstractDriveBasedRule;

public class BluenoidFolderMappingRule extends AbstractDriveBasedRule {

	public BluenoidFolderMappingRule(DriveBasedMetadataRuleVO ruleVO) {
		super(ruleVO);
	}

	@Override
	public void apply() {
		/**
		 * Apply Folder Mapping Rule (Taxonomy) : Refer Columns(In Brand
		 * Configuartion sheet) : Folder Name -> Mapped AEM Assets Folder
		 * Path Find out absTargetPath.
		 */
		MetadataUtil.applyFolderMappingRule(ruleVO.destinationAssetPath, ruleVO.assetMetadataMap,
				MigratorConstants.BRAND_BLUENOID);
	}

}
