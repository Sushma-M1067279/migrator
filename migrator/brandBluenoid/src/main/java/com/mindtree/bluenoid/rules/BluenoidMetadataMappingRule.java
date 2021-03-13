package com.mindtree.bluenoid.rules;

import java.util.Map;

import com.mindtree.bluenoid.util.MetadataUtil;
import com.mindtree.models.vo.DriveBasedMetadataRuleVO;
import com.mindtree.utils.service.AbstractDriveBasedRule;

public class BluenoidMetadataMappingRule extends AbstractDriveBasedRule {
	
	public BluenoidMetadataMappingRule() {
		
	}

	public BluenoidMetadataMappingRule(DriveBasedMetadataRuleVO ruleVO) {
		super(ruleVO);
	}

	@Override
	public void apply() {
		/**
		 * Apply Business rules & metadata rules.
		 */
		ruleVO.assetMetadataMap = MetadataUtil.applyMetadataMappingRules(ruleVO.masterMetadataMap,
				ruleVO.destinationAssetPath);

	}

}
