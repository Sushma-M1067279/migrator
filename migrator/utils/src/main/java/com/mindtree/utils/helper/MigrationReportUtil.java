package com.mindtree.utils.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.CopyPartRequest;
import com.amazonaws.services.s3.model.CopyPartResult;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.exception.MigratorServiceException;

/**
 * @author M1032046
 *
 */
public class MigrationReportUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MigrationReportUtil.class);

	private static List<String> dateMetadataFields = Arrays.asList("jcr:created", "xmp:CreateDate", "xmp:MetadataDate",
			"xmp:ModifyDate", "expiration_date", "photoshop:DateCreated", "exif:DateTimeOriginal",
			"videoRightsStartDate", "model1RightsStartDate", "videoRightsExpirationDate",
			"photographer2RightsStartDate", "photographer1RightsStartDate",
			"illustratorRightsExpirationDate", "expiration_date", "model2RightsExpirationDate",
			"model2RightsStartDate", "photographer2RightsExpirationDate",
			"illustratorRightsStartDate", "photographer1RightsExpirationDate",
			"model1RightsExpirationDate", "model4RightsStartDate", "model3RightsExpirationDate",
			"model4RightsExpirationDate", "model3RightsStartDate", "model5RightsStartDate",
			"model5RightsExpirationDate", "musicRightsStartDate", "musicRightsExpirationDate",
			"photographer4RightsExpirationDate");

	private MigrationReportUtil() {
		super();
	}

	private static List<String> dateFormats = new ArrayList<String>();

	static {
		dateFormats.add("MM/dd/yyyy HH:MM a");
		dateFormats.add("MMM dd yyyy HH:mma");
		dateFormats.add("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		dateFormats.add("yyyy:MM:dd HH:mm:ss");
		dateFormats.add("yyyy-MM-dd'T'HH:mm");
		dateFormats.add("yyyy-MM-dd'T'HH:mm:ss.SSS");
		dateFormats.add(MigratorConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS_SSSZ);
		dateFormats.add("MM/dd/yyyy HH:MM a");
		dateFormats.add("yyyy-MM-dd HH:mm:ss.SSS");
		dateFormats.add("dd/MM/yyyy HH:MM a");
		// dateFormats.add("dd/MM/yyyy");
		dateFormats.add("MM/dd/yyyy");

	}

	/**
	 * This method is to write final migration metadata to excel file for CSV
	 * importer.
	 * 
	 * @param brandPrefix
	 * @param removeAemContent
	 * @param finalAssetMetadataMapList
	 * @param finalAssetMetadataMapKeySet
	 */
	public static boolean generateOutputAndReplicateAssets(AmazonS3 s3Client, StringBuilder brandPrefix,
			Set<String> finalAssetMetadataMapKeySet, List<Map<String, String>> finalAssetMetadataMapList) {
		boolean isSuccess = false;
		XSSFWorkbook workbook = null;
		XSSFWorkbook folderCreatorWorkbook = null;
		try {
			/**
			 * Read brand specific properties.
			 */
			Properties prop = MigrationUtils.getPropValues();

			String migrationCSVReportName = prop.getProperty(brandPrefix + ""
					+ MigratorConstants.MIGRATION_CSV_REPORT_PATH);

			String migrationFolderCreatorReportPath = prop.getProperty(brandPrefix + ""
					+ MigratorConstants.MIGRATION_FOLDER_CREATOR_REPORT_PATH);

			String renditionsTargetPath = prop.getProperty(brandPrefix + ""
					+ MigratorConstants.MIGRATION_RENDITION_TARGET_PATH);

			String s3ReplicationFlag = prop.getProperty(brandPrefix + ""
					+ MigratorConstants.MIGRATION_S3_REPLICATION_ON_OFF_FLAG);

			String migrationType = prop.getProperty(brandPrefix + "" + MigratorConstants.BRAND_ASSET_MIGRATION_TYPE);

			String contentDamPrefix = "/content/dam/";
			// Blank workbook
			workbook = new XSSFWorkbook();
			folderCreatorWorkbook = new XSSFWorkbook();

			// Create a blank sheets
			/**
			 * This sheet is for csv metadata to be consumed by CSV Asset
			 * importer
			 */
			XSSFSheet migrationSheet = workbook.createSheet("Asset_Migration");
			/**
			 * This sheet is to be consumed for Asset folder creator
			 */
			XSSFSheet assetPathSheet = folderCreatorWorkbook.createSheet("Asset_Folder");
			Map<String, Integer> headersMap = new HashMap<String, Integer>();

			int rownum = 0;
			int cellnum = 0;
			finalAssetMetadataMapKeySet.add(MigratorConstants.MIGRATION_BATCH);
			finalAssetMetadataMapKeySet.add(MigratorConstants.RENDITIONS_TARGET_KEY);
			finalAssetMetadataMapKeySet.add(MigratorConstants.DC_TITLE);

			LOGGER.info("MigrationReportUtil : writeToCSV & S3 replication : total migration assets count:{}",
					finalAssetMetadataMapList.size());
			if (finalAssetMetadataMapKeySet.size() > 0 && finalAssetMetadataMapList.size() > 0) {

				Row migrationHeaders = migrationSheet.createRow(rownum++);
				LOGGER.info("MigrationReportUtil : writeToCSV & S3 replication : replication count:{}", rownum);

				for (String header : finalAssetMetadataMapKeySet) {
					String headerKey = header;
					String finalHeader = header;
					if (!header.equalsIgnoreCase(MigratorConstants.CSV_IMPORTER_COLUMN_SRC_REL_PATH)
							&& !header.equalsIgnoreCase(MigratorConstants.ABS_TARGET_PATH)) {
						String[] headerSplit = header.split(MigratorConstants.SPECIAL_CHARACTER_COLON);
						if (headerSplit.length > 1 && !BusinessRulesUtil.aemNameSpaces.contains(headerSplit[0])) {
							if (headerSplit[0].equalsIgnoreCase(MigratorConstants.METADATA_PREFIX_AUX)) {
								header = header.replace(MigratorConstants.METADATA_PREFIX_AUX,
										MigratorConstants.METADATA_PREFIX_PSAUX);
							} else {
								header = header.replace(MigratorConstants.SPECIAL_CHARACTER_COLON,
														MigratorConstants.SPECIAL_CHARACTER_UNDERSCORE);
//								header = MigratorConstants.MASTER_NAME_SPACE.concat(
//										MigratorConstants.SPECIAL_CHARACTER_COLON).concat(
//										header.replace(MigratorConstants.SPECIAL_CHARACTER_COLON,
//												MigratorConstants.SPECIAL_CHARACTER_UNDERSCORE));
							}
						} else if (headerSplit.length == 1) {
//							header = MigratorConstants.MASTER_NAME_SPACE.concat(
//									MigratorConstants.SPECIAL_CHARACTER_COLON).concat(header);
							header = header;
						}
					}

					if (header.toLowerCase().contains("date") && !header.toLowerCase().contains("{{ date }}")) {
						finalHeader = header.concat("{{ Date }}");
					} else {

						finalHeader = header.trim();
						header = header.trim();
						Map<String, String> defaultMDDataTypeMap = BusinessRulesUtil.defaultMDDataTypeMap;
						if (header.contains(MigratorConstants.SPECIAL_CHARACTER_CURLY_BRACES)) {
							header = header.substring(0,
									header.indexOf(MigratorConstants.SPECIAL_CHARACTER_CURLY_BRACES));
						}

						if (defaultMDDataTypeMap.containsKey(header)) {
							finalHeader = header.concat(defaultMDDataTypeMap.get(header));
						}
					}
					Cell headerColumn = migrationHeaders.createCell(cellnum++);
					headerColumn.setCellValue(finalHeader);
					headersMap.put(headerKey, headerColumn.getColumnIndex());
				}

				Map<String, Map<String, String>> pathsMap = new HashMap<>();

				for (Map<String, String> assetMetadataMap : finalAssetMetadataMapList) {
					String assetFileName = null;
					String assetRelativePath = null;
					String assetFileNameOriginal = null;
					String assetTargetPath = null;
					String assetId = null;
					Row assetMDRow = migrationSheet.createRow(rownum++);
					String[] renditions = null;
					for (Entry<String, String> metadata : assetMetadataMap.entrySet()) {
						if (metadata.getValue() != null && !metadata.getValue().isEmpty()
								&& headersMap.containsKey(metadata.getKey())) {

							Cell metadataValue = assetMDRow.createCell(headersMap.get(metadata.getKey()));

							String mdvalue = metadata.getValue();
							if (mdvalue.contains("\n")) {
								mdvalue = mdvalue.replaceAll("\n", " ");
							}

							if (metadata.getKey().equalsIgnoreCase(MigratorConstants.ABS_TARGET_PATH)) {
								/**
								 * capture folder structure for folder creation
								 */

								assetTargetPath = preparePathForFolderCreator(StringUtils.substring(mdvalue, 0, 32767),
										pathsMap);
								String targetPath = assetTargetPath;
								targetPath = contentDamPrefix.concat(assetTargetPath);
								metadataValue.setCellValue(targetPath.trim());

							} else if (dateMetadataFields.stream().anyMatch(metadata.getKey()::contains)) {
								String dateValue = mdvalue;

								parseDateValue(metadata, metadataValue, dateValue);
							}

							else {

								metadataValue.setCellValue(StringUtils.substring(mdvalue, 0, 32767));
							}

							/**
							 * Collect asset data for renditions creation.
							 */
							if (metadata.getKey().equalsIgnoreCase(MigratorConstants.CSV_IMPORTER_COLUMN_SRC_REL_PATH)) {
								assetFileName = metadata.getValue();
							} else if (metadata.getKey().equalsIgnoreCase(MigratorConstants.CSV_COLUMN_ASSET_ID)) {
								assetId = metadata.getValue();
							} else if (metadata.getKey().equalsIgnoreCase(MigratorConstants.COLUMN_FILE_NAME)) {
								assetFileNameOriginal = metadata.getValue();
							} else if (metadata.getKey().equalsIgnoreCase(MigratorConstants.EXCEL_COLUMN_DRIVE_PATH)) {
								assetFileName = metadata.getValue().trim();
							}

							if (migrationType != null && migrationType.equalsIgnoreCase(MigratorConstants.AGENCY)
									&& metadata.getKey().equalsIgnoreCase("path")) {
								assetRelativePath = metadata.getValue().trim();
							}

							/**
							 * Create new rows for rendition files.
							 */
							if (metadata.getKey().equalsIgnoreCase(MigratorConstants.AEM_PROPERTY_RENDITIONS)) {
								if (metadata.getValue() != null && !metadata.getValue().isEmpty()) {
									renditions = metadata.getValue().split(MigratorConstants.COLON);
								} else {
									renditions = null;
								}

							}

						}
					}

					if (headersMap.get(MigratorConstants.MIGRATION_BATCH) != null) {
						Cell migrationBatchCell = assetMDRow.createCell(headersMap
								.get(MigratorConstants.MIGRATION_BATCH));
						migrationBatchCell.setCellValue("Asset Migration");
					}

					/**
					 * Create new rows for rendition files.
					 */

					rownum = writeRenditionsToCSV(s3Client,brandPrefix, renditionsTargetPath, migrationSheet, headersMap,
							rownum, assetTargetPath, assetId, renditions, s3ReplicationFlag);

					/**
					 * Replicate AEM target path in S3 for S3 ingestor tool for
					 * real asset.
					 */
					if (s3ReplicationFlag.equalsIgnoreCase("true")) {
						if (migrationType != null && migrationType.equalsIgnoreCase(MigratorConstants.AGENCY)) {
							LOGGER.info("Agency : assetRelativePath :" + assetRelativePath + " | assetTargetPath: "
									+ assetTargetPath);
							replicateS3AsAEM(s3Client, brandPrefix, assetRelativePath, assetTargetPath);
						} else {
							LOGGER.info("NonAgency : assetFileName :" + assetFileName + " | assetTargetPath: "
									+ assetTargetPath);
							replicateS3AsAEM(s3Client, brandPrefix, assetFileName, assetTargetPath);
						}
					}
				}

				preparePathFolderCreatorFile(pathsMap, assetPathSheet);
				createAndUploadFile(s3Client, folderCreatorWorkbook, migrationFolderCreatorReportPath);
			}

			createAndUploadFile(s3Client, workbook, migrationCSVReportName);

			LOGGER.info("Migration completed succesfully!!!!!!! Congratulations!!!!!!!!");
			isSuccess = true;
		} catch (Exception e) {
			LOGGER.error("MigrationReportUtil writeToCSV Exception:{}", e);
			isSuccess = false;
		} finally {
			if (null != workbook) {
				try {
					workbook.close();
				} catch (IOException e) {
					LOGGER.error("MigrationReportUtil writeToCSV IOException:{}", e);
				}
			}
			if (null != folderCreatorWorkbook) {
				try {
					folderCreatorWorkbook.close();
				} catch (IOException e) {
					LOGGER.error("MigrationReportUtil writeToCSV IOException:{}", e);
				}
			}
		}
		return isSuccess;
	}

	public static void createAndUploadFile(AmazonS3 s3Client, XSSFWorkbook workbook, String migrationCSVReportName)
			throws FileNotFoundException, IOException, MigratorServiceException {
		// String configPath = getConfigPath();
		if (null != workbook) {
			// this Writes the workbook to local file.
			File file = File.createTempFile(migrationCSVReportName, "");
			FileOutputStream out = new FileOutputStream(file);
			workbook.write(out);
			out.close();
			LOGGER.info("asset_migration.xlsx written successfully on disk.");

			uploadFileToS3(s3Client, file, migrationCSVReportName);

		}
	}

	public static void preparePathFolderCreatorFile(Map<String, Map<String, String>> pathsMap, XSSFSheet assetPathSheet) {
		int rownum = 0;
		for (Entry<String, Map<String, String>> assetPath : pathsMap.entrySet()) {
			Row assetMDRow = assetPathSheet.createRow(rownum++);

			String path = assetPath.getKey();

			String[] oldNewPaths = path.split(":");

			if (oldNewPaths.length > 1) {
				path = oldNewPaths[0];
			}

			String[] folders = path.split("\\/");
			Map<String, String> folderMap = assetPath.getValue();
			if (folders.length > 0) {
				updateFolderCell(assetMDRow, folderMap, folders);
			}

		}

	}

	private static void updateFolderCell(Row assetMDRow, Map<String, String> folderMap, String[] folders) {
		int cellnum = 0;
		for (String folder : folders) {
			if (folder != null && !folder.isEmpty()) {
				String originalFolderName = folder.trim();

				if (folderMap.get(originalFolderName) != null) {
					String excelValuePerTemplate = originalFolderName + "{{" + folderMap.get(originalFolderName) + "}}";
					Cell folderCell = assetMDRow.createCell(cellnum++);
					folderCell.setCellValue(excelValuePerTemplate);
				}
			}
		}
	}

	public static String preparePathForFolderCreator(String path, Map<String, Map<String, String>> pathsMap) {
		Pattern pt = Pattern.compile("[^a-zA-Z0-9._/\\p{L}]");

		String fileName = path.substring(path.lastIndexOf('/') + 1, path.length()).trim();
		String updatedFileName = replaceSpecialCharacters(pt, fileName, MigratorConstants.FILE_NAME);

		path = path.substring(0, path.lastIndexOf('/'));
		String[] folders = path.split("\\/");
		Map<String, String> folderMap = new HashMap<>();

		StringBuilder mapKey = new StringBuilder(path);
		StringBuilder modifiedPath = new StringBuilder();

		if (folders.length > 0) {
			for (String folder : folders) {
				if (folder != null && !folder.isEmpty()) {
					String originalFolderName = folder.trim();

					String updatedFolderName = replaceSpecialCharacters(pt, originalFolderName,
							MigratorConstants.FOLDER);

					modifiedPath.append(updatedFolderName).append("/");

					folderMap.put(originalFolderName, updatedFolderName);
				}
			}
			if (modifiedPath.toString() != null && !modifiedPath.toString().isEmpty()) {
				mapKey.append(":").append(modifiedPath);
				if (!modifiedPath.toString().endsWith("/")) {
					path = modifiedPath.toString().concat("/").concat(updatedFileName);
				} else {
					path = modifiedPath.toString().concat(updatedFileName);
				}

			}

			pathsMap.put(mapKey.toString(), folderMap);
		}

		return path;
	}

	private static String replaceSpecialCharacters(Pattern pt, String originalFolderName, String type) {
		String updatedName = trimExtraSpaces(originalFolderName, type);
		Matcher match = pt.matcher(updatedName);
		String replaceChar = MigratorConstants.SPECIAL_CHARACTER_UNDERSCORE;

		if (type.equalsIgnoreCase(MigratorConstants.FILE_NAME)) {
			replaceChar = MigratorConstants.SPECIAL_CHARACTER_HYPHEN;
		}
		while (match.find()) {
			String s = match.group();
			updatedName = updatedName.replaceAll("\\" + s, replaceChar);
		}
		return updatedName;
	}

	private static void parseDateValue(Entry<String, String> metadata, Cell metadataValue, String dateValue) {
		try {

			dateValue = parseDateToAEMDate(dateValue);
			if (dateValue != null) {
				metadataValue.setCellValue(dateValue);
			}
		} catch (ParseException e) {
			LOGGER.error("MigrationReportUtil parseDateValue ParseException ::header: {} ----- columnValue :{}",
					metadata.getKey(), metadata.getValue());
		}
	}

	private static String parseDateToAEMDate(String dateString) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(MigratorConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS_SSSZ);
		String aemDate = null;
		for (String dateFormat : dateFormats) {

			SimpleDateFormat format = new SimpleDateFormat(dateFormat);
			try {
				if (dateString.trim().endsWith(":")) {
					dateString = dateString.trim().concat("00");
				}
				Date dat = format.parse(dateString);
				aemDate = formatter.format(dat);
				break;
			} catch (ParseException e) {
				LOGGER.error("MigrationReportUtil parseDateToAEMDate ParseException ::{} ", e.getMessage());
			}
		}
		return aemDate;
	}

	private static String trimExtraSpaces(String targetPath, String type) {
		String replaceChar = MigratorConstants.SPECIAL_CHARACTER_UNDERSCORE;

		if (type.equalsIgnoreCase(MigratorConstants.FILE_NAME)) {
			replaceChar = MigratorConstants.SPECIAL_CHARACTER_HYPHEN;
		}

		targetPath = targetPath.replace(" / ", "/");
		targetPath = targetPath.replace(" /", "/");
		targetPath = targetPath.replace("/ ", "/");
		targetPath = targetPath.replaceAll(" ", replaceChar);
		targetPath = targetPath.toLowerCase();
		return targetPath;
	}

	private static int writeRenditionsToCSV(AmazonS3 s3Client,StringBuilder brandPrefix, String renditionsTargetPath,
			XSSFSheet migrationSheet, Map<String, Integer> headersMap, int rownum, String assetTargetPath,
			String assetId, String[] renditions, String s3ReplicationFlag) {

		String contentDamPrefix = "/content/dam/";

		if (renditions != null && renditions.length > 0) {

			for (String rendition : renditions) {
				String renditionsTempPath = null;

				String[] renditionFileNames = rendition.split(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT);
				Row renditionMDRow = migrationSheet.createRow(rownum++);
				String fileNameOriginal = rendition;
				if (renditionFileNames.length > 1) {
					rendition = renditionFileNames[0];
					fileNameOriginal = renditionFileNames[1];
				}

				renditionsTempPath = assetTargetPath.substring(0, (assetTargetPath.lastIndexOf('/'))).concat("/")
						.concat(rendition);

				setRenditionsMetadataToCell(renditionsTargetPath, headersMap, assetTargetPath, assetId,
						contentDamPrefix, rendition, renditionMDRow, fileNameOriginal);

				/**
				 * Replicate AEM target path in S3 for S3 ingestor tool for
				 * rendition files.
				 */
				if (s3ReplicationFlag.equalsIgnoreCase("true")) {
					replicateS3AsAEM(s3Client, brandPrefix, rendition, renditionsTargetPath.concat(renditionsTempPath));
				}
			}
		}
		return rownum;
	}

	private static void setRenditionsMetadataToCell(String renditionsTargetPath, Map<String, Integer> headersMap,
			String assetTargetPath, String assetId, String contentDamPrefix, String rendition, Row renditionMDRow,
			String fileNameOriginal) {
		String renditionsTempPath;
		if (headersMap.get(MigratorConstants.ABS_TARGET_PATH) != null) {
			renditionsTempPath = assetTargetPath.substring(0, (assetTargetPath.lastIndexOf('/'))).concat("/")
					.concat(rendition);

			Cell targetPathCell = renditionMDRow.createCell(headersMap.get(MigratorConstants.ABS_TARGET_PATH));
			targetPathCell.setCellValue(contentDamPrefix.concat(renditionsTargetPath.concat(renditionsTempPath)));
		}
		if (headersMap.get(MigratorConstants.COLUMN_FILE_NAME) != null) {
			Cell targetPathCell = renditionMDRow.createCell(headersMap.get(MigratorConstants.COLUMN_FILE_NAME));
			targetPathCell.setCellType(CellType.STRING);
			targetPathCell.setCellValue(fileNameOriginal);
		}

		if (headersMap.get(MigratorConstants.CSV_IMPORTER_COLUMN_SRC_REL_PATH) != null) {
			Cell targetPathCell = renditionMDRow.createCell(headersMap
					.get(MigratorConstants.CSV_IMPORTER_COLUMN_SRC_REL_PATH));
			targetPathCell.setCellValue(rendition);
		}
		if (headersMap.get(MigratorConstants.CSV_COLUMN_ASSET_ID) != null) {
			Cell assetIdCell = renditionMDRow.createCell(headersMap.get(MigratorConstants.CSV_COLUMN_ASSET_ID));
			assetIdCell.setCellValue(assetId);
		}
		if (headersMap.get(MigratorConstants.RENDITIONS_TARGET_KEY) != null) {
			Cell assetIdCell = renditionMDRow.createCell(headersMap.get(MigratorConstants.RENDITIONS_TARGET_KEY));
			assetIdCell.setCellValue(contentDamPrefix.concat(assetTargetPath));
		}
		if (headersMap.get(MigratorConstants.DC_TITLE) != null) {
			Cell jcrTitleCell = renditionMDRow.createCell(headersMap.get(MigratorConstants.DC_TITLE));
			jcrTitleCell.setCellValue(fileNameOriginal);
		}
	}

	/**
	 * This method logs migration summary report into transformation log file.
	 * 
	 * @param brandPrefix
	 * @param migrationAssetsList
	 * @param finalHeadersSet
	 * @param nonMigratedAssetsMap
	 * @param migratedAssetsMap
	 */
	public static void createSummaryReport(AmazonS3 s3Client, StringBuilder brandPrefix, Map<String, String> migratedAssetsMap,
			Map<String, String> nonMigratedAssetsMap) {

		/**
		 * Read brand specific properties.
		 */
		Properties prop;
		XSSFWorkbook workbook = null;
		try {
			prop = MigrationUtils.getPropValues();
			String migrationSummaryReportName = prop.getProperty(brandPrefix + ""
					+ MigratorConstants.MIGRATION_SUMMARY_REPORT_PATH);
			// Blank workbook
			workbook = new XSSFWorkbook();

			// Create a blank sheet

			XSSFSheet summerySheet = workbook.createSheet("Migration_Summary");

			int rownum = 0;
			int cellnum = 0;
			Row headerRowSummarySheet = summerySheet.createRow(rownum++);
			Cell migrationCountColumn = headerRowSummarySheet.createCell(cellnum++);
			migrationCountColumn.setCellValue("Migrated Assets Count");

			Cell nonMigrationCountColumn = headerRowSummarySheet.createCell(cellnum++);
			nonMigrationCountColumn.setCellValue("Non Migrated Assets Count");

			cellnum = 0;

			Row rowSummarySheet = summerySheet.createRow(rownum++);
			Cell migrationCount = rowSummarySheet.createCell(cellnum++);
			migrationCount.setCellValue(migratedAssetsMap.size());

			Cell nonMigrationCount = rowSummarySheet.createCell(cellnum++);
			nonMigrationCount.setCellValue(nonMigratedAssetsMap.size());

			XSSFSheet sheet1 = workbook.createSheet("Migrated_Assets");
			rownum = 0;
			cellnum = 0;
			Row headerRowSheet1 = sheet1.createRow(rownum++);
			Cell headerColumn1 = headerRowSheet1.createCell(cellnum++);
			headerColumn1.setCellValue("Asset_Id");

			Cell headerColumn2 = headerRowSheet1.createCell(cellnum++);
			headerColumn2.setCellValue("Description");

			LOGGER.info("------------Migrated Assets------");
			LOGGER.info("ExcelFolderMDTransformation transform : Migrated Assets size:{}", migratedAssetsMap.size());
			for (Entry<String, String> migratedMap : migratedAssetsMap.entrySet()) {
				cellnum = 0;
				Row row = sheet1.createRow(rownum++);
				Cell column1 = row.createCell(cellnum++);
				column1.setCellValue((String) migratedMap.getKey());

				Cell column2 = row.createCell(cellnum++);
				column2.setCellValue((String) migratedMap.getValue());
			}

			XSSFSheet sheet2 = workbook.createSheet("Non_Migrated_Assets");

			rownum = 0;
			cellnum = 0;
			Row headerRowSheet2 = sheet2.createRow(rownum++);
			Cell headerColumn1Sheet2 = headerRowSheet2.createCell(cellnum++);
			headerColumn1Sheet2.setCellValue("Asset_Id");

			Cell headerColumn2Sheet2 = headerRowSheet2.createCell(cellnum++);
			headerColumn2Sheet2.setCellValue("Description");

			LOGGER.info("------------Non Migrated Assets------");
			LOGGER.info("ExcelFolderMDTransformation transform : Non migrated Assets size:{}",
					nonMigratedAssetsMap.size());
			for (Entry<String, String> nonMigratedMap : nonMigratedAssetsMap.entrySet()) {

				cellnum = 0;
				Row row = sheet2.createRow(rownum++);
				Cell column1 = row.createCell(cellnum++);
				column1.setCellValue((String) nonMigratedMap.getKey());

				Cell column2 = row.createCell(cellnum++);
				column2.setCellValue((String) nonMigratedMap.getValue());
			}

			// String configPath = getConfigPath();
			File file = File.createTempFile(migrationSummaryReportName, "");
			if (null != workbook) {
				// this Writes the workbook gfgcontribute
				FileOutputStream out = new FileOutputStream(file);
				workbook.write(out);
				out.close();
				LOGGER.info("migration_summary.xlsx written successfully on disk.");

				uploadFileToS3(s3Client, file, migrationSummaryReportName);
			}

		} catch (Exception e) {
			LOGGER.error("MigrationReportUtil logMigrationSummaryReport Exception:{}", e);
		} finally {
			if (null != workbook) {
				try {
					workbook.close();
				} catch (IOException e) {
					LOGGER.error("MigrationReportUtil logMigrationSummaryReport IOException:{}", e);
				}
			}
		}
	}

	public static void uploadFileToS3(AmazonS3 s3Client, File file, String migrationCSVReportName) throws MigratorServiceException {
		// String path = getConfigPath();

		Format formatter = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
		String dateString = formatter.format(new Date());

		String devMigrationBucketName = MigrationUtils.getPropValues().getProperty(
				"migrator.dev.asset.migration.bucket.name");
		String devMigrationReportPath = MigrationUtils.getPropValues().getProperty(
				"migrator.asset.migration.report.path");

		LOGGER.info("Uploading a new object to S3 from a file\n");
		if (file != null) {
			String fileName = migrationCSVReportName.split("\\.")[0];
			String extn = migrationCSVReportName.split("\\.")[1];

			PutObjectResult res = s3Client.putObject(new PutObjectRequest(devMigrationBucketName, devMigrationReportPath
					+ "/" + fileName.concat("." + dateString).concat("." + extn), file));
			LOGGER.info("PutObjectResult res:{}", res);
		}

	}

	// public static String getConfigPath() {
	// RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
	// List<String> arguments = runtimeMxBean.getInputArguments();
	//
	// for (String args : arguments) {
	// if (args.contains("DconfigDir")) {
	// String[] paths = args.split("=");
	// return paths[1];
	// }
	//
	// }
	// return null;
	//
	// }

	/**
	 * This method is to replicate asset path in s3 for S3 ingestor tool.
	 * 
	 * @param brandPrefix
	 * @param src
	 * @param dst
	 */
	public static void replicateS3AsAEM(AmazonS3 s3Client,StringBuilder brandPrefix, String src, String dst) {
		Properties prop = null;
		String srcBucket = null;
		String dstBucket = null;
		String mimeType = null;
		try {
			prop = MigrationUtils.getPropValues();
			srcBucket = prop.getProperty(brandPrefix + "" + MigratorConstants.S3_SOURCE_BUCKET_NAME);
			dstBucket = prop.getProperty(brandPrefix + "" + MigratorConstants.S3_DESTINATION_BUCKET_NAME);

			LOGGER.info("-------------S3 Replication Start----------------");
			LOGGER.info("MigrationUtils replicateS3AsAEM :srcBucket:{} - src:{}", srcBucket, src);
			LOGGER.info("MigrationUtils replicateS3AsAEM :dstBucket:{} - dst:{}", dstBucket, dst);

			String fileExtension = MigrationUtils.getFileExtension(src);

			if (!fileExtension.isEmpty() && BusinessRulesUtil.MimeTypeMap != null
					&& BusinessRulesUtil.MimeTypeMap.size() > 0
					&& BusinessRulesUtil.MimeTypeMap.containsKey(fileExtension.toLowerCase())) {
				mimeType = BusinessRulesUtil.MimeTypeMap.get(fileExtension.toLowerCase());
			}

			s3MultiPartUpload(srcBucket, dstBucket, src, dst, s3Client, mimeType);
		} catch (AmazonS3Exception ase) {
			LOGGER.error(
					"MigrationUtils : replicateS3AsAEM : S3 replication failed :AmazonS3Exception : {} : Src Key:{}",
					ase, src);
			src = trySecondAttemptToUpload(src, dst, srcBucket, dstBucket, s3Client, mimeType);
		} catch (AmazonServiceException ase) {
			LOGGER.error(
					"MigrationUtils : replicateS3AsAEM : S3 replication failed :AmazonServiceException : {}: Src Key:{}",
					ase, src);
		} catch (AmazonClientException ace) {
			LOGGER.error(
					"MigrationUtils : replicateS3AsAEM : S3 replication failed :AmazonClientException : {} : Src Key:{}",
					ace, src);
		} catch (MigratorServiceException e) {
			LOGGER.error("MigrationUtils : replicateS3AsAEM : S3 replication failed : {} : Src Key:{}", e, src);
		} catch (Exception e) {
			LOGGER.error("MigrationUtils : replicateS3AsAEM : S3 replication failed :Exception : {} : Src Key:{}", e,
					src);
		}
		LOGGER.info("-------------S3 Replication End----------------");
	}

	private static String trySecondAttemptToUpload(String src, String dst, String srcBucket, String dstBucket,
			AmazonS3 s3, String mimeType) {
		try {
			String[] filename = src.split("\\.");
			if (filename.length > 1) {
				if (isLowerCase(filename[1])) {
					src = src.toUpperCase();
				} else {
					src = src.toLowerCase();
				}
				s3MultiPartUpload(srcBucket, dstBucket, src, dst, s3, mimeType);
			}

		} catch (Exception e) {
			LOGGER.error(
					"MigrationUtils : replicateS3AsAEM : S3 replication failed :Exception(2nd Attempt) : {} : Src Key:{}",
					e, src);
		}
		return src;
	}

	private static boolean isLowerCase(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (Character.isAlphabetic(s.charAt(i)) && !Character.isLowerCase(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static File getFileFromS3(AmazonS3 s3Client, String fileName, String s3Folder) {

		File file = null;
		String devMigrationBucketName = "";
		try {

			devMigrationBucketName = MigrationUtils.getPropValues().getProperty(
					"migrator.dev.asset.migration.bucket.name");
			S3Object s3object = s3Client.getObject(new GetObjectRequest(devMigrationBucketName, s3Folder + "/" + fileName));
			try (InputStream inputStream = s3object.getObjectContent()) {
				file = File.createTempFile("s3test", "");
				try (FileOutputStream outputStream = new FileOutputStream(file)) {
					int read;
					byte[] bytes = new byte[1024];
					while ((read = inputStream.read(bytes)) != -1) {
						outputStream.write(bytes, 0, read);
					}
				}
			}

		} catch (MigratorServiceException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;

	}

	public static void updateLastProcessedAsset(String assetIdBeingProcessed) throws IOException {

		Properties prop;
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			prop = MigrationUtils.getPropValues();

			String migrationFailureFilePath = prop
					.getProperty(MigratorConstants.MIGRATION_INTERRUPT_LAST_PROCESSED_ASSET_FILE_PATH);
			File file = File.createTempFile(migrationFailureFilePath, "");
			fw = new FileWriter(file.getPath());
			bw = new BufferedWriter(fw);
			bw.write("Last Processed Asset Is :" + assetIdBeingProcessed);
		} catch (MigratorServiceException e) {
			LOGGER.error(
					"MigrationUtils : updateLastProcessedAsset : updateLastProcessedAsset failed :MigratorServiceException : {}",
					e);
		} finally {

			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch (IOException ex) {
				LOGGER.error(
						"MigrationUtils : updateLastProcessedAsset : updateLastProcessedAsset failed :IOException : {}",
						ex);

			}
		}

	}

	public static void s3MultiPartUpload(String sourceBucketName, String targetBucketName, String sourceObjectKey,
			String targetObjectKey, AmazonS3 s3Client, String mimeType) {
		List<CopyPartResult> copyResponses = new ArrayList<CopyPartResult>();

		ObjectMetadata metadata = new ObjectMetadata();
		if (mimeType != null) {
			metadata.setContentType(mimeType);
		}

		InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(targetBucketName,
				targetObjectKey, metadata);

		InitiateMultipartUploadResult initResult = s3Client.initiateMultipartUpload(initiateRequest);

		// Get object size.
		GetObjectMetadataRequest metadataRequest = new GetObjectMetadataRequest(sourceBucketName, sourceObjectKey);

		ObjectMetadata metadataResult = s3Client.getObjectMetadata(metadataRequest);
		long objectSize = metadataResult.getContentLength(); // in bytes

		// Copy parts.
		long partSize = 4096 * (long) Math.pow(2.0, 20.0); // 4 GB

		long bytePosition = 0;

		LOGGER.info("MigrationUtils replicateS3AsAEM s3MultiPartUpload :src : {}  -  objectSize:{}", sourceObjectKey,
				objectSize);
		for (int i = 1; bytePosition < objectSize; i++) {
			CopyPartRequest copyRequest = new CopyPartRequest()
					.withDestinationBucketName(targetBucketName)
					.withDestinationKey(targetObjectKey)
					.withSourceBucketName(sourceBucketName)
					.withSourceKey(sourceObjectKey)
					.withUploadId(initResult.getUploadId())
					.withFirstByte(bytePosition)
					.withLastByte(
							bytePosition + partSize - 1 >= objectSize ? objectSize - 1 : bytePosition + partSize - 1)
					.withPartNumber(i);

			copyResponses.add(s3Client.copyPart(copyRequest));
			bytePosition += partSize;

		}
		CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(targetBucketName,
				targetObjectKey, initResult.getUploadId(), GetETags(copyResponses));
		CompleteMultipartUploadResult completeUploadResponse = s3Client.completeMultipartUpload(completeRequest);
		LOGGER.info("MigrationUtils replicateS3AsAEM s3MultiPartUpload :CopyObjectResult:{}", completeUploadResponse);
	}

	// Helper function that constructs ETags.
	private static List<PartETag> GetETags(List<CopyPartResult> responses) {
		List<PartETag> etags = new ArrayList<PartETag>();
		for (CopyPartResult response : responses) {
			etags.add(new PartETag(response.getPartNumber(), response.getETag()));
		}
		return etags;
	}

	private static boolean containsHanScript(String s) {
		return s.codePoints().anyMatch(
				codepoint -> Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN);
	}

	/**
	 * This method logs migration summary report into transformation log file.
	 * 
	 * @param brandPrefix
	 * @param migrationAssetsList
	 * @param finalHeadersSet
	 * @param nonMigratedAssetsMap
	 * @param migratedAssetsMap
	 */
	public static void logMigrationSummaryReport(AmazonS3 s3Client,StringBuilder brandPrefix, Map<String, String> migratedAssetsMap,
			Map<String, String> nonMigratedAssetsMap) {

		/**
		 * Read brand specific properties.
		 */
		Properties prop;
		XSSFWorkbook workbook = null;
		try {
			prop = MigrationUtils.getPropValues();

			String migrationSummaryReportName = prop.getProperty(brandPrefix + ""
					+ MigratorConstants.MIGRATION_SUMMARY_REPORT_PATH);
			// Blank workbook
			workbook = new XSSFWorkbook();

			// Create a blank sheet

			XSSFSheet summerySheet = workbook.createSheet("Migration_Summary");

			int rownum = 0;
			int cellnum = 0;
			Row headerRowSummarySheet = summerySheet.createRow(rownum++);
			Cell migrationCountColumn = headerRowSummarySheet.createCell(cellnum++);
			migrationCountColumn.setCellValue("Migrated Assets Count");

			Cell nonMigrationCountColumn = headerRowSummarySheet.createCell(cellnum++);
			nonMigrationCountColumn.setCellValue("Non Migrated Assets Count");

			cellnum = 0;

			Row rowSummarySheet = summerySheet.createRow(rownum++);
			Cell migrationCount = rowSummarySheet.createCell(cellnum++);
			migrationCount.setCellValue(migratedAssetsMap.size());

			Cell nonMigrationCount = rowSummarySheet.createCell(cellnum++);
			nonMigrationCount.setCellValue(nonMigratedAssetsMap.size());

			XSSFSheet sheet1 = workbook.createSheet("Migrated_Assets");
			rownum = 0;
			cellnum = 0;
			Row headerRowSheet1 = sheet1.createRow(rownum++);
			Cell headerColumn1 = headerRowSheet1.createCell(cellnum++);
			headerColumn1.setCellValue("Asset_Id");

			Cell headerColumn2 = headerRowSheet1.createCell(cellnum++);
			headerColumn2.setCellValue("Description");

			LOGGER.info("------------Migrated Assets------");
			LOGGER.info("DriveBasedTransform : Migrated Assets size:{}", migratedAssetsMap.size());
			for (Entry<String, String> migratedMap : migratedAssetsMap.entrySet()) {
				cellnum = 0;
				Row row = sheet1.createRow(rownum++);
				Cell column1 = row.createCell(cellnum++);
				column1.setCellValue((String) migratedMap.getKey());

				Cell column2 = row.createCell(cellnum++);
				column2.setCellValue((String) migratedMap.getValue());
			}

			XSSFSheet sheet2 = workbook.createSheet("Non_Migrated_Assets");

			rownum = 0;
			cellnum = 0;
			Row headerRowSheet2 = sheet2.createRow(rownum++);
			Cell headerColumn1Sheet2 = headerRowSheet2.createCell(cellnum++);
			headerColumn1Sheet2.setCellValue("Asset_Id");

			Cell headerColumn2Sheet2 = headerRowSheet2.createCell(cellnum++);
			headerColumn2Sheet2.setCellValue("Description");

			LOGGER.info("------------Non Migrated Assets------");
			LOGGER.info("DriveBasedTransform transform : Non migrated Assets size:{}", nonMigratedAssetsMap.size());
			for (Entry<String, String> nonMigratedMap : nonMigratedAssetsMap.entrySet()) {

				cellnum = 0;
				Row row = sheet2.createRow(rownum++);
				Cell column1 = row.createCell(cellnum++);
				column1.setCellValue((String) nonMigratedMap.getKey());

				Cell column2 = row.createCell(cellnum++);
				column2.setCellValue((String) nonMigratedMap.getValue());
			}

			// String configPath = getConfigPath();
			File file = File.createTempFile(migrationSummaryReportName, "");
			if (null != workbook) {
				// this Writes the workbook gfgcontribute
				FileOutputStream out = new FileOutputStream(file);
				workbook.write(out);
				out.close();
				LOGGER.info("migration_summary.xlsx written successfully on disk.");

				uploadFileToS3(s3Client, file, migrationSummaryReportName);
			}

		} catch (Exception e) {
			LOGGER.error("MigrationReportUtil logMigrationSummaryReport Exception:{}", e);
		} finally {
			if (null != workbook) {
				try {
					workbook.close();
				} catch (IOException e) {
					LOGGER.error("MigrationReportUtil logMigrationSummaryReport IOException:{}", e);
				}
			}
		}
	}
}
