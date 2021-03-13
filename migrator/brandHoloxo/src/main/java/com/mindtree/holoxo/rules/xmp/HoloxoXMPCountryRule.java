package com.mindtree.holoxo.rules.xmp;

import com.mindtree.models.vo.XMPMetadataRuleVO;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.service.AbstractXMPMetadataRule;

public class HoloxoXMPCountryRule extends AbstractXMPMetadataRule {
	
	public HoloxoXMPCountryRule() {
		// TODO Auto-generated constructor stub
	}

	public HoloxoXMPCountryRule(XMPMetadataRuleVO ruleVO) {
		super(ruleVO);
	}

	@Override
	public void apply() {
		String country = ruleVO.xmpMetadata.getValue();
		if (ruleVO.assetMetadataMapFromXMP.get(MigratorConstants.AEM_PROPERTY_BOOST_SEARCH) != null
				&& !ruleVO.assetMetadataMapFromXMP.get(MigratorConstants.AEM_PROPERTY_BOOST_SEARCH).isEmpty()) {
			country = ruleVO.assetMetadataMapFromXMP.get(MigratorConstants.AEM_PROPERTY_BOOST_SEARCH)
					.concat(MigratorConstants.SPECIAL_CHARACTER_PIPE).concat(country);
		}
		ruleVO.assetMetadataMapFromXMP.put(MigratorConstants.AEM_PROPERTY_BOOST_SEARCH, country);

	}

}
