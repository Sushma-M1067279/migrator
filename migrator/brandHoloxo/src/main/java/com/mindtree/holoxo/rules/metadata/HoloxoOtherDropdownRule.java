package com.mindtree.holoxo.rules.metadata;

import java.util.Map;

import com.mindtree.holoxo.config.HoloxoReqConfigurationLoader;
import com.mindtree.holoxo.util.HoloxoMetadataUtil;
import com.mindtree.models.vo.XLSMetadataRuleVO;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.service.AbstractXLSMetadataRule;

public class HoloxoOtherDropdownRule extends AbstractXLSMetadataRule {
	
	public HoloxoOtherDropdownRule() {
		// TODO Auto-generated constructor stub
	}

	public HoloxoOtherDropdownRule(XLSMetadataRuleVO ruleVO) {
		super(ruleVO);
	}

	@Override
	public void apply() {
		if (ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.REGION)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.LANGUAGE)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.PHOTOGRAPHER_RIGHTS_USAGE)
				|| ruleVO.brandMetadataHeader.contains(MigratorConstants.MODEL_ETHNICITY)
				|| ruleVO.brandMetadataHeader.equalsIgnoreCase(MigratorConstants.USAGE)
				|| HoloxoMetadataUtil.photographerUsageFields.stream().anyMatch(
						ruleVO.brandMetadataHeader::equalsIgnoreCase)) {

			if (ruleVO.brandMetadataHeader.contains(MigratorConstants.MODEL_ETHNICITY)) {
				ruleVO.brandMetadataHeader = MigratorConstants.MODEL_ETHNICITY;
			} else if (HoloxoMetadataUtil.photographerUsageFields.stream().anyMatch(
					ruleVO.brandMetadataHeader::equalsIgnoreCase)) {
				ruleVO.brandMetadataHeader = MigratorConstants.PHOTOGRAPHER_RIGHTS_USAGE;
			}

			Map<String, String> map = HoloxoReqConfigurationLoader.genericMap.get(ruleVO.brandMetadataHeader);

			if (ruleVO.brandMetadataValue.contains(MigratorConstants.SPECIAL_CHARACTER_PIPE)) {
				String[] values = ruleVO.brandMetadataValue.split(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT);
				StringBuilder aemMetadataValue = new StringBuilder();
				for (String value : values) {

					String aemValue = value.trim();
					if (map.size() > 0 && map.get(value.trim()) != null) {
						aemValue = map.get(value.trim());
					}

					aemMetadataValue.append(aemValue).append(MigratorConstants.SPECIAL_CHARACTER_PIPE);
				}
				ruleVO.assetMetadataMap.put(
						ruleVO.masterMetadataHeader,
						aemMetadataValue.toString()
								.substring(0, aemMetadataValue.lastIndexOf(MigratorConstants.SPECIAL_CHARACTER_PIPE))
								.trim());
			} else {
				String aemValue = ruleVO.brandMetadataValue.trim();
				if (map.size() > 0 && map.get(ruleVO.brandMetadataValue) != null) {
					aemValue = map.get(ruleVO.brandMetadataValue);
				}
				ruleVO.assetMetadataMap.put(ruleVO.masterMetadataHeader, aemValue);
			}

		}

	}

}
