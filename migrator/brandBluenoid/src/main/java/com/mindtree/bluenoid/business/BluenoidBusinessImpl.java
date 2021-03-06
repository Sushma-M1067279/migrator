package com.mindtree.bluenoid.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.bluenoid.filters.BluenoidEmptyAssetFilter;
import com.mindtree.bluenoid.filters.BluenoidExtensionBasedFilter;
import com.mindtree.bluenoid.filters.BluenoidFileTypeFilter;
import com.mindtree.bluenoid.rules.BluenoidDefaultMetadataRule;
import com.mindtree.bluenoid.rules.BluenoidFolderMappingRule;
import com.mindtree.bluenoid.rules.BluenoidMetadataMappingRule;
import com.mindtree.bluenoid.rules.BluenoidProgramFolderMetadataRule;
import com.mindtree.bluenoid.rules.BluenoidProgramSubfolderMetadataRule;
import com.mindtree.models.dto.BrandMasterMappingDto;
import com.mindtree.models.vo.DriveBasedMetadataRuleVO;
import com.mindtree.models.vo.ExtensionFilterVO;
import com.mindtree.core.service.IFilter;
import com.mindtree.core.service.IMigratorBusiness;
import com.mindtree.core.service.IRule;
import com.mindtree.core.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.service.FilterEvaluator;
import com.mindtree.utils.service.RulesEvaluator;

/**
 * @author M1032046
 *
 */
public class BluenoidBusinessImpl implements IMigratorBusiness {
	
	static final Logger LOGGER = LoggerFactory.getLogger(BluenoidBusinessImpl.class);
	List<IFilter> filters;
	List<IRule> rules;


	/**
	 * For DAM based Source systems
	 */

	@Override
	public void applyBrandXLSMetadataRules(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMap, String brandMetadataHeader, String brandMetadataValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean applyBrandFolderRules(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void applyBrandXMPMetadataRules(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMapFromXMP, String exportFlowFlag, Entry<String, String> xmpMetadata,
			String metadataHeader) {
		throw new UnsupportedOperationException();
	}

	/**
	 * For Drive based source systems
	 */

	@Override
	public Map<String, Object> applyBrandSpecificRules(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Entry<String, Long> s3Asset, String brand, String brandPrefix) throws MigratorServiceException {
		Map<String, Object> outputMap = new HashMap<String, Object>();
		Map<String, String> migratedAssetsMap = new HashMap<String, String>();
		Map<String, String> nonMigratedAssetsMap = new HashMap<String, String>();
		List<String> missingAssets = new ArrayList<>();
		LOGGER.info("Applying bluenoid specific filters");
		String sourceAssetPath = s3Asset.getKey().trim();
		String destinationAssetPath = sourceAssetPath;

		Boolean eligibleToMigrate = true;
		ExtensionFilterVO filterVO = new ExtensionFilterVO(migratedAssetsMap, nonMigratedAssetsMap, brandPrefix,
				s3Asset, destinationAssetPath);

		/**
		 * Apply filters
		 */
		LOGGER.info("Applying bluenoid specific filters : "+s3Asset.getKey());
		FilterEvaluator filterEvaluator = new FilterEvaluator();
		for (int i = 0; i < filters.size(); i++) {
			IFilter filter = filters.get(i);
			filter.setVO(filterVO);
			filterEvaluator.addFilter(filter);
		}
		
		eligibleToMigrate = filterEvaluator.evaluateAllFilters();

		/**
		 * Apply rules
		 */
		if (eligibleToMigrate) {
			LOGGER.info("Applying bluenoid specific rules : "+s3Asset.getKey());
			Map<String, String> assetMetadataMap = new HashMap<String, String>();

			DriveBasedMetadataRuleVO ruleVO = new DriveBasedMetadataRuleVO(assetMetadataMap, masterMetadataMap,
					destinationAssetPath, missingAssets, migratedAssetsMap, s3Asset);

			RulesEvaluator rulesEvaluator = new RulesEvaluator();
			
			for (int i = 0; i < rules.size(); i++) {
				IRule rule = rules.get(i);
				rule.setVO(ruleVO);
				rulesEvaluator.addRule(rule);
			}
			
			rulesEvaluator.evaluateAllRules();

			outputMap.put(MigratorConstants.OUTPUT_ASSET_METADATA_MAP, ruleVO.assetMetadataMap);
			outputMap.put(MigratorConstants.OUTPUT_MIGRATED_ASSETS_MAP, ruleVO.migratedAssetsMap);
			outputMap.put(MigratorConstants.OUTPUT_NON_MIGRATED_ASSETS_MAP, nonMigratedAssetsMap);
			outputMap.put(MigratorConstants.OUTPUT_MISSING_ASSETS, ruleVO.missingAssets);

		}

		return outputMap;
	}

	/**
	 * @param filters the filters to set
	 */
	public void setFilters(List<IFilter> filters) {
		this.filters = filters;
	}

	/**
	 * @param rules the rules to set
	 */
	public void setRules(List<IRule> rules) {
		this.rules = rules;
	}
}
