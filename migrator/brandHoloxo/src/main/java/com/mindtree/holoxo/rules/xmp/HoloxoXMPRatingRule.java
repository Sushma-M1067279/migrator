package com.mindtree.holoxo.rules.xmp;

import com.mindtree.models.vo.XMPMetadataRuleVO;
import com.mindtree.core.service.AppContext;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.MigrationUtil;
import com.mindtree.utils.service.AbstractXMPMetadataRule;

public class HoloxoXMPRatingRule extends AbstractXMPMetadataRule {
	
	public HoloxoXMPRatingRule() {
		// TODO Auto-generated constructor stub
	}

	public HoloxoXMPRatingRule(XMPMetadataRuleVO ruleVO) {
		super(ruleVO);
	}

	/**
	 * This method is to handle Rating and Kill fields.
	 * 
	 * @param assetMetadataMapFromXMP
	 * @param xmpMetadata
	 * @param masterMetadataHeader
	 */
	@Override
	public void apply() {

		String rating = ruleVO.xmpMetadata.getValue();
		if (null != rating && !rating.isEmpty()) {
			int rat = Integer.parseInt(rating);
			if (rat == -1) {
				ruleVO.assetMetadataMapFromXMP.put(MigratorConstants.AEM_XMP_FIELD_KILL,
						MigrationUtil.encode(ruleVO.xmpMetadata.getValue()));
			} else {
				ruleVO.assetMetadataMapFromXMP.put(ruleVO.masterMetadataHeader,
						MigrationUtil.encode(ruleVO.xmpMetadata.getValue()));
			}
		}

	}

}
