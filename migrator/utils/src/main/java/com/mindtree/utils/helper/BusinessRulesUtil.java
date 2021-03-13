package com.mindtree.utils.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.mindtree.models.dto.BrandMasterMappingDto;
import com.mindtree.core.service.AppContext;
import com.mindtree.core.service.MigratorServiceException;
import com.mindtree.utils.constants.EnumUtil;
import com.mindtree.utils.constants.MigratorConstants;


/**
 * @author M1032046
 *
 */
public class BusinessRulesUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessRulesUtil.class);
	private static final Map<String, String> usageMap = new HashMap<String, String>();
	static final Set<String> aemNameSpaces = new HashSet<String>();
	public static final Map<String, String> colorLabelMap = new HashMap<String, String>();
	static final Map<String, String> defaultMDDataTypeMap = new HashMap<String, String>();
	public static final Map<String, String> MimeTypeMap = new HashMap<String, String>();
	public static Set<String> assetsPathsSet = new HashSet<String>();
	public static Set<String> absTargetPathsSet = new HashSet<String>();

	private BusinessRulesUtil() {
	}

	static {
		try {
			Properties prop = AppContext.getAppConfig();
			String masterBrandMappingFileName = AppContext.getAppConfig().getProperty("migrator.asset.masterBrandMappingFileName");
			String usageMappingSheetName = prop.getProperty(MigratorConstants.BRAND_USAGE_MAPPING_SHEETNAME);
			String nameSpaceSheetName = prop.getProperty(MigratorConstants.AEM_NAME_SPACE_SHEETNAME);
			String defaultMDSheetName = prop.getProperty(MigratorConstants.AEM_DEFAULT_METADATA_TYPE_SHEETNAME);
			String mimeTypeSheetName = prop.getProperty(MigratorConstants.AEM_MIME_TYPE_SHEETNAME);
			fillUsageMap(masterBrandMappingFileName, usageMappingSheetName);
			fillNameSpaceSet( masterBrandMappingFileName, nameSpaceSheetName);
			fillColorLabelMap();
			fillDefaultMDDataTypeMap(masterBrandMappingFileName, defaultMDSheetName);
			loadMimeTypeMap( masterBrandMappingFileName, mimeTypeSheetName);

		} catch (MigratorServiceException e) {
			LOGGER.error("BusinessRulesUtil : Static block : Unable to load usage mapping :{}", e);
		}

	}

	private static void fillDefaultMDDataTypeMap(String masterBrandMappingFileName, String defaultMDSheetName)
			throws MigratorServiceException {
		XSSFSheet assetSheet = null;
		String devMigrationConfigPath = AppContext.getAppConfig().getProperty(MigratorConstants.DEV_ASSET_MIG_CONFIG_PATH);
		assetSheet = ReadExcel.getExcelSheet(masterBrandMappingFileName, defaultMDSheetName, devMigrationConfigPath);
		Iterator<Row> rowIterator = assetSheet.iterator();
		while (rowIterator.hasNext()) {
			Row currentRow = rowIterator.next();
			if (currentRow.getRowNum() == 0)
				continue;
			else {

				Cell metadataKey;
				Cell metadataType;

				metadataKey = currentRow.getCell(0);
				metadataType = currentRow.getCell(1);

				if (null != metadataKey && null != metadataType && !metadataKey.getStringCellValue().isEmpty()
						&& !metadataType.getStringCellValue().isEmpty()) {
					defaultMDDataTypeMap.put(metadataKey.getStringCellValue(), metadataType.getStringCellValue());
				}
			}

		}
	}

	private static void loadMimeTypeMap(String masterBrandMappingFileName, String sheetname)
			throws MigratorServiceException {
		XSSFSheet assetSheet = null;
		String devMigrationConfigPath = AppContext.getAppConfig().getProperty(MigratorConstants.DEV_ASSET_MIG_CONFIG_PATH);
		assetSheet = ReadExcel.getExcelSheet(masterBrandMappingFileName, sheetname, devMigrationConfigPath);
		Iterator<Row> rowIterator = assetSheet.iterator();
		while (rowIterator.hasNext()) {
			Row currentRow = rowIterator.next();
			if (currentRow.getRowNum() == 0)
				continue;
			else {

				Cell metadataKey;
				Cell metadataType;

				metadataKey = currentRow.getCell(0);
				metadataType = currentRow.getCell(1);

				if (null != metadataKey && null != metadataType && !metadataKey.getStringCellValue().isEmpty()
						&& !metadataType.getStringCellValue().isEmpty()) {
					MimeTypeMap.put(metadataKey.getStringCellValue(), metadataType.getStringCellValue());
				}
			}

		}
	}

	private static void fillColorLabelMap() {
		colorLabelMap.put("1", "Red");
		colorLabelMap.put("2", "Green");
		colorLabelMap.put("3", "Blue");
		colorLabelMap.put("5", "Pink + Purple");
		colorLabelMap.put("6", "Orange");
		colorLabelMap.put("7", "Yellow");
	}

	private static void fillNameSpaceSet(String masterBrandMappingFileName, String nameSpaceFileName)
			throws MigratorServiceException {
		XSSFSheet assetSheet = null;
		String devMigrationConfigPath = AppContext.getAppConfig().getProperty(MigratorConstants.DEV_ASSET_MIG_CONFIG_PATH);
		assetSheet = ReadExcel.getExcelSheet(masterBrandMappingFileName, nameSpaceFileName, devMigrationConfigPath);
		Iterator<Row> rowIterator = assetSheet.iterator();
		while (rowIterator.hasNext()) {
			Row currentRow = rowIterator.next();
			if (currentRow.getRowNum() == 0)
				continue;
			else {
				Cell namespace;

				namespace = currentRow.getCell(0);

				if (null != namespace && !namespace.getStringCellValue().isEmpty()) {
					aemNameSpaces.add(namespace.getStringCellValue());
				}
			}

		}
	}

	private static void fillUsageMap(String masterBrandMappingFileName, String usageMappingFileName)
			throws MigratorServiceException {
		XSSFSheet assetSheet = null;
		String devMigrationConfigPath = AppContext.getAppConfig().getProperty(MigratorConstants.DEV_ASSET_MIG_CONFIG_PATH);
		assetSheet = ReadExcel.getExcelSheet(masterBrandMappingFileName, usageMappingFileName, devMigrationConfigPath);
		Iterator<Row> rowIterator = assetSheet.iterator();
		while (rowIterator.hasNext()) {
			Row currentRow = rowIterator.next();
			if (currentRow.getRowNum() == 0)
				continue;
			else {

				Cell usageKey;
				Cell usageValue;

				usageKey = currentRow.getCell(0);
				usageValue = currentRow.getCell(1);

				if (null != usageKey && null != usageValue && !usageKey.getStringCellValue().isEmpty()
						&& !usageValue.getStringCellValue().isEmpty()) {
					usageMap.put(usageKey.getStringCellValue(), usageValue.getStringCellValue());
				}
			}

		}
	}

	/**
	 * applyRegionBusinessRule class applies business rules on region field.
	 * 
	 * @param excelMDMap
	 * @param path
	 * @param foldersList
	 * @param brandMasterMappingMap
	 * @param exportFlowFlag
	 */
	public static void applyRegionBusinessRule(Map<String, String> excelMDMap, List<String> foldersList,
			Map<String, BrandMasterMappingDto> brandMasterMappingMap, String exportFlowFlag) {

		String region = "Region";
		String country = "country";
		region = getMasterMDMapping(brandMasterMappingMap, region, exportFlowFlag);
		country = getMasterMDMapping(brandMasterMappingMap, country, exportFlowFlag);
		if (foldersList.contains("bluenoid Global")) {
			excelMDMap.put(region, MigrationUtil.encode("APAC - Asia and Pacific"));
			excelMDMap.put(country, MigrationUtil.encode("Japan"));
		} else if (foldersList.contains("bluenoid Local")) {
			excelMDMap.put(region, MigrationUtil.encode("Global"));
		} else if (foldersList.contains(MigratorConstants.TRAVEL_RETAIL_STRING)) {
			excelMDMap.put(region, MigrationUtil.encode(MigratorConstants.TRAVEL_RETAIL_STRING));
		}
	}

	/**
	 * This method is prepares the metadata according to the business rules for
	 * brand.
	 * 
	 * @param excelMDMap
	 * @param path
	 * @param foldersList
	 * @param brandMasterMappingMap
	 * @param dbExportFlow
	 */
	public static void applyOtherBusinessRule(Map<String, String> excelMDMap, String path, List<String> foldersList,
			Map<String, BrandMasterMappingDto> brandMasterMappingMap, String exportFlowFlag) {
		String usage = MigratorConstants.USAGE;
		usage = getMasterMDMapping(brandMasterMappingMap, usage, exportFlowFlag);
		String usageValue = null;
		if (foldersList.contains(MigratorConstants.FOLDER_PROGRAMS)) {
			applyProgramFolderRules(excelMDMap, path, brandMasterMappingMap, exportFlowFlag);

		} else if (foldersList.contains(MigratorConstants.FOLDER_DIGITAL)) {
			applyDigitalFolderRules(excelMDMap, path, brandMasterMappingMap, exportFlowFlag);

		} else if (foldersList.contains(MigratorConstants.FOLDER_IMAGES)) {
			usageValue = MigratorConstants.USAGE_RANDOM_PACKSHOTS;
		} else if (foldersList.contains("Education")) {
			usageValue = MigratorConstants.USAGE_EDUCATION;
		} else if (foldersList.contains("Consumer Events")) {
			usageValue = MigratorConstants.USAGE_CONSUMER_EVENTS;
		} else if (foldersList.contains("CRM")) {
			usageValue = MigratorConstants.USAGE_CRM;
		} else if (foldersList.contains("Visual Merchandising")) {
			usageValue = MigratorConstants.USAGE_VISUAL_MARCHANDISE;
		} else if (foldersList.contains("Travel Retail")) {
			usageValue = MigratorConstants.TRAVEL_RETAIL;
		}

		if (null != usageValue) {
			excelMDMap.put(usage, MigrationUtil.encode(usageValue));
		}
	}

	private static String getMasterMDMapping(Map<String, BrandMasterMappingDto> brandMasterMappingMap,
			String metadataKey, String exportFlowFlag) {
		if (brandMasterMappingMap.containsKey(metadataKey)) {
			BrandMasterMappingDto brandMasterMappingDto = brandMasterMappingMap.get(metadataKey);
			if (exportFlowFlag != null && exportFlowFlag.equalsIgnoreCase(MigratorConstants.CSV_EXPORT_FLOW)) {
				metadataKey = brandMasterMappingDto.getAemPropertyName().concat(brandMasterMappingDto.getFieldType());
			} else {
				metadataKey = brandMasterMappingDto.getFieldType().concat(MigratorConstants.SPECIAL_CHARACTER_PIPE)
						.concat(brandMasterMappingDto.getAemPropertyName());
			}

		}
		return metadataKey;
	}

	private static void applyDigitalFolderRules(Map<String, String> excelMDMap, String path,
			Map<String, BrandMasterMappingDto> brandMasterMappingMap, String exportFlowFlag) {
		String assetType = "Asset Type";
		assetType = getMasterMDMapping(brandMasterMappingMap, assetType, exportFlowFlag);
		String usage = MigratorConstants.USAGE;
		usage = getMasterMDMapping(brandMasterMappingMap, usage, exportFlowFlag);
		excelMDMap.put(assetType, MigrationUtil.encode("Layout Digital"));
		String[] futurePath = path.split("Social");
		if (futurePath != null && futurePath.length > 1) {
			String splitRegex = Pattern.quote(System.getProperty("file.separator"));
			String[] subPath = futurePath[1].split(splitRegex);
			if (subPath != null && subPath.length > 1) {
				String social = subPath[1];
				if (EnumUtil.contains(EnumUtil.SOCIAL.class, social.trim().toUpperCase())) {
					excelMDMap.put(usage, MigrationUtil.encode("Social - " + social.trim()));
				}
			}
		}
	}

	private static void applyProgramFolderRules(Map<String, String> excelMDMap, String path,
			Map<String, BrandMasterMappingDto> brandMasterMappingMap, String exportFlowFlag) {
		String assetType = "Asset Type";
		assetType = getMasterMDMapping(brandMasterMappingMap, assetType, exportFlowFlag);
		String usage = MigratorConstants.USAGE;
		usage = getMasterMDMapping(brandMasterMappingMap, usage, exportFlowFlag);
		String productName = "headline";
		productName = getMasterMDMapping(brandMasterMappingMap, productName, exportFlowFlag);

		excelMDMap.put(assetType, MigrationUtil.encode("Layout Print"));
		String[] futurePath = path.split("FY");
		getFuturePath(excelMDMap, productName, futurePath);
		if (!usageMap.isEmpty()) {
			for (Map.Entry<String, String> usageKeyword : usageMap.entrySet()) {
				if (path.contains(usageKeyword.getKey())) {
					if (usageKeyword.getValue() != null && !usageKeyword.getValue().isEmpty()) {
						excelMDMap.put(usage, MigrationUtil
								.encode(usageKeyword.getValue().replaceAll(", ", MigratorConstants.SPECIAL_CHARACTER_PIPE)));
					}

				}
			}
		}
	}

	private static void getFuturePath(Map<String, String> excelMDMap, String productName, String[] futurePath) {
		if (futurePath != null && futurePath.length > 1) {
			String splitRegex = Pattern.quote(System.getProperty("file.separator"));
			String[] subPath = futurePath[1].split(splitRegex);
			if (subPath != null && subPath.length > 1) {
				String programName = subPath[1];
				LOGGER.info("BusinessRulesUtil applyProgramFolderRules : programName :", programName);
				if (!programName.isEmpty()) {
					excelMDMap.put(productName, MigrationUtil.encode(programName));

				}
			}
		}
	}

}
