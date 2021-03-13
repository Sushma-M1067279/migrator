package com.mindtree.bluenoid.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.mindtree.bluenoid.config.BluenoidReqConfigurationLoader;
import com.mindtree.models.dto.BrandMasterMappingDto;
import com.mindtree.core.service.AppContext;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.BusinessRulesUtil;

/**
 * @author M1032046
 *
 */
public class MetadataUtil {
	
	private static String fileSep = "" + AppContext.getStorage().fileSeparator();

	private MetadataUtil() {

	}

	/**
	 * Apply Business rules & metadata rules.
	 * 
	 * @param masterMetadataMap
	 * @param assetPath
	 * @return
	 */
	public static Map<String, String> applyMetadataMappingRules(
			Map<String, BrandMasterMappingDto> masterMetadataMap, String assetPath) {
		Map<String, String> assetMetadataMap = new HashMap<String, String>();
		if (BluenoidReqConfigurationLoader.customMetadataMap != null
				&& BluenoidReqConfigurationLoader.customMetadataMap.size() > 0) {

			for (Map.Entry<String, Map<String, String>> customMetatadata : BluenoidReqConfigurationLoader.customMetadataMap
					.entrySet()) {

				if (assetPath.toLowerCase().contains(customMetatadata.getKey().toLowerCase())) {
					Map<String, String> customMetadataMap = customMetatadata.getValue();

					if ((customMetatadata.getKey().equalsIgnoreCase(MigratorConstants.KEYWORD_BROW)
							&& assetPath.toLowerCase().contains(MigratorConstants.KEYWORD_BROWN))

							&& !customMetatadata.getKey().equalsIgnoreCase(MigratorConstants.KEYWORD_BROW_PATH)) {
						continue;
					}

					if (customMetadataMap.size() > 0) {
						for (Map.Entry<String, String> customMetadata : customMetadataMap.entrySet()) {
							String aemMetadataKey = customMetadata.getKey();
							aemMetadataKey = getMasterMDMapping(masterMetadataMap, aemMetadataKey);
							String columnValue = customMetadata.getValue();

							if (aemMetadataKey.trim().endsWith(MigratorConstants.TYPE_MULTI_STRING)
									&& assetMetadataMap.get(aemMetadataKey) != null
									&& !assetMetadataMap.get(aemMetadataKey).isEmpty()) {

								if (!assetMetadataMap.get(aemMetadataKey).contains(columnValue)) {
									String keyValue = assetMetadataMap.get(aemMetadataKey);

									String[] splitColumnVal = columnValue
											.split(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT);

									/*
									 * if(splitColumnVal.length>0) {
									 */
									String finalKeyValue = keyValue;
									for (String splitValue : splitColumnVal) {
										if (!Arrays.asList(keyValue.split(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT))
												.contains(splitValue)) {
											finalKeyValue = keyValue.concat(MigratorConstants.SPECIAL_CHARACTER_PIPE)
													.concat(splitValue);

											keyValue = finalKeyValue;
										}
									}

									columnValue = finalKeyValue;
									/*
									 * }else { columnValue = value
									 * .concat(MigratorConstants.SPECIAL_CHARACTER_PIPE).concat(splitValue); }
									 */

								} else {
									columnValue = assetMetadataMap.get(aemMetadataKey);
								}

							}
							/*if(aemMetadataKey.equalsIgnoreCase("dam:search_promote{{ String : multi }}")) {
								System.out.println("columnValue:"+columnValue);
							}*/
							assetMetadataMap.put(aemMetadataKey, columnValue);
						}

					}
				}
			}

		}
		return assetMetadataMap;
	}

	/**
	 * Apply Folder Mapping Rule (Taxonomy) : Refer Columns(In Brand Configuartion
	 * sheet) : Folder Name -> Mapped AEM Assets Folder Path Find out absTargetPath.
	 * 
	 * @param assetPath
	 * @param assetMetadataMap
	 * @param brand
	 */
	public static void applyFolderMappingRule(String assetPath, Map<String, String> assetMetadataMap, String brand) {
		String assetMainFolderName = null;
		String[] assetsFolders = assetPath.split("\\"+fileSep);
		if (assetsFolders.length > 0 && assetsFolders[0] != null) {
			assetMainFolderName = assetsFolders[0];

			String subPath = null;

			if (BluenoidReqConfigurationLoader.folderMappingMap != null && assetMainFolderName != null) {

				if (BluenoidReqConfigurationLoader.folderMappingMap.get(assetMainFolderName.toLowerCase()) != null) {
					subPath = assetPath.substring(assetMainFolderName.length() + 1, assetPath.length());
					/**
					 * capture folder structure for folder creation
					 */
					updateAbsTargetPath(assetMetadataMap, assetMainFolderName, subPath, brand);
				} else {
					if (assetsFolders.length > 1) {
						System.out.println("assetMainFolderName:" + assetMainFolderName);
						assetMainFolderName = assetMainFolderName.concat(fileSep).concat(assetsFolders[1]);
						subPath = assetPath.substring(assetMainFolderName.length() + 1, assetPath.length());

						if (BluenoidReqConfigurationLoader.folderMappingMap
								.get(assetMainFolderName.toLowerCase()) != null) {
							/**
							 * capture folder structure for folder creation
							 */
							updateAbsTargetPath(assetMetadataMap, assetMainFolderName, subPath, brand);
						}
					}
				}

			}
		}
	}

