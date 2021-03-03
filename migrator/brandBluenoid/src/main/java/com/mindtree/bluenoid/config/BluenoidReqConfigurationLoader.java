package com.mindtree.bluenoid.config;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.exception.MigratorServiceException;
import com.mindtree.utils.helper.BusinessRulesUtil;
import com.mindtree.utils.helper.MigrationUtils;
import com.mindtree.utils.helper.ReadExcel;

/**
 * @author M1032046
 *
 */
public class BluenoidReqConfigurationLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(BluenoidReqConfigurationLoader.class);

	private static String brandAbbreviation = null;
	public static final Map<String, String> folderMappingMap = new HashMap<String, String>();
	public static final List<String> fileTypesToMigrate = new ArrayList<String>();
	public static final Map<String, Map<String, String>> customMetadataMap = new LinkedHashMap<String, Map<String, String>>();
	public static final Map<String, Map<String, String>> metadataMapByPathKey = new HashMap<>();
	public static final Map<String, Map<String, String>> customExtensionsMap = new HashMap<String, Map<String, String>>();
	public static final List<String> programFolderPathList = new ArrayList<String>();
	public static final List<String> duplicatePathList = new ArrayList<String>();

	public static String filePath = null;
	
	private static AmazonS3 s3Client = (AmazonS3) MigrationUtils.getStorageClient();

	/**
	 * Default Constructor
	 */
	public BluenoidReqConfigurationLoader() {
		throw new UnsupportedOperationException();
	}

	static {
		Properties prop;
		try {

			LOGGER.info("BluenoidReqConfigurationLoader : Loading static contents : Start..................");
			prop = MigrationUtils.getPropValues();
			brandAbbreviation ="BN";
			StringBuilder brandPrefix = MigrationUtils.prepareBrandPrefix(brandAbbreviation);
			String brandConfigFile = prop.getProperty(brandPrefix + ""
					+ MigratorConstants.BRAND_REQ_CONFIGURATION_FILENAME);
			String folderMappingSheet = prop.getProperty(brandPrefix.toString().concat("")
					.concat(MigratorConstants.BRAND_FOLDER_MAPPING_SHEETNAME));
			String buisnessMetadataMappingSheet = prop.getProperty(brandPrefix.toString().concat("")
					.concat(MigratorConstants.BRAND_METADATA_MAPPING_SHEETNAME));
			String fileTypesToMigrateSheet = prop.getProperty(brandPrefix.toString().concat("")
					.concat(MigratorConstants.BRAND_FILETYPES_TO_MIGRATE_SHEETNAME));
			String blankExtensionsSheet = prop.getProperty(brandPrefix.toString().concat("")
					.concat(MigratorConstants.BRAND_BLANK_EXTENSIONS_SHEETNAME));
			String programFolderPathsSheet = prop.getProperty(brandPrefix + ""
					+ MigratorConstants.PROGRAM_FOLDER_PATH_SHEETNAME);

			loadFolderMappingMap(brandConfigFile, folderMappingSheet);
			loadFileTypesToMigrateList(brandConfigFile, fileTypesToMigrateSheet);
			loadCustomMetadataMap(brandConfigFile, buisnessMetadataMappingSheet);
			if (blankExtensionsSheet != null) {
				loadCustomExtensionsMap(brandConfigFile, blankExtensionsSheet);
			}
			if (programFolderPathsSheet != null) {
				// loadProgramFolderPathList(brandConfigFile,
				// programFolderPathsSheet);
			}
			/*
			 * for(Map.Entry<String,String> folder:folderMappingMap.entrySet())
			 * {
			 * System.out.println("source::"+folder.getKey()+" target:"+folder.
			 * getValue()); }
			 * 
			 * for(String type:fileTypesToMigrate) {
			 * System.out.println("type:"+type); }
			 * 
			 * for(Entry<String, Map<String, String>>
			 * metadataMapByKey:metadataMapByPathKey.entrySet()) {
			 * System.out.println
			 * ("Path Key::"+metadataMapByKey.getKey()+" Metadat Map:"
			 * +metadataMapByKey.getValue()); }
			 * 
			 * for(Entry<String, Map<String, String>>
			 * extMapByKey:customExtensionsMap.entrySet()) {
			 * System.out.println("Path Key::"
			 * +extMapByKey.getKey()+" extMapByKey Map:"
			 * +extMapByKey.getValue()); }
			 */

			LOGGER.info("BluenoidReqConfigurationLoader : Loading static contents : Completed..................");

		} catch (MigratorServiceException e) {
			LOGGER.error(
					"BluenoidReqConfigurationLoader : Static block : Unable to load Brand Requierement Configurations:{}",
					e);
		}

		LOGGER.info("BluenoidReqConfigurationLoader : Loading static contents : End..................");
	}

	/**
	 * This method is to load folder mappings rules as per brand's requirement.
	 * 
	 * @param reqConfigFilename
	 * @param sheetname
	 * @throws MigratorServiceException
	 */
	private static void loadFolderMappingMap( String reqConfigFilename, String sheetname)
			throws MigratorServiceException {
		XSSFSheet assetSheet = null;
		DataFormatter formatter = new DataFormatter();
		String devMigrationConfigPath = MigrationUtils.getPropValues().getProperty(MigratorConstants.DEV_ASSET_MIG_CONFIG_PATH);
		assetSheet = ReadExcel.getExcelSheet(s3Client, reqConfigFilename, sheetname,
				devMigrationConfigPath.concat("/").concat(brandAbbreviation));
		Iterator<Row> rowIterator = assetSheet.iterator();
		while (rowIterator.hasNext()) {
			Row currentRow = rowIterator.next();
			if (currentRow.getRowNum() == 0)
				continue;
			else {

				Cell sourceFolder;
				Cell targetAEMFolder;

				sourceFolder = currentRow.getCell(0);
				targetAEMFolder = currentRow.getCell(1);

				String sourceFolderName = formatter.formatCellValue(sourceFolder);

				if (null != sourceFolder && null != targetAEMFolder && !sourceFolderName.isEmpty()
						&& !targetAEMFolder.getStringCellValue().isEmpty()) {
					folderMappingMap.put(sourceFolderName.toLowerCase().trim(), targetAEMFolder.getStringCellValue()
							.trim());
				}
			}

		}
	}

	/**
	 * This method is to load file types to migrate as per brand's requirement.
	 * 
	 * @param reqConfigFilename
	 * @param sheetname
	 * @throws MigratorServiceException
	 */
	private static void loadFileTypesToMigrateList(String reqConfigFilename, String sheetname)
			throws MigratorServiceException {
		XSSFSheet assetSheet = null;

		String devMigrationConfigPath = MigrationUtils.getPropValues().getProperty(MigratorConstants.DEV_ASSET_MIG_CONFIG_PATH);
		assetSheet = ReadExcel.getExcelSheet( s3Client, reqConfigFilename, sheetname,
				devMigrationConfigPath.concat("/").concat(brandAbbreviation));
		Iterator<Row> rowIterator = assetSheet.iterator();
		while (rowIterator.hasNext()) {
			Row currentRow = rowIterator.next();

			Cell fileType;

			fileType = currentRow.getCell(0);

			if (null != fileType && !fileType.getStringCellValue().isEmpty()) {
				fileTypesToMigrate.add(fileType.getStringCellValue().toLowerCase());
			}

		}
	}

	/**
	 * This method is to load custom metadata mapping rules as per Brand's
	 * requirements.
	 * 
	 * @param reqConfigFilename
	 * @param sheetname
	 * @throws MigratorServiceException
	 */
	private static void loadCustomMetadataMap(String reqConfigFilename, String sheetname)
			throws MigratorServiceException {
		XSSFSheet assetSheet = null;

		DataFormatter formatter = new DataFormatter();
		Map<String, Integer> headers = new HashMap<String, Integer>();
		String devMigrationConfigPath = MigrationUtils.getPropValues().getProperty(MigratorConstants.DEV_ASSET_MIG_CONFIG_PATH);
		assetSheet = ReadExcel.getExcelSheet(s3Client, reqConfigFilename, sheetname,
				devMigrationConfigPath.concat("/").concat(brandAbbreviation));

		/**
		 * Read excel headers
		 */
		XSSFRow row = assetSheet.getRow(0);
		Iterator<Cell> cells = row.iterator();

		while (cells.hasNext()) {
			Cell cell = cells.next();
			if (cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty()) {
				headers.put(cell.getStringCellValue(), cell.getColumnIndex());
			}
		}

		Iterator<Row> rowIterator = assetSheet.iterator();
		while (rowIterator.hasNext()) {
			Row currentRow = rowIterator.next();
			String pathKey = null;
			if (currentRow.getRowNum() == 0)
				continue;
			else {

				Map<String, String> customeMetadataMap = new HashMap<>();
				pathKey = getPathKey(formatter, headers, currentRow, pathKey, customeMetadataMap);

				if (pathKey != null && null != customeMetadataMap && customeMetadataMap.size() > 0) {
					customMetadataMap.put(pathKey, customeMetadataMap);
				}
			}

		}
	}

	private static String getPathKey(DataFormatter formatter, Map<String, Integer> headers, Row currentRow,
			String pathKey, Map<String, String> customeMetadataMap) {
		for (Entry<String, Integer> header : headers.entrySet()) {
			Cell cell;
			cell = currentRow.getCell(header.getValue());
			String value = formatter.formatCellValue(cell);

			if (value != null && value != "" && !value.isEmpty()) {
				if (header.getKey().equalsIgnoreCase("Keyword")) {
					pathKey = value;
				} else if (!header.getKey().equalsIgnoreCase("Rule")) {
					customeMetadataMap.put(header.getKey(), value);
				}
			}
		}
		return pathKey;
	}

	/**
	 * This method is to load custom extensions mapping rules as per Brand's
	 * requirements.
	 * 
	 * @param reqConfigFilename
	 * @param sheetname
	 * @throws MigratorServiceException
	 */
	private static void loadCustomExtensionsMap(String reqConfigFilename, String sheetname)
			throws MigratorServiceException {
		XSSFSheet assetSheet = null;

		Map<String, Integer> headersMap = new HashMap<String, Integer>();
		String devMigrationConfigPath = MigrationUtils.getPropValues().getProperty(MigratorConstants.DEV_ASSET_MIG_CONFIG_PATH);
		assetSheet = ReadExcel.getExcelSheet(s3Client, reqConfigFilename, sheetname,
				devMigrationConfigPath.concat("/").concat(brandAbbreviation));

		FileInputStream file;
		DataFormatter dataFormatter = new DataFormatter();

		assetSheet.forEach(row -> {
			filePath = null;
			int rowIndex = row.getRowNum();
			Map<String, String> readMap = new HashMap<String, String>();
			row.forEach(cell -> {
				String cellValue = dataFormatter.formatCellValue(cell);
				int cellIndex = cell.getColumnIndex();
				if (rowIndex == 0) {
					headersMap.put(cellValue, cell.getColumnIndex());
				} else {
					if (headersMap.size() > 0) {
						getFilepath(headersMap, readMap, cellValue, cellIndex);
					}
				}
			});
			if (rowIndex != 0)
				customExtensionsMap.put(filePath.trim(), readMap);
		});

		LOGGER.debug("Header:" + headersMap);
	}

	private static void getFilepath(Map<String, Integer> headersMap, Map<String, String> readMap, String cellValue,
			int cellIndex) {
		headersMap.forEach((k, v) -> {
			if (k.equalsIgnoreCase("File Path") && v == cellIndex) {
				filePath = cellValue;
			} else if (v == cellIndex) {
				readMap.put(k, cellValue);
			}

		});
	}

	/**
	 * This method is to load file types to migrate as per brand's requirement.
	 * 
	 * @param reqConfigFilename
	 * @param sheetname
	 * @throws MigratorServiceException
	 */
	private static void loadProgramFolderPathList(String reqConfigFilename, String sheetname)
			throws MigratorServiceException {
		XSSFSheet assetSheet = null;
		String devMigrationConfigPath = MigrationUtils.getPropValues().getProperty(MigratorConstants.DEV_ASSET_MIG_CONFIG_PATH);
		assetSheet = ReadExcel.getExcelSheet(s3Client, reqConfigFilename, sheetname,
				devMigrationConfigPath.concat("/").concat(brandAbbreviation));
		Iterator<Row> rowIterator = assetSheet.iterator();
		while (rowIterator.hasNext()) {
			Row currentRow = rowIterator.next();

			Cell folder;

			folder = currentRow.getCell(0);

			if (null != folder && !folder.getStringCellValue().isEmpty()) {
				programFolderPathList.add(folder.getStringCellValue().toLowerCase());
			}

		}
	}

	/**
	 * This method is to load duplicate file path as per brand's requirement.
	 * 
	 * @param reqConfigFilename
	 * @param sheetname
	 * @throws MigratorServiceException
	 */
	private static void loadDuplicatePathList(String reqConfigFilename, String sheetname)
			throws MigratorServiceException {
		XSSFSheet assetSheet = null;
		String devMigrationConfigPath = MigrationUtils.getPropValues().getProperty(MigratorConstants.DEV_ASSET_MIG_CONFIG_PATH);
		assetSheet = ReadExcel.getExcelSheet(s3Client, reqConfigFilename, sheetname,
				devMigrationConfigPath.concat("/").concat(brandAbbreviation));
		Iterator<Row> rowIterator = assetSheet.iterator();
		while (rowIterator.hasNext()) {
			Row currentRow = rowIterator.next();

			Cell folder;

			folder = currentRow.getCell(0);

			if (null != folder && !folder.getStringCellValue().isEmpty()) {
				programFolderPathList.add(folder.getStringCellValue().toLowerCase());
			}

		}
	}

}
