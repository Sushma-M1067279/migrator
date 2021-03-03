package com.mindtree.models.vo;

import java.util.Map;

import com.mindtree.models.dto.BrandMasterMappingDto;

public class XMPMetadataRuleVO {
	public Map<String, BrandMasterMappingDto> masterMetadataMap;
	public Map<String, String> assetMetadataMapFromXMP;
	public String exportFlowFlag;
	public Map.Entry<String, String> xmpMetadata;
	public String metadataHeader;
	public String masterMetadataHeader;

	public XMPMetadataRuleVO(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMapFromXMP, String exportFlowFlag, Map.Entry<String, String> xmpMetadata,
			String metadataHeader, String masterMetadataHeader) {
		this.masterMetadataMap = masterMetadataMap;
		this.assetMetadataMapFromXMP = assetMetadataMapFromXMP;
		this.exportFlowFlag = exportFlowFlag;
		this.xmpMetadata = xmpMetadata;
		this.metadataHeader = metadataHeader;
		this.masterMetadataHeader = masterMetadataHeader;
	}
}
