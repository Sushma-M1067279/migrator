package com.mindtree.models.vo;

import java.util.Map;
import java.util.Map.Entry;

public class ExtensionFilterVO extends RuleVO {

	public Map<String, String> migratedAssetsMap;
	public Map<String, String> nonMigratedAssetsMap;
	public String brandPrefix;
	public Entry<String, Long> s3Asset;
	public String destinationAssetPath;

	public ExtensionFilterVO(Map<String, String> migratedAssetsMap, Map<String, String> nonMigratedAssetsMap,
			String brandPrefix, Entry<String, Long> s3Asset, String destinationAssetPath) {
		this.migratedAssetsMap = migratedAssetsMap;
		this.nonMigratedAssetsMap = nonMigratedAssetsMap;
		this.brandPrefix = brandPrefix;
		this.s3Asset = s3Asset;
		this.destinationAssetPath = destinationAssetPath;
	}

}
