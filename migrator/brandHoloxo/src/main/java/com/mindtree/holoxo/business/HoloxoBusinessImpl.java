package com.mindtree.holoxo.business;

import java.util.Map;
import java.util.Map.Entry;

import com.mindtree.holoxo.config.HoloxoReqConfigurationLoader;
import com.mindtree.holoxo.rules.folder.HoloxoFolderRule;
import com.mindtree.holoxo.rules.metadata.HoloxoAssetTypeRule;
import com.mindtree.holoxo.rules.metadata.HoloxoCategoryRule;
import com.mindtree.holoxo.rules.metadata.HoloxoCountryRule;
import com.mindtree.holoxo.rules.metadata.HoloxoDataCorrectionRule;
import com.mindtree.holoxo.rules.metadata.HoloxoMetadataFolderMappingRule;
import com.mindtree.holoxo.rules.metadata.HoloxoOtherDropdownRule;
import com.mindtree.holoxo.rules.xmp.HoloxoXMPColorLabelRule;
import com.mindtree.holoxo.rules.xmp.HoloxoXMPCountryRule;
import com.mindtree.holoxo.rules.xmp.HoloxoXMPLabelRule;
import com.mindtree.holoxo.rules.xmp.HoloxoXMPRatingRule;
import com.mindtree.models.dto.BrandMasterMappingDto;
import com.mindtree.models.vo.FolderRuleVO;
import com.mindtree.models.vo.XLSMetadataRuleVO;
import com.mindtree.models.vo.XMPMetadataRuleVO;
import com.mindtree.utils.business.IMigratorBusiness;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.exception.MigratorServiceException;
import com.mindtree.utils.helper.MigrationUtils;
import com.mindtree.utils.service.RulesEvaluator;

/**
 * Holoxo brand specific rules implementation
 * 
 * @author M1032046
 *
 */
public class HoloxoBusinessImpl implements IMigratorBusiness {
	
	/**
	 * For Legacy DAM Source systems
	 */
	@Override
	public void applyBrandXLSMetadataRules(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMap, String brandMetadataHeader, String brandMetadataValue) {

		// Process asset as per holoxo metadata requirements/rules
		RulesEvaluator rulesEvaluator = new RulesEvaluator();

		String masterMetadataHeader = brandMetadataHeader;
		// if master metadata map contains mapping for this brand metadata
		// header, then get the master header
		if (masterMetadataMap.containsKey(brandMetadataHeader)) {
			BrandMasterMappingDto brandMasterMappingDto = masterMetadataMap.get(brandMetadataHeader);
			// replace the brand header with master header (aem property)
			masterMetadataHeader = brandMasterMappingDto.getAemPropertyName().concat(
					brandMasterMappingDto.getFieldType());
		}
		XLSMetadataRuleVO ruleVO = new XLSMetadataRuleVO(masterMetadataMap, assetMetadataMap, brandMetadataHeader,
				brandMetadataValue, masterMetadataHeader);

		rulesEvaluator.addRule(new HoloxoAssetTypeRule(ruleVO)).addRule(new HoloxoCountryRule(ruleVO))
				.addRule(new HoloxoCategoryRule(ruleVO)).addRule(new HoloxoOtherDropdownRule(ruleVO))
				.addRule(new HoloxoDataCorrectionRule(ruleVO)).addRule(new HoloxoMetadataFolderMappingRule(ruleVO));

		rulesEvaluator.evaluateAllRules();

	}

	@Override
	public boolean applyBrandFolderRules(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMap) {
		boolean needsMigration = true;
		FolderRuleVO ruleVO = new FolderRuleVO(masterMetadataMap, assetMetadataMap);

		RulesEvaluator rulesEvaluator = new RulesEvaluator();

		rulesEvaluator.addRule(new HoloxoFolderRule(ruleVO));

		rulesEvaluator.evaluateAllRules();

		return needsMigration;
	}

	@Override
	public void applyBrandXMPMetadataRules(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMapFromXMP, String exportFlowFlag, Entry<String, String> xmpMetadata,
			String metadataHeader) {
		String masterMetadataHeader;
		// Get mapping for this metadata header
		BrandMasterMappingDto brandMasterMappingDto = masterMetadataMap.get(metadataHeader);

		if (exportFlowFlag != null && exportFlowFlag.equalsIgnoreCase(MigratorConstants.CSV_EXPORT_FLOW)) {
			masterMetadataHeader = brandMasterMappingDto.getAemPropertyName().concat(
					brandMasterMappingDto.getFieldType());
		} else {
			masterMetadataHeader = brandMasterMappingDto.getFieldType()
					.concat(MigratorConstants.SPECIAL_CHARACTER_PIPE)
					.concat(brandMasterMappingDto.getAemPropertyName());
		}
		RulesEvaluator rulesEvaluator = new RulesEvaluator();
		XMPMetadataRuleVO ruleVO = new XMPMetadataRuleVO(masterMetadataMap, assetMetadataMapFromXMP, exportFlowFlag,
				xmpMetadata, metadataHeader, masterMetadataHeader);

		rulesEvaluator.addRule(new HoloxoXMPRatingRule(ruleVO)).addRule(new HoloxoXMPLabelRule(ruleVO))
				.addRule(new HoloxoXMPCountryRule(ruleVO)).addRule(new HoloxoXMPColorLabelRule(ruleVO));

		assetMetadataMapFromXMP.put(masterMetadataHeader, MigrationUtils.encode(xmpMetadata.getValue()));

		rulesEvaluator.evaluateAllRules();

		assetMetadataMapFromXMP.put(masterMetadataHeader, MigrationUtils.encode(xmpMetadata.getValue()));
	}

	/**
	 * For Drive based source systems
	 * @return 
	 */
	@Override
	public Map<String, Object> applyBrandSpecificRules(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Entry<String, Long> s3Asset, String brand, String brandPrefix) throws MigratorServiceException {
		throw new UnsupportedOperationException();
	}

}
