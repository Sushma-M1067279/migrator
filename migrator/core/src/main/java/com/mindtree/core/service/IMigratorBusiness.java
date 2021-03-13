package com.mindtree.core.service;

import java.util.List;
import java.util.Map;

import com.mindtree.models.dto.BrandMasterMappingDto;

/**
 * @author M1032046
 *
 */
public interface IMigratorBusiness {

	/**
	 * For Legacy DAM Source systems
	 */
	void applyBrandXLSMetadataRules(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMap, String brandMetadataHeader, String brandMetadataValue);

	boolean applyBrandFolderRules(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMap);

	void applyBrandXMPMetadataRules(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMapFromXMP, String exportFlowFlag, Map.Entry<String, String> xmpMetadata,
			String metadataHeader);

	/**
	 * For Drive based source systems
	 */
	Map<String, Object> applyBrandSpecificRules(Map<String, BrandMasterMappingDto> masterMetadataMap, Map.Entry<String, Long> s3Asset,
			String brand, String brandPrefix) throws MigratorServiceException;
	
	void setFilters(List<IFilter> filters);
	void setRules(List<IRule> rules);

}