	/**
	 * Apply metadata rule : Assets included in the online/2018 folders will have
	 * subfolders within it all nmed by program name. Use the folder name as the
	 * program name Metadata Value
	 * 
	 * @param brandMasterMappingMap
	 * @param assetPath
	 * @param assetMetadataMap
	 * @param programFolderRulePath
	 */
	public static void applyProgramSubFolderMetadataRule(Map<String, BrandMasterMappingDto> brandMasterMappingMap,
			String assetPath, Map<String, String> assetMetadataMap, String programSubFolderRulePath) {
		if (assetPath.toLowerCase().startsWith(programSubFolderRulePath)) {
			String subPath = assetPath.substring(programSubFolderRulePath.length() + 1, assetPath.length());
			String[] subFolders = subPath.split(MigratorConstants.FILE_SEPARETOR);

			if (subFolders.length > 0) {
				String aemMetadataKey = MigratorConstants.METADATA_PROGRAM_NAME;
				aemMetadataKey = getMasterMDMapping(brandMasterMappingMap, aemMetadataKey);

				assetMetadataMap.put(aemMetadataKey, subFolders[0]);
			}

		}
	}

	/**
	 * This method updates the absTargetPath metadata.
	 * 
	 * @param assetMetadataMap
	 * @param assetMainFolderName
	 * @param subPath
	 * @param brand
	 */
	private static void updateAbsTargetPath(Map<String, String> assetMetadataMap, String assetMainFolderName,
			String subPath, String brand) {
		String aemTargetFolder = BluenoidReqConfigurationLoader.folderMappingMap.get(assetMainFolderName.toLowerCase());
		String subPath1 = subPath.replace('\\', '/');
		String assetTargetPath = brand.concat("/").concat(aemTargetFolder).concat("/").concat(subPath1);

		assetMetadataMap.put(MigratorConstants.ABS_TARGET_PATH, assetTargetPath);
		BusinessRulesUtil.absTargetPathsSet.add(assetTargetPath.toLowerCase());

		if (aemTargetFolder.toLowerCase().startsWith("wip")) {
			assetMetadataMap.put(MigratorConstants.ASSET_STATUS, "WIP");
		} else if (aemTargetFolder.toLowerCase().startsWith("final")) {
			assetMetadataMap.put(MigratorConstants.ASSET_STATUS, "Final");
		}
	}

	/**
	 * This method gets the AEM mapping property name for each custom metadata
	 * defined by brand.
	 * 
	 * @param brandMasterMappingMap
	 * @param metadataKey
	 * @return
	 */
	private static String getMasterMDMapping(Map<String, BrandMasterMappingDto> brandMasterMappingMap,
			String metadataKey) {
		if (brandMasterMappingMap.containsKey(metadataKey)) {
			BrandMasterMappingDto brandMasterMappingDto = brandMasterMappingMap.get(metadataKey);
			metadataKey = brandMasterMappingDto.getAemPropertyName().concat(brandMasterMappingDto.getFieldType());

		}
		return metadataKey;
	}

	public static void applyProgramFolderMetadataRule(Map<String, BrandMasterMappingDto> brandMasterMappingMap,
			String assetPath, Map<String, String> assetMetadataMap) {
		String[] subFolders = assetPath.split(MigratorConstants.FILE_SEPARETOR);
		if (subFolders.length > 0) {
			String mainFolderName = subFolders[0].trim();

			if (BluenoidReqConfigurationLoader.programFolderPathList.contains(mainFolderName.toLowerCase())) {
				String programName = mainFolderName;
				if (mainFolderName.startsWith(MigratorConstants.FISCAL_YEAR_2018)) {
					programName = mainFolderName.substring(MigratorConstants.FISCAL_YEAR_2018.length() + 1,
							mainFolderName.length());
				}

				String aemMetadataKey = MigratorConstants.METADATA_PROGRAM_NAME;
				aemMetadataKey = getMasterMDMapping(brandMasterMappingMap, aemMetadataKey);

				assetMetadataMap.put(aemMetadataKey, programName);
			}
		}

	}
}
