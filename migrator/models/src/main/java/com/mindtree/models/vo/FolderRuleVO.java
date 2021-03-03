package com.mindtree.models.vo;

import java.util.Map;

import com.mindtree.models.dto.BrandMasterMappingDto;

public class FolderRuleVO extends RuleVO {
	public Map<String, BrandMasterMappingDto> masterMetadataMap;
	public Map<String, String> assetMetadataMap;

	public FolderRuleVO(Map<String, BrandMasterMappingDto> masterMetadataMap, Map<String, String> assetMetadataMap) {
		this.masterMetadataMap = masterMetadataMap;
		this.assetMetadataMap = assetMetadataMap;
	}
}
