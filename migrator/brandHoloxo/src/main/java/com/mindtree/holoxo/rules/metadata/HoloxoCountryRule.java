package com.mindtree.holoxo.rules.metadata;

import com.mindtree.holoxo.config.HoloxoReqConfigurationLoader;
import com.mindtree.models.vo.XLSMetadataRuleVO;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.service.AbstractXLSMetadataRule;

public class HoloxoCountryRule extends AbstractXLSMetadataRule {

	public HoloxoCountryRule(XLSMetadataRuleVO ruleVO) {
		super(ruleVO);
	}

	@Override
	public void apply() {
		if (ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.COUNTRY)) {

			if (ruleVO.brandMetadataValue.contains(MigratorConstants.SPECIAL_CHARACTER_PIPE)) {
				String[] values = ruleVO.brandMetadataValue.split(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT);
				StringBuilder aemMetadataValue = new StringBuilder();
				for (String value : values) {

					String aemValue = HoloxoReqConfigurationLoader.countryMap.get(value.trim()).get(
							MigratorConstants.AEM);
					aemMetadataValue.append(aemValue).append(MigratorConstants.SPECIAL_CHARACTER_PIPE);
				}
				ruleVO.assetMetadataMap.put(
						ruleVO.masterMetadataHeader,
						aemMetadataValue.toString().substring(0,
								aemMetadataValue.lastIndexOf(MigratorConstants.SPECIAL_CHARACTER_PIPE)));
			} else {
				if (HoloxoReqConfigurationLoader.countryMap.size() > 0
						&& HoloxoReqConfigurationLoader.countryMap.get(ruleVO.brandMetadataValue.trim()).get(
								MigratorConstants.AEM) != null) {
					String aemValue = HoloxoReqConfigurationLoader.countryMap.get(ruleVO.brandMetadataValue.trim())
							.get(MigratorConstants.AEM);
					ruleVO.assetMetadataMap.put(ruleVO.masterMetadataHeader, aemValue);

				}
			}

		}
	}

}
