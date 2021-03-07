package com.mindtree.holoxo.rules.metadata;

import com.mindtree.holoxo.config.HoloxoReqConfigurationLoader;
import com.mindtree.models.vo.XLSMetadataRuleVO;
import com.mindtree.transformer.service.AppContext;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.MigrationUtil;
import com.mindtree.utils.service.AbstractXLSMetadataRule;

public class HoloxoDataCorrectionRule extends AbstractXLSMetadataRule {

	public HoloxoDataCorrectionRule(XLSMetadataRuleVO ruleVO) {
		super(ruleVO);
	}

	@Override
	public void apply() {
		if (ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.PPSN)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.SHADE)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.RETAILER)) {
			if (HoloxoReqConfigurationLoader.genericMap.get(ruleVO.brandMetadataHeader) != null
					&& HoloxoReqConfigurationLoader.genericMap.get(ruleVO.brandMetadataHeader).get(
							ruleVO.brandMetadataValue) != null
					&& !HoloxoReqConfigurationLoader.genericMap.get(ruleVO.brandMetadataHeader)
							.get(ruleVO.brandMetadataValue).isEmpty()) {
				ruleVO.brandMetadataValue = HoloxoReqConfigurationLoader.genericMap.get(ruleVO.brandMetadataHeader)
						.get(ruleVO.brandMetadataValue);
			}
		}

		ruleVO.assetMetadataMap.put(ruleVO.masterMetadataHeader, MigrationUtil.encode(ruleVO.brandMetadataValue));
	}

}
