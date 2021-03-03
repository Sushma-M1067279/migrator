package com.mindtree.holoxo.rules.folder;

import com.mindtree.holoxo.util.HoloxoMetadataUtil;
import com.mindtree.models.vo.FolderRuleVO;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.exception.MigratorServiceException;
import com.mindtree.utils.service.AbstractFolderRule;

public class HoloxoFolderRule extends AbstractFolderRule {

	public HoloxoFolderRule(FolderRuleVO ruleVO) {
		super(ruleVO);
	}

	@Override
	public void apply() {
		String aemTargetPath = null;
		try {
			aemTargetPath = HoloxoMetadataUtil.folderMappingMethod(ruleVO.assetMetadataMap);
		} catch (MigratorServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (aemTargetPath != null) {
			ruleVO.assetMetadataMap.put(MigratorConstants.ABS_TARGET_PATH, aemTargetPath);
			ruleVO.assetMetadataMap.put(MigratorConstants.MASTER_NAME_SPACE, MigratorConstants.BRAND_HOLOXO);
		}
	}

}
