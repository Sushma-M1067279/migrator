package com.mindtree.models.vo;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mindtree.models.dto.BrandMasterMappingDto;

public class DriveBasedMetadataRuleVO extends RuleVO {

	public Map<String, String> assetMetadataMap;
	public Map<String, BrandMasterMappingDto> masterMetadataMap;
	public String destinationAssetPath;
	public List<String> missingAssets;
	public Map<String, String> migratedAssetsMap;
	public Entry<String, Long> s3Asset;

	public DriveBasedMetadataRuleVO(Map<String, String> assetMetadataMap,
			Map<String, BrandMasterMappingDto> masterMetadataMap, String destinationAssetPath,
			List<String> missingAssets, Map<String, String> migratedAssetsMap, Entry<String, Long> s3Asset) {
		this.assetMetadataMap = assetMetadataMap;
		this.masterMetadataMap = masterMetadataMap;
		this.destinationAssetPath = destinationAssetPath;
		this.missingAssets = missingAssets;
		this.migratedAssetsMap = migratedAssetsMap;
		this.s3Asset = s3Asset;
	}

}
