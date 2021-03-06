package com.mindtree.holoxo.rules.xmp;

import com.mindtree.models.vo.XMPMetadataRuleVO;
import com.mindtree.core.service.AppContext;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.MigrationUtil;
import com.mindtree.utils.service.AbstractXMPMetadataRule;

public class HoloxoXMPLabelRule extends AbstractXMPMetadataRule {
	
	public HoloxoXMPLabelRule() {
		// TODO Auto-generated constructor stub
	}

	public HoloxoXMPLabelRule(XMPMetadataRuleVO ruleVO) {
		super(ruleVO);
	}
	
	/**
	 * This method is to handle label - Approve, select, Alt values.
	 * 
	 * @param assetMetadataMapFromXMP
	 * @param xmpMetadata
	 * @param masterMetadataHeader
	 */

	@Override
	public void apply() {

		String selectAltApprove = ruleVO.xmpMetadata.getValue();
		if (null != selectAltApprove && selectAltApprove.equalsIgnoreCase(MigratorConstants.XMP_VALUE_ALT)) {
			selectAltApprove = MigratorConstants.AEM_XMP_VALUE_SECOND;
		}
		ruleVO.assetMetadataMapFromXMP.put(ruleVO.masterMetadataHeader, MigrationUtil.encode(selectAltApprove));

	}

}
