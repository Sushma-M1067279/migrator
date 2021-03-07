package com.mindtree.holoxo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.holoxo.config.HoloxoReqConfigurationLoader;
import com.mindtree.models.dto.BrandMasterMappingDto;
import com.mindtree.transformer.service.AppContext;
import com.mindtree.transformer.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;

/**
 * This class contains Holoxo brand specific rules for metadata population
 * 
 * @author M1032046
 *
 */
public class HoloxoMetadataUtil {

	public static List<String> folderMappingList = new ArrayList<>();
	/**
	 * This map is populated by rules
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(HoloxoMetadataUtil.class);
	public static Map<String, String> folderMappingMap = new HashMap<>();
	public static String brandPrefix = "migrator.HX.";
	public static List<String> photographerUsageFields = Arrays.asList("photographer_01_rights_usage",
			"photographer_01_rights_exclusion_usage", "photographer_02_rights_usage",
			"photographer_02_rights_exclusion_usage", "photographer_03_rights_usage",
			"photographer_03_rights_exclusion_usage", "photographer_04_rights_usage",
			"photographer_04_rights_exclusion_usage", "photographer_05_rights_usage",
			"photographer_05_rights_exclusion_usage", "model_01_rights_usage", "model_01_rights_exclusion_usage",
			"model_02_rights_usage", "model_02_rights_exclusion_usage", "model_03_rights_usage",
			"model_03_rights_exclusion_usage", "model_04_rights_usage", "model_04_rights_exclusion_usage",
			"model_05_rights_usage", "model_05_rights_exclusion_usage");

	public static String folderMappingMethod(Map<String, String> assetMetadataMap) throws MigratorServiceException {

		Properties prop = AppContext.getAppConfig();
		String brand = prop.getProperty(brandPrefix + "" + MigratorConstants.BRAND);
		String holoxoMigrationType = prop.getProperty(brandPrefix + "" + MigratorConstants.BRAND_ASSET_MIGRATION_TYPE);

		String fileName = folderMappingMap.get(MigratorConstants.COLUMN_FILE_NAME);
		StringBuilder aemTargetPath = new StringBuilder(brand);

		String currentLegacyDAMpath = folderMappingMap.get(MigratorConstants.PATH_COLUMN).replaceAll("\\\\\\\\",
				MigratorConstants.SPECIAL_CHARACTER_SLASH_STRING);

		if (assetMetadataMap.get(MigratorConstants.MIGRATION_BATCH) != null
				&& assetMetadataMap.get(MigratorConstants.MIGRATION_BATCH).equalsIgnoreCase(
						MigratorConstants.AGENCY_ASSET_NO_METADATA)) {
			aemTargetPath.append(MigratorConstants.SPECIAL_CHARACTER_SLASH_STRING).append(
					MigratorConstants.AGENCY_TEMP_FOLDER_FOR_NO_METADATA_ASSETS);
		} else {
			LOGGER.info("folderMappingMap :"+folderMappingMap.size());
			for(String key: folderMappingMap.keySet()){
				LOGGER.info(key+ " : "+folderMappingMap.get(key));
			}
			String assetStatus = getAssetStatus(folderMappingMap.get(MigratorConstants.ASSET_TYPE));
			aemTargetPath.append(MigratorConstants.SPECIAL_CHARACTER_SLASH_STRING).append(assetStatus);
			if (assetStatus != null && assetStatus.equalsIgnoreCase(MigratorConstants.FINAL)) {
				updateFinalAssetPath(folderMappingMap, holoxoMigrationType, aemTargetPath, currentLegacyDAMpath);
			} else {
				updateWIPAssetPath(aemTargetPath, currentLegacyDAMpath);
			}
		}

		/**
		 * Append File name to the end of folder path.
		 */
		aemTargetPath.append(MigratorConstants.SPECIAL_CHARACTER_SLASH).append(fileName);
		assetMetadataMap.put(MigratorConstants.DC_TITLE, fileName);

