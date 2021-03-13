package com.mindtree.holoxo.rules.metadata;

import com.mindtree.holoxo.config.HoloxoReqConfigurationLoader;
import com.mindtree.holoxo.util.HoloxoMetadataUtil;
import com.mindtree.models.vo.XLSMetadataRuleVO;
import com.mindtree.models.vo.RuleVO;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.service.AbstractXLSMetadataRule;

public class HoloxoAssetTypeRule extends AbstractXLSMetadataRule {
	
	public HoloxoAssetTypeRule() {
		// TODO Auto-generated constructor stub
	}

	public HoloxoAssetTypeRule(XLSMetadataRuleVO ruleVO) {
		super(ruleVO);
	}

	@Override
	public void apply() {
		if (ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.ASSET_TYPE)) {

			String assetStatus = HoloxoMetadataUtil.getAssetStatus(ruleVO.brandMetadataValue);

			ruleVO.assetMetadataMap.put(MigratorConstants.ASSET_STATUS, assetStatus);

			if (HoloxoReqConfigurationLoader.assetTypeMap.size() > 0
					&& HoloxoReqConfigurationLoader.assetTypeMap.get(ruleVO.brandMetadataValue.trim()) != null
					&& HoloxoReqConfigurationLoader.assetTypeMap.get(ruleVO.brandMetadataValue.trim()).get(
							MigratorConstants.AEM) != null) {
				String aemValue = HoloxoReqConfigurationLoader.assetTypeMap.get(ruleVO.brandMetadataValue).get(
						MigratorConstants.AEM);
				ruleVO.assetMetadataMap.put(ruleVO.masterMetadataHeader, aemValue);

			}

		}
	}

}
