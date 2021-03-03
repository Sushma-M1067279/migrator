package com.mindtree.holoxo.rules.xmp;

import java.util.Map;

import com.mindtree.models.vo.XMPMetadataRuleVO;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.BusinessRulesUtil;
import com.mindtree.utils.service.AbstractXMPMetadataRule;

public class HoloxoXMPColorLabelRule extends AbstractXMPMetadataRule {

	public HoloxoXMPColorLabelRule(XMPMetadataRuleVO ruleVO) {
		super(ruleVO);
	}

	@Override
	public void apply() {
		String colorLabelCode = ruleVO.xmpMetadata.getValue();

		if (null != colorLabelCode && !colorLabelCode.isEmpty()) {
			Map<String, String> colorLabelMap = BusinessRulesUtil.colorLabelMap;
			if (colorLabelMap != null && colorLabelMap.get(colorLabelCode) != null) {
				ruleVO.assetMetadataMapFromXMP.put(ruleVO.xmpMetadata.getKey().concat(MigratorConstants.TYPE_STRING),
						colorLabelMap.get(colorLabelCode));
			}
		}		
	}

}
