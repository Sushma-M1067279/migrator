package com.mindtree.holoxo.rules.metadata;

import com.mindtree.holoxo.util.HoloxoMetadataUtil;
import com.mindtree.models.vo.XLSMetadataRuleVO;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.service.AbstractXLSMetadataRule;

public class HoloxoCategoryRule extends AbstractXLSMetadataRule {
	
	public HoloxoCategoryRule() {
		// TODO Auto-generated constructor stub
	}


	public HoloxoCategoryRule(XLSMetadataRuleVO ruleVo) {
		super(ruleVo);
	}

	@Override
	public void apply() {
		if (ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.CATEGORY)) {
			ruleVO.brandMetadataValue = HoloxoMetadataUtil.dataCorrection(ruleVO.brandMetadataValue,
					MigratorConstants.CATEGORY);

			if (ruleVO.brandMetadataValue.contains(MigratorConstants.SPECIAL_CHARACTER_PIPE)) {
				String[] categories = ruleVO.brandMetadataValue.split(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT);

				for (String categoryValue : categories) {
					HoloxoMetadataUtil.putCategoryMDIntoMetadataMap(ruleVO.masterMetadataMap,
							ruleVO.assetMetadataMap, categoryValue.trim());
				}
			} else {
				HoloxoMetadataUtil.putCategoryMDIntoMetadataMap(ruleVO.masterMetadataMap,
						ruleVO.assetMetadataMap, ruleVO.brandMetadataValue.trim());
			}
		}
	}

}