		return aemTargetPath.toString();
	}

	public static void updateWIPAssetPath(StringBuilder aemTargetPath, String currentGEpath) {
		/**
		 * WIP Folder structure - Maintain As Is folder structure as in current
		 * DAM
		 */
		currentGEpath = currentGEpath.substring(currentGEpath.indexOf('-') + 1, currentGEpath.length());
		String[] folders = currentGEpath.split(MigratorConstants.SPECIAL_CHARACTER_SLASH_STRING);
		for (String folder : folders) {
			aemTargetPath.append(MigratorConstants.SPECIAL_CHARACTER_SLASH).append(folder.trim());
		}
	}

	public static void updateFinalAssetPath(Map<String, String> folderMappingMap, String cliniqueMigrationType,
			StringBuilder aemTargetPath, String currentGEpath) {
		/**
		 * Append Calendar season year or product icon folder name as second
		 * folder
		 */
		String calendarSY = folderMappingMap.get(MigratorConstants.CALENDAR_SEASON_YEAR);
		if (calendarSY != null) {
			aemTargetPath.append(MigratorConstants.SPECIAL_CHARACTER_SLASH).append(
					calendarSY.replaceAll(MigratorConstants.SPECIAL_CHARACTER_COMMA,
							MigratorConstants.SPECIAL_CHARACTER_UNDERSCORE));
		} else {
			aemTargetPath.append(MigratorConstants.SPECIAL_CHARACTER_SLASH).append(MigratorConstants.PRODUCT_ICON);
		}

		/**
		 * Append PPSN name as folder name as 3rd folder
		 */
		if (cliniqueMigrationType != null && cliniqueMigrationType.equalsIgnoreCase(MigratorConstants.GLOBAL_EDIT)
				&& !currentGEpath.startsWith("USER UPLOADS")) {
			String[] folders = currentGEpath.split(MigratorConstants.SPECIAL_CHARACTER_SLASH_STRING);
			aemTargetPath.append(MigratorConstants.SPECIAL_CHARACTER_SLASH).append(folders[2].trim());
		} else {
			aemTargetPath.append(MigratorConstants.SPECIAL_CHARACTER_SLASH).append(
					folderMappingMap
							.get(MigratorConstants.PPSN)
							.trim()
							.replaceAll(MigratorConstants.SPECIAL_CHARACTER_COMMA_AND_SLASH,
									MigratorConstants.SPECIAL_CHARACTER_UNDERSCORE));
		}

		/**
		 * Append asset type as folder name as 4th folder.
		 */
		String assetType = folderMappingMap.get(MigratorConstants.ASSET_TYPE);

		if (assetType != null) {

			if (assetType.contains(MigratorConstants.COLON)) {
				// String aemAssetType = assetType.substring(0,
				// assetType.indexOf(":")).trim();
				Map<String, Map<String, String>> assetTypeMap = HoloxoReqConfigurationLoader.assetTypeMap;
				assetType = assetTypeMap.get(assetType).get(MigratorConstants.AEM_FOLDER_MAPPING);
				aemTargetPath.append(MigratorConstants.SPECIAL_CHARACTER_SLASH).append(assetType.trim());
			}
		}

		/**
		 * Append Region folder as 5th folder
		 */
		if (folderMappingMap.get(MigratorConstants.REGION) != null) {
			String regionValue = folderMappingMap.get(MigratorConstants.REGION);
			String finalRegionFolder = null;
			if (regionValue.contains(MigratorConstants.SPECIAL_CHARACTER_PIPE)) {
				String[] regions = regionValue.split(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT);
				StringBuilder finalRegionFolderBuf = new StringBuilder();
				for (String region : regions) {
					String regionFolder = region.trim();
					if (region.contains(MigratorConstants.HYPHEN)) {
						regionFolder = region.substring(0, region.indexOf('-') - 1).trim();
					}
					finalRegionFolderBuf.append(regionFolder).append(MigratorConstants.HYPHEN);
				}
				finalRegionFolder = finalRegionFolderBuf.toString()
						.substring(0, finalRegionFolderBuf.lastIndexOf(MigratorConstants.HYPHEN)).trim();
			} else {
				finalRegionFolder = regionValue.trim();
				if (regionValue.contains(MigratorConstants.HYPHEN)) {
					finalRegionFolder = regionValue.substring(0, regionValue.indexOf('-') - 1).trim();
				}

			}

			aemTargetPath.append(MigratorConstants.SPECIAL_CHARACTER_SLASH).append(finalRegionFolder.trim());
		} else {

			Map<String, Map<String, String>> countryMap = HoloxoReqConfigurationLoader.countryMap;
			Map<String, String> countryRegionMap = countryMap.get(folderMappingMap.get(MigratorConstants.COUNTRY));
			aemTargetPath.append(MigratorConstants.SPECIAL_CHARACTER_SLASH).append(
					countryRegionMap.get(MigratorConstants.AEM_FOLDER_MAPPING).trim());

		}

		/**
		 * Append Country folder as 6th folder.
		 */
		if (folderMappingMap.get(MigratorConstants.COUNTRY) != null) {
			String country = folderMappingMap.get(MigratorConstants.COUNTRY);

			if (country.contains(MigratorConstants.SPECIAL_CHARACTER_PIPE)) {
				String[] countries = country.split(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT);
				StringBuilder finalCountryFolderBuf = new StringBuilder();
				for (String countryName : countries) {

					finalCountryFolderBuf.append(countryName.trim()).append(MigratorConstants.HYPHEN);
				}
				country = finalCountryFolderBuf.toString().substring(0,
						finalCountryFolderBuf.lastIndexOf(MigratorConstants.HYPHEN));
			}

			aemTargetPath.append(MigratorConstants.SPECIAL_CHARACTER_SLASH).append(country.trim());
		}
	}

	// public static void applyOtherDropdownMetadataRule(Map<String, String>
	// assetMetadataMap, String brandMetadataHeader,
	// String brandMetadataValue, String masterMetadataHeader) {
	//
	// if (brandMetadataHeader.contains(MigratorConstants.MODEL_ETHNICITY)) {
	// brandMetadataHeader = MigratorConstants.MODEL_ETHNICITY;
	// } else if
	// (photographerUsageFields.stream().anyMatch(brandMetadataHeader::equalsIgnoreCase))
	// {
	// brandMetadataHeader = MigratorConstants.PHOTOGRAPHER_RIGHTS_USAGE;
	// }
	//
	// Map<String, String> map =
	// HoloxoReqConfigurationLoader.genericMap.get(brandMetadataHeader);
	//
	// if
	// (brandMetadataValue.contains(MigratorConstants.SPECIAL_CHARACTER_PIPE)) {
	// String[] values =
	// brandMetadataValue.split(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT);
	// StringBuilder aemMetadataValue = new StringBuilder();
	// for (String value : values) {
	//
	// String aemValue = value.trim();
	// if (map.size() > 0 && map.get(value.trim()) != null) {
	// aemValue = map.get(value.trim());
	// }
	//
	// aemMetadataValue.append(aemValue).append(MigratorConstants.SPECIAL_CHARACTER_PIPE);
	// }
	// assetMetadataMap.put(
	// masterMetadataHeader,
	// aemMetadataValue.toString()
	// .substring(0,
	// aemMetadataValue.lastIndexOf(MigratorConstants.SPECIAL_CHARACTER_PIPE))
	// .trim());
	// } else {
	// String aemValue = brandMetadataValue.trim();
	// if (map.size() > 0 && map.get(brandMetadataValue) != null) {
	// aemValue = map.get(brandMetadataValue);
	// }
	// assetMetadataMap.put(masterMetadataHeader, aemValue);
	// }
	//
	// }

	// public static void applyCategoryMetadataRule(Map<String,
	// BrandMasterMappingDto> brandMasterMappingMap,
	// Map<String, String> assetMetadataMap, String brandMetadataValue) {
	//
	// brandMetadataValue = dataCorrection(brandMetadataValue,
	// MigratorConstants.CATEGORY);
	//
	// if
	// (brandMetadataValue.contains(MigratorConstants.SPECIAL_CHARACTER_PIPE)) {
	// String[] categories =
	// brandMetadataValue.split(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT);
	//
	// for (String categoryValue : categories) {
	// putCategoryMDIntoMetadataMap(brandMasterMappingMap, assetMetadataMap,
	// categoryValue.trim());
	// }
	// } else {
	// putCategoryMDIntoMetadataMap(brandMasterMappingMap, assetMetadataMap,
	// brandMetadataValue.trim());
	// }
	//
	// }

	public static void putCategoryMDIntoMetadataMap(Map<String, BrandMasterMappingDto> brandMasterMappingMap,
			Map<String, String> assetMetadataMap, String brandMetadataValue) {
		String masterMetadataHeader;
		if (HoloxoReqConfigurationLoader.categoryMap.size() > 0
				&& HoloxoReqConfigurationLoader.categoryMap.get(brandMetadataValue) != null) {
			Map<String, String> catMetadataMap = HoloxoReqConfigurationLoader.categoryMap.get(brandMetadataValue);

			for (Map.Entry<String, String> catMetadata : catMetadataMap.entrySet()) {
				masterMetadataHeader = catMetadata.getKey();
				if (brandMasterMappingMap.containsKey(catMetadata.getKey())) {
					BrandMasterMappingDto brandMasterMappingDto = brandMasterMappingMap.get(catMetadata.getKey());
					masterMetadataHeader = brandMasterMappingDto.getAemPropertyName().concat(
							brandMasterMappingDto.getFieldType());
				}
				brandMetadataValue = catMetadata.getValue();
				if (masterMetadataHeader.endsWith(MigratorConstants.TYPE_MULTI_STRING)) {
					if (assetMetadataMap.get(masterMetadataHeader) != null
							&& !assetMetadataMap.get(masterMetadataHeader).isEmpty()) {
						brandMetadataValue = assetMetadataMap.get(masterMetadataHeader)
								.concat(MigratorConstants.SPECIAL_CHARACTER_PIPE).concat(brandMetadataValue);

						String[] brandMetadataValues = Arrays
								.stream(brandMetadataValue.split(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT))
								.distinct().toArray(String[]::new);
						brandMetadataValue = "";
						for (String value : brandMetadataValues) {
							brandMetadataValue = brandMetadataValue.concat(MigratorConstants.SPECIAL_CHARACTER_PIPE)
									.concat(value);
						}
						if (brandMetadataValue.startsWith(MigratorConstants.SPECIAL_CHARACTER_PIPE)) {
							brandMetadataValue = brandMetadataValue.substring(1, brandMetadataValue.length());
						}

					}
				}

				assetMetadataMap.put(masterMetadataHeader, brandMetadataValue);
			}

		}
	}

	public static String dataCorrection(String brandMetadataValue, String type) {
		if (HoloxoReqConfigurationLoader.genericMap.get(type) != null
				&& HoloxoReqConfigurationLoader.genericMap.get(type).get(brandMetadataValue) != null
				&& !HoloxoReqConfigurationLoader.genericMap.get(type).get(brandMetadataValue).isEmpty()) {
			brandMetadataValue = HoloxoReqConfigurationLoader.genericMap.get(type).get(brandMetadataValue);
		}
		return brandMetadataValue;
	}

	// public static void applyCountryMetadataRule(Map<String, String>
	// assetMetadataMap, String brandMetadataValue,
	// String masterMetadataHeader) {
	//
	// if
	// (brandMetadataValue.contains(MigratorConstants.SPECIAL_CHARACTER_PIPE)) {
	// String[] values =
	// brandMetadataValue.split(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT);
	// StringBuilder aemMetadataValue = new StringBuilder();
	// for (String value : values) {
	//
	// String aemValue =
	// HoloxoReqConfigurationLoader.countryMap.get(value.trim()).get(MigratorConstants.AEM);
	// aemMetadataValue.append(aemValue).append(MigratorConstants.SPECIAL_CHARACTER_PIPE);
	// }
	// assetMetadataMap.put(
	// masterMetadataHeader,
	// aemMetadataValue.toString().substring(0,
	// aemMetadataValue.lastIndexOf(MigratorConstants.SPECIAL_CHARACTER_PIPE)));
	// } else {
	// if (HoloxoReqConfigurationLoader.countryMap.size() > 0
	// && HoloxoReqConfigurationLoader.countryMap.get(brandMetadataValue.trim())
	// .get(MigratorConstants.AEM) != null) {
	// String aemValue =
	// HoloxoReqConfigurationLoader.countryMap.get(brandMetadataValue.trim()).get(
	// MigratorConstants.AEM);
	// assetMetadataMap.put(masterMetadataHeader, aemValue);
	//
	// }
	// }
	//
	// }

	// public static void applyAssetTypeMetadataRule(Map<String, String>
	// assetMetadataMap, String brandMetadataValue,
	// String masterMetadataHeader) {
	//
	// String assetStatus = getAssetStatus(brandMetadataValue);
	//
	// assetMetadataMap.put(MigratorConstants.ASSET_STATUS, assetStatus);
	//
	// if (HoloxoReqConfigurationLoader.assetTypeMap.size() > 0
	// &&
	// HoloxoReqConfigurationLoader.assetTypeMap.get(brandMetadataValue.trim())
	// != null
	// &&
	// HoloxoReqConfigurationLoader.assetTypeMap.get(brandMetadataValue.trim()).get(MigratorConstants.AEM)
	// != null) {
	// String aemValue =
	// HoloxoReqConfigurationLoader.assetTypeMap.get(brandMetadataValue).get(
	// MigratorConstants.AEM);
	// assetMetadataMap.put(masterMetadataHeader, aemValue);
	//
	// }
	//
	// }

	public static String getAssetStatus(String brandMetadataValue) {
		String assetStatus = null;
		if (brandMetadataValue.toUpperCase().endsWith(MigratorConstants.FINAL)) {
			assetStatus = MigratorConstants.ASSET_STATUS_FINAL;
		} else if (brandMetadataValue.toUpperCase().endsWith(MigratorConstants.WIP)
				|| brandMetadataValue.toUpperCase().endsWith(MigratorConstants.RAW)) {
			assetStatus = MigratorConstants.WIP;
		}

		return assetStatus;
	}

}
