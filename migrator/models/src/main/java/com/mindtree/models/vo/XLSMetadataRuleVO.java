package com.mindtree.models.vo;

import java.util.Map;

import com.mindtree.models.dto.BrandMasterMappingDto;

public class XLSMetadataRuleVO extends RuleVO {
	public Map<String, BrandMasterMappingDto> masterMetadataMap;
	public Map<String, String> assetMetadataMap;
	public String brandMetadataHeader;
	public String brandMetadataValue;
	public String masterMetadataHeader;

	public XLSMetadataRuleVO(Map<String, BrandMasterMappingDto> masterMetadataMap, Map<String, String> assetMetadataMap,
			String brandMetadataHeader, String brandMetadataValue, String masterMetadataHeader) {
		this.masterMetadataMap = masterMetadataMap;
		this.assetMetadataMap = assetMetadataMap;
		this.brandMetadataHeader = brandMetadataHeader;
		this.brandMetadataValue = brandMetadataValue;
		this.masterMetadataHeader = masterMetadataHeader;
	}
}
