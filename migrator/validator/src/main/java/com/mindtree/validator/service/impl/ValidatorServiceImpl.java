package com.mindtree.validator.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.amazonaws.services.s3.AmazonS3;
import com.mindtree.core.service.AppContext;
import com.mindtree.core.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.AmazonSESUtil;
import com.mindtree.utils.helper.ReadExcel;
import com.mindtree.validator.http.RestHttpClient;
import com.mindtree.validator.model.AemQueryBuilderResponse;
import com.mindtree.validator.model.AemRequest;
import com.mindtree.validator.model.Asset;
import com.mindtree.validator.model.Derivative;
import com.mindtree.validator.service.IValidatorService;

/**
 * @author M1032046
 *
 */
public class ValidatorServiceImpl implements IValidatorService {

	private final SimpleDateFormat sdfFinal = new SimpleDateFormat(
			MigratorConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS_SSSZ);

	private final SimpleDateFormat sdfAem = new SimpleDateFormat(MigratorConstants.DATE_FORMAT_EEE_MMM_DD_YYYY_HH_MM_SS);

	private final SimpleDateFormat sdfFile = new SimpleDateFormat(MigratorConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);

	private final SimpleDateFormat sdfDate = new SimpleDateFormat(MigratorConstants.DATE_FORMAT_YYYY_MM_DD);

	private static final Logger MISSING_ASSET_LOG = LoggerFactory.getLogger("validation-missing-files");

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidatorServiceImpl.class);

	private static final Logger FAILURE_LOG = LoggerFactory.getLogger("validation-error");

	private static Map<String, Map<String, String>> assetRendMap = new HashMap<>();

	private static Map<String, Map<String, String>> migAssetMap = new HashMap<>();

	private static Map<String, Integer> headersMap = new HashMap<>();

	private int counter = 0;

	boolean status = false;

//	ValidationConfigProperties validationConfig;
	RestHttpClient httpClient;
	Properties prop = null;

	@Override
	public AemQueryBuilderResponse getAllAEMAssets(long offSet) throws MigratorServiceException {

		prop = AppContext.getAppConfig();
		httpClient = new RestHttpClient();
		ResponseEntity<AemQueryBuilderResponse> aemResponse = null;
		AemRequest aemRequestBody = this.buildRequestBodyForAsset(offSet);
		aemResponse = httpClient.post(prop.getProperty("migrator.validation.hostname")
				+ MigratorConstants.GET_AEM_ASSET_RESOURCE_URL, aemRequestBody);

		return aemResponse.getBody();
	}

	private AemRequest buildRequestBodyForAsset(long offSet) {
		AemRequest aemRequest = new AemRequest();
		aemRequest.setDateLowerBound(prop.getProperty("migrator.validation.daterange.lowerBound"));
		aemRequest.setDateUpperBound(prop.getProperty("migrator.validation.daterange.upperBound"));
		aemRequest.setOffset(offSet);
		aemRequest.setLimit(Long.parseLong(prop.getProperty("migrator.validation.limits")));
		aemRequest.setPaths(Arrays.asList(prop.getProperty("migrator.validation.searchPaths").split(",")));
		aemRequest.setType(MigratorConstants.ASSET);
		return aemRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elc.validator.service.ValidatorService#validateUploadsToAEM()
	 */
	public final void validateUploadsToAEM(String sourceAbbreviation) {
		try {
			// Call AEM to get all uploaded assets for given date.
			AemQueryBuilderResponse allAEMAssets = null;

			this.getAllAssetsFromExcel(prop.getProperty("migrator.report.fileName"),
					prop.getProperty("migrator.report.sheetName"), sourceAbbreviation);
			LOGGER.info("Validator: {excel: Asset size {},  Rendition size {} }", migAssetMap.size(),
					assetRendMap.size());
			int offSet = 0;
			do {
				allAEMAssets = this.getAllAEMAssets(offSet);
				LOGGER.info("Validator: { aemAssetSize {} }", allAEMAssets.getAssetList().size());
				for (Asset aemRespAsset : allAEMAssets.getAssetList()) {
					processAssetValidation(aemRespAsset, sourceAbbreviation);
				}
				offSet += Integer.parseInt(prop.getProperty("migrator.validation.limits"));
				LOGGER.info("Validator:  OFFSET: Validation Completed OFFSET : {}", offSet);
			}

			while (allAEMAssets.isMore());
			logMigrationSummaryReport(sourceAbbreviation);
		} catch (MigratorServiceException e) {
			LOGGER.error("Validator: {MigratorServiceException }", e);
		}
	}

	/**
	 * @param assetRendMap
	 * @param aemRenditionList
	 * @param migAssetMap
	 * @param aemRespAsset
	 * @param sourceAbbreviation
	 * @throws IOException
	 */
	private void processAssetValidation(Asset aemRespAsset, String sourceAbbreviation) {
		String assetId = null;
		if (prop.getProperty("migrator.xmp.validation.flag").equalsIgnoreCase("true")) {

			if (sourceAbbreviation.equals(MigratorConstants.GLOBAL_EDIT)) {
				if (aemRespAsset.getMetadata().getAdditionalProperties().get(MigratorConstants.MIGRATOR_ASSET_ID) != null) {
					assetId = aemRespAsset.getMetadata().getAdditionalProperties().get(MigratorConstants.MIGRATOR_ASSET_ID)
							.toString();
				} else {
					assetId = aemRespAsset.getPath();
				}
			} else {
				assetId = aemRespAsset.getPath();
				LOGGER.info("Validator: { processAssetValidation: Validation Completed Count : {} }", counter++);
			}
		}
		processValidation(aemRespAsset, assetId);
	}

	private void processValidation(Asset aemRespAsset, String assetId) {
		if (null == assetId) {
			MISSING_ASSET_LOG.debug("Validator: {AEM assetId : {} is not Valid Asset Id, Path: {} }", assetId,
					aemRespAsset.getPath());
		} else {
			LOGGER.info("Validator: { processAssetValidation: Validation Started assetId : {} }", assetId);
			Map<String, String> metadataMap = migAssetMap.get(assetId);

			if (metadataMap != null) {
				this.validateAssets(aemRespAsset, metadataMap, assetId);
				migAssetMap.put(assetId, metadataMap);
			} else {
				metadataMap = migAssetMap.get(MigratorConstants.VAL_MIS_FILES_IN_EXCEL);
				if (metadataMap == null) {
					metadataMap = new HashMap<>();
				}
				Map<String, String> assetRend = assetRendMap.get(assetId);
				List<String> aemRenditionList = getAemDerivatives(aemRespAsset.getDerivatives());
				this.validateDerivatives(metadataMap, assetRend, aemRenditionList, false);

				metadataMap.put(assetId, aemRespAsset.getPath());
				migAssetMap.put(MigratorConstants.VAL_MIS_FILES_IN_EXCEL, metadataMap);
				MISSING_ASSET_LOG.debug("Validator: {AEM asset : {} missing in Excel}", aemRespAsset.getPath());
			}
			LOGGER.info("Validator: { processAssetValidation: Validation Completed assetId : {} }", assetId);
		}
	}

	private void getAllAssetsFromExcel(String fileName, String sheetName, String sourceAbbreviation) {

		Map<String, String> metadataMap = null;

		XSSFSheet assetSheet = null;
		try {
			String s3Folder = AppContext.getAppVariables().configFolder;
			assetSheet = ReadExcel.getExcelSheet(fileName, sheetName, s3Folder);
			LOGGER.info("Validator: { getAllAssetsFromExcel : fileName - {}, sheetName - {}, s3Folder - {}", fileName,
					sheetName, s3Folder);
			if (null == assetSheet) {
				LOGGER.info("Validator: { getAllAssetsFromExcel : assetSheet read is null. Please verify fileName/SheetName");
			} else {
				Iterator<Row> rows = assetSheet.iterator();

				while (rows.hasNext()) {
					Row currentRow = rows.next();
					if (currentRow.getRowNum() == 0) {
						createHeaderMap(currentRow, sourceAbbreviation);
					} else {
						metadataMap = createMetaDataMap(currentRow, sourceAbbreviation);
						updateMigAssetMap(metadataMap, sourceAbbreviation);
					}

				}
			}
		} catch (MigratorServiceException e) {
			LOGGER.error("Validator: { getAllAssetsFromExcel : MigratorServiceException }", e);
		}
	}

	/**
	 * @param metadataMap
	 * @param sourceAbbreviation
	 */
	private void updateMigAssetMap(Map<String, String> metadataMap, String sourceAbbreviation) {
		if (null != metadataMap && !metadataMap.isEmpty()) {
			if (prop.getProperty("migrator.xmp.validation.flag").equalsIgnoreCase("true")) {
				if (sourceAbbreviation.equals(MigratorConstants.GLOBAL_EDIT))
					migAssetMap.put(metadataMap.get(MigratorConstants.MIGRATOR_ASSET_ID), metadataMap);
				else
					migAssetMap.put(metadataMap.get(MigratorConstants.ABS_TARGET_PATH), metadataMap);

			} else {
				migAssetMap.put(metadataMap.get(MigratorConstants.ABS_TARGET_PATH), metadataMap);
			}
		}
	}

	/**
	 * @param row
	 * @param sourceAbbreviation
	 * @return
	 */
	private void createHeaderMap(Row row, String sourceAbbreviation) {
		Iterator<Cell> cellIterator = row.cellIterator();
		headersMap.put(MigratorConstants.VALIDATION_STATUS, MigratorConstants.ZERO);
		headersMap.put(MigratorConstants.VALIDATION_MSG, MigratorConstants.ONE);
		int headerIndex = 2;
		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();
			headersMap.put(cell.getStringCellValue(), cell.getColumnIndex() + MigratorConstants.TWO);
			headerIndex++;
		}
		headersMap.put(MigratorConstants.RENDITION_MSG, headerIndex);
		headerIndex++;
		if (sourceAbbreviation.equals(MigratorConstants.ISILON_SERVER)
				&& !headersMap.containsKey(MigratorConstants.RENDITIONS_TARGET_KEY_STR))
			headersMap.put(MigratorConstants.RENDITIONS_TARGET_KEY_STR, headerIndex);
	}

	/**
	 * @param assetRendMap
	 * @param row
	 * @param sourceAbbreviation
	 * @return
	 */
	private Map<String, String> createMetaDataMap(Row row, String sourceAbbreviation) {
		Map<String, String> metadataMap = null;
		Map<String, String> rendMap = null;

		if (null == row.getCell(headersMap.get(MigratorConstants.RENDITIONS_TARGET_KEY_STR) - MigratorConstants.TWO)) {
			metadataMap = new HashMap<>();
			processExcellForNullCellVal(row, metadataMap);

		} else {
			String assetId = null;
			if (sourceAbbreviation.equals(MigratorConstants.GLOBAL_EDIT)) {
				assetId = new DataFormatter().formatCellValue(row.getCell(headersMap
						.get(MigratorConstants.MIGRATOR_ASSET_ID) - MigratorConstants.TWO));
			} else {
				assetId = new DataFormatter().formatCellValue(row.getCell(headersMap
						.get(MigratorConstants.ABS_TARGET_PATH)));
			}

			if (null == assetRendMap.get(assetId)) {
				rendMap = new HashMap<>();
			} else {
				rendMap = assetRendMap.get(assetId);
			}
			StringBuilder sb = new StringBuilder(row.getCell(
					headersMap.get(MigratorConstants.ABS_TARGET_PATH) - MigratorConstants.TWO).toString());
			rendMap.put(sb.toString(), MigratorConstants.DERIVATIVE_NA);
			assetRendMap.put(assetId, rendMap);
		}
		return metadataMap;
	}

	/**
	 * @param row
	 * @param metadataMap
	 */
	private void processExcellForNullCellVal(Row row, Map<String, String> metadataMap) {
		for (Entry<String, Integer> headerVal : headersMap.entrySet()) {
			if (headerVal.getValue() == MigratorConstants.ZERO) {
				metadataMap.put(headerVal.getKey(), MigratorConstants.ASSET_NA);
			} else if (headerVal.getValue() == MigratorConstants.ONE) {
				metadataMap.put(headerVal.getKey(), "");
			} else {
				if (null == row.getCell(headerVal.getValue() - MigratorConstants.TWO)) {
					metadataMap.put(headerVal.getKey(), "");
				} else {
					metadataMap.put(headerVal.getKey(), getCellValue(row, headerVal));
				}
			}
		}
	}

	private String getCellValue(Row row, Entry<String, Integer> headerVal) {
		return new DataFormatter().formatCellValue(row.getCell(headerVal.getValue() - MigratorConstants.TWO));
	}

	/**
	 * @param aemResponseMetadata
	 * @param metadataMap
	 * @param renditionList
	 * @param assetRendList
	 * @return
	 * @throws IOException
	 * @throws ElcDaoException
	 */
	private void validateAssets(Asset aemResponseMetadata, Map<String, String> metadataMap, String assetId) {

		Map<String, Object> aemMetadata = aemResponseMetadata.getMetadata().getAdditionalProperties();
		List<String> aemRenditionList = getAemDerivatives(aemResponseMetadata.getDerivatives());
		List<String> aemRelatedList = getAemRelated(aemResponseMetadata.getRelated());
		Map<String, String> assetRend = assetRendMap.get(assetId);
		boolean flag = this.validateMetadata(metadataMap, aemMetadata);
		flag = this.validateDerivatives(metadataMap, assetRend, aemRenditionList, flag);
		String relatedStatus = aemRelatedList.toString();
		this.validateRelated(relatedStatus, metadataMap);
		if (!flag) {
			metadataMap.put(MigratorConstants.VALIDATION_STATUS, MigratorConstants.ASSET_A_METADATA_NA);
			if (FAILURE_LOG.isDebugEnabled()) {
				FAILURE_LOG
						.debug("Asset upload is validated but metadata validation failed : ASSET ID {}, AEM Metadata {}, Excel Metadata, {}",
								metadataMap.get(MigratorConstants.CSV_COLUMN_ASSET_ID), aemMetadata, metadataMap);
			}
		}
	}

	private boolean validateRelated(String relatedStatus, Map<String, String> metadataMap) {
		metadataMap.put(MigratorConstants.AEM_RELATED, relatedStatus);
		return true;
	}

	private List<String> getAemRelated(List<String> related) {
		List<String> aemRelatedList = new ArrayList<>();
		for (String relateds : related) {
			aemRelatedList.add(relateds);
		}
		return aemRelatedList;
	}

	/**
	 * @param derivatives
	 * @return
	 */
	private List<String> getAemDerivatives(List<Derivative> derivatives) {
		List<String> aemRenditionList = new ArrayList<>();
		for (Derivative derivative : derivatives) {
			aemRenditionList.add(derivative.getName());
			LOGGER.info("Derivative is:" + derivative.getName());
		}
		return aemRenditionList;
	}

	/**
	 * @param metadataMap
	 * @param metadata
	 * @return
	 */
	private boolean validateMetadata(Map<String, String> metadataMap, Map<String, Object> aemMetadata) {
		boolean flag = true;
		StringBuilder unMatchedVal = new StringBuilder();
		int count = 0;
		for (Entry<String, String> metadata : metadataMap.entrySet()) {
			String key = getKey(metadata);
			if (metadata.getValue() != null
					&& !metadata.getValue().trim().isEmpty()
					&& !(key.equalsIgnoreCase(MigratorConstants.ABS_TARGET_PATH)
							|| key.equalsIgnoreCase(MigratorConstants.VALIDATION_STATUS) || key
								.equalsIgnoreCase(MigratorConstants.CSV_IMPORTER_COLUMN_SRC_REL_PATH))) {
				if (aemMetadata.get(key) == null) {
					flag = false;
					unMatchedVal.append(key).append(MigratorConstants.SPECIAL_CHARACTER_HASH)
							.append(MigratorConstants.UNAVAILABLE_IN_AEM)
							.append(MigratorConstants.SPECIAL_CHARACTER_DOLLER);
				} else if (isMetadataValueNotEqual(metadata.getValue(), aemMetadata.get(key), key)) {
					flag = false;
					Object val = aemMetadata.get(key);
					unMatchedVal.append(key).append(MigratorConstants.SPECIAL_CHARACTER_HASH)
							.append(getStrVal(metadata.getValue(), val))
							.append(MigratorConstants.SPECIAL_CHARACTER_UNDERSCORE).append(getStringValue(val))
							.append(MigratorConstants.SPECIAL_CHARACTER_DOLLER);
				} else {
					count++;
				}
			}
		}
		if (flag) {
			metadataMap.put(MigratorConstants.VALIDATION_STATUS, MigratorConstants.ASSET_A_METADATA_A_DERIV_NA);
		} else if (count > 0) {
			metadataMap.put(MigratorConstants.VALIDATION_STATUS, MigratorConstants.ASSET_A_METADATA_PA);
		}
		metadataMap.put(MigratorConstants.VALIDATION_MSG, unMatchedVal.toString());
		LOGGER.info("Validator: { validateMetadata: Matedata Validation completed.}");
		return flag;
	}

	/**
	 * @param value
	 * @param object
	 * @param key
	 * @return
	 */
	private boolean isMetadataValueNotEqual(String metadata, Object aemMetadata, String key) {
		boolean flag = true;
		if (key.equalsIgnoreCase(MigratorConstants.JCR_CREATED)
				|| key.equalsIgnoreCase(MigratorConstants.XMP_CREATE_DATE)
				|| key.equalsIgnoreCase(MigratorConstants.XMP_METADATADATE)
				|| key.equalsIgnoreCase(MigratorConstants.XMP_MODIFYDATE)
				|| key.equalsIgnoreCase(MigratorConstants.MIGRATOR_EXPIRATION_DATE)
				|| key.equalsIgnoreCase(MigratorConstants.PHOTOSHOP_DATECREATED)
				|| key.equalsIgnoreCase(MigratorConstants.EXIF_DATETIME_ORIGINAL)) {

			LOGGER.info("Validator: {metadata Name {}, befor parsing values : aem date {}, excel date {} }", key,
					aemMetadata, metadata);
			String aemDate = this.getStringDate(sdfAem, aemMetadata.toString());
			String mDate = this.getStringDate(sdfFinal, metadata);
			if (mDate == null) {
				mDate = getStringDate(sdfAem, metadata);
			}
			if (aemDate != null && aemDate.equals(mDate)) {
				flag = false;
			}
			LOGGER.info("Validator: {flag {}, parsed date values : aem date {}, excel date {} }", flag, aemDate, mDate);
		} else {
			if (getStrVal(metadata, aemMetadata).equals(getStringValue(aemMetadata))) {
				flag = false;
			}
		}
		return flag;
	}

	private String getStringDate(SimpleDateFormat sdf, String date) {
		String finalDate = null;
		try {
			if (prop.getProperty("migrator.xmp.validation.flag").equalsIgnoreCase("true")) {
				finalDate = sdfFinal.format(sdf.parse(date));
			} else {
				finalDate = sdfDate.format(sdf.parse(date));
			}
		} catch (ParseException e) {
			LOGGER.error("Validator: {getStringDate - Error while parsing date. date {}, {} }", date, e);
		}
		return finalDate;
	}

	/**
	 * Returns value After removing special character at beginning.
	 * 
	 * @param metadata
	 * @param aemMetadata
	 * @return
	 */
	private String getStrVal(String metadata, Object aemVal) {
		if (!(aemVal instanceof String || aemVal instanceof ArrayList)
				&& metadata.length() > MigratorConstants.ONE
				&& (metadata.startsWith(MigratorConstants.PLUS_SIGN) || metadata.startsWith(MigratorConstants.ZERO_STR))) {
			return metadata.substring(MigratorConstants.ONE).trim();
		}
		return metadata.trim();
	}

	/**
	 * @param aemVal
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String getStringValue(Object aemVal) {
		if (aemVal instanceof ArrayList) {
			return ((ArrayList) aemVal).stream().map(Object::toString)
					.collect(Collectors.joining(MigratorConstants.SPECIAL_CHARACTER_PIPE)).toString();
		} else {
			return aemVal.toString();
		}
	}

	/**
	 * @param metadata
	 * @return
	 */
	private String getKey(Entry<String, String> metadata) {
		String key;
		if (metadata.getKey().contains(MigratorConstants.SPECIAL_CHARACTER_CURLY_BRACES)) {
			key = metadata.getKey().substring(0,
					metadata.getKey().indexOf(MigratorConstants.SPECIAL_CHARACTER_CURLY_BRACES));
		} else {
			key = metadata.getKey();
		}
		return key.trim();
	}

	/**
	 * @param metadataMap
	 * @param assetRendMap
	 * @param aemRenditionList
	 * @param flag
	 * @return
	 */
	private boolean validateDerivatives(Map<String, String> metadataMap, Map<String, String> assetRendMap,
			List<String> aemRenditionList, boolean flag) {
		int count = 0;
		boolean rendFlag = false;
		if (flag && (null == assetRendMap || assetRendMap.size() == MigratorConstants.ZERO)) {
			metadataMap.put(MigratorConstants.VALIDATION_STATUS, MigratorConstants.SUCCESS_ASSET_A_METADATA_A);
			statusOfRendition(aemRenditionList, metadataMap);
			rendFlag = true;
		} else if (!flag && (null == assetRendMap || assetRendMap.size() == MigratorConstants.ZERO)) {
			statusOfRendition(aemRenditionList, metadataMap);
			rendFlag = true;
		} else {

			count += validateAemDerivatives(assetRendMap, aemRenditionList);

			rendFlag = checkValidationStatus(metadataMap, flag, count, rendFlag);

			if (status) {
				metadataMap.put(MigratorConstants.RENDITION_MSG, MigratorConstants.RENDITION_A);
			} else {
				metadataMap.put(MigratorConstants.RENDITION_MSG, MigratorConstants.RENDITION_NA);
			}
		}
		LOGGER.info("Validator: { validateDerivatives: Derivative Validation completed.}");
		return rendFlag;
	}

	private boolean checkValidationStatus(Map<String, String> metadataMap, boolean flag, int count, boolean rendFlag) {
		if (flag && count == 0) {
			metadataMap.put(MigratorConstants.VALIDATION_STATUS, MigratorConstants.SUCCESS_ASSET_A_METADATA_A_DERIV_A);
			rendFlag = true;
		} else if (flag && count > MigratorConstants.ZERO) {
			metadataMap.put(MigratorConstants.VALIDATION_STATUS, MigratorConstants.ASSET_A_METADATA_A_DERIV_NA);
			rendFlag = true;
		} else if (!flag && count == 0) {
			metadataMap.put(MigratorConstants.VALIDATION_STATUS, MigratorConstants.ASSET_A_METADATA_NA_DERIV_A);
			rendFlag = true;
		}
		return rendFlag;
	}

	private void statusOfRendition(List<String> aemRenditionList, Map<String, String> metadataMap) {
		if (!(aemRenditionList.isEmpty())) {
			metadataMap.put(MigratorConstants.RENDITION_MSG, MigratorConstants.RENDITION_A);
		} else {
			metadataMap.put(MigratorConstants.RENDITION_MSG, MigratorConstants.RENDITION_NA);
		}
	}

	/**
	 * @param
	 * @param aemRenditionList
	 * @param count
	 * @return
	 */
	private int validateAemDerivatives(Map<String, String> assetRendMap, List<String> aemRenditionList) {
		int count = 0;
		String rem = null;
		for (Entry<String, String> assetRend : assetRendMap.entrySet()) {
			String rend = assetRend.getKey().substring(
					assetRend.getKey().lastIndexOf(MigratorConstants.SPECIAL_CHARACTER_SLASH) + MigratorConstants.ONE);
			rem = rend;
			if (aemRenditionList.contains(rend)) {
				assetRend.setValue(MigratorConstants.DERIVATIVE_A);
				aemRenditionList.remove(rem);
			} else {
				assetRend.setValue(MigratorConstants.DERIVATIVE_NA);
				count = 1;
			}
			if (!(aemRenditionList.isEmpty())) {
				status = true;
			} else {
				status = false;
			}
		}

		return count;
	}

	/**
	 * @param sourceAbbreviation
	 * 
	 */
	private void logMigrationSummaryReport(String sourceAbbreviation) {

		// Blank workbook
		LOGGER.info("Validator: { logMigrationSummaryReport: Logging Report Started. }");
		int mRowNum = 1;
		int nmRowNum = 1;
		int migAssetCount = 0;
		int nonMigAssetCount = 0;
		StringBuilder file = new StringBuilder(prop.getProperty("migrator.report.dest.path"));
		file.append(MigratorConstants.ASSET_MIGRATION_SUMMARY).append(sdfFile.format(new Date()))
				.append(MigratorConstants.XLSX);
		try (XSSFWorkbook workbook = new XSSFWorkbook();
				FileOutputStream out = new FileOutputStream(new File(file.toString()))) {
			XSSFSheet sheet1 = createMigartionReport(workbook, MigratorConstants.MIGRATED_ASSETS, sourceAbbreviation);
			XSSFSheet sheet2 = createMigartionReport(workbook, MigratorConstants.NON_MIGRATED_ASSETS,
					sourceAbbreviation);

			for (Entry<String, Map<String, String>> migMap : migAssetMap.entrySet()) {

				if (migMap.getKey().equals(MigratorConstants.VAL_MIS_FILES_IN_EXCEL)) {
					logMissingFIle(workbook, migMap.getValue());
				} else {
					Map<String, String> metaData = migMap.getValue();
					if (metaData.get(MigratorConstants.VALIDATION_STATUS).equals(MigratorConstants.ASSET_A_METADATA_NA)
							|| metaData.get(MigratorConstants.VALIDATION_STATUS).equals(
									MigratorConstants.ASSET_A_METADATA_PA)
							|| metaData.get(MigratorConstants.VALIDATION_STATUS).equals(
									MigratorConstants.ASSET_A_METADATA_A_DERIV_NA)
							|| metaData.get(MigratorConstants.VALIDATION_STATUS).equals(
									MigratorConstants.ASSET_A_METADATA_NA_DERIV_A)
							|| metaData.get(MigratorConstants.VALIDATION_STATUS).equals(
									MigratorConstants.SUCCESS_ASSET_A_METADATA_A_DERIV_A)
							|| metaData.get(MigratorConstants.VALIDATION_STATUS).equals(
									MigratorConstants.SUCCESS_ASSET_A_METADATA_A)) {
						updateMigrationReport(sheet1, metaData, mRowNum++, sourceAbbreviation);
						migAssetCount++;

					} else {
						updateMigrationReport(sheet2, metaData, nmRowNum++, sourceAbbreviation);
						nonMigAssetCount++;
					}
				}
			}

			LOGGER.info("Validator: { logMigrationSummaryReport: Migration Report Created. }");

			createRenditionSummaryReport(workbook);
			createMigrationSummaryReport(workbook, migAssetCount, nonMigAssetCount);

			LOGGER.info("Validator: { Migrated Assets count: {} - Non Migrated Assets count: {} }", migAssetCount,
					nonMigAssetCount);

			// this Writes the workbook gfgcontribute
			workbook.write(out);
			LOGGER.info("Validator: {Report written successfully on disk at path : {} }",
					prop.getProperty("migrator.report.dest.path"));

			AmazonSESUtil.sendMail(file.toString(), prop.getProperty("migrator.ses.email.toAddress"),
					prop.getProperty("migrator.ses.email.fromAddress"));

		} catch (Exception e) {
			LOGGER.error("Validator: { {} }", e.getMessage());
		}
	}

	/**
	 * @param workbook
	 * @param migAssetCount
	 * @param nonMigAssetCount
	 */
	private void createMigrationSummaryReport(XSSFWorkbook workbook, int migAssetCount, int nonMigAssetCount) {
		// Create a blank sheet for Migration_Summary_Report.
		XSSFSheet summerySheet = workbook.createSheet(MigratorConstants.MIGRATION_SUMMARY_REPORT);

		Row migRow = summerySheet.createRow(MigratorConstants.ZERO);
		Cell migrationCount = migRow.createCell(MigratorConstants.ZERO);
		migrationCount.setCellValue(MigratorConstants.MIGRATED_ASSETS_COUNT);
		migrationCount = migRow.createCell(MigratorConstants.ONE);
		migrationCount.setCellValue(migAssetCount);

		Row nonMigRow = summerySheet.createRow(MigratorConstants.ONE);
		Cell nonMigrationCount = nonMigRow.createCell(MigratorConstants.ZERO);
		nonMigrationCount.setCellValue(MigratorConstants.NON_MIGRATED_ASSETS_COUNT);
		nonMigrationCount = nonMigRow.createCell(MigratorConstants.ONE);
		nonMigrationCount.setCellValue(nonMigAssetCount);

		Row rendRow = summerySheet.createRow(MigratorConstants.TWO);
		Cell rendCount = rendRow.createCell(MigratorConstants.ZERO);
		rendCount.setCellValue(MigratorConstants.RENDITION_COUNT);
		rendCount = rendRow.createCell(MigratorConstants.ONE);
		rendCount.setCellValue(assetRendMap.size());

		LOGGER.info("Validator: { logMigrationSummaryReport: Migration Summary Report Created. }");
	}

	/**
	 * @param assetRendMap
	 * @param workbook
	 */
	private void createRenditionSummaryReport(XSSFWorkbook workbook) {
		int mRowNum = 0;
		int cellNum = 0;
		Cell cell;
		// Create Rendition Report
		XSSFSheet renditionSheet = workbook.createSheet(MigratorConstants.RENDITION_SUMMARY_REPORT);
		Row headerRow = renditionSheet.createRow(mRowNum++);
		cell = headerRow.createCell(cellNum++);
		cell.setCellValue(MigratorConstants.RENDITION_PATH);
		cell = headerRow.createCell(cellNum++);
		cell.setCellValue(MigratorConstants.TARGET_ASSET_ID);
		cell = headerRow.createCell(cellNum);
		cell.setCellValue(MigratorConstants.RENDITION_UPLOAD_STATUS);

		for (Entry<String, Map<String, String>> assetRend : assetRendMap.entrySet()) {
			Map<String, String> assets = assetRend.getValue();
			for (Entry<String, String> asset : assets.entrySet()) {
				cellNum = 0;
				Row rowData = renditionSheet.createRow(mRowNum++);
				cell = rowData.createCell(cellNum++);
				cell.setCellValue(asset.getKey());
				cell = rowData.createCell(cellNum++);
				cell.setCellValue(assetRend.getKey());
				cell = rowData.createCell(cellNum);
				cell.setCellValue(asset.getValue());
			}
		}
		LOGGER.info("Validator: { logMigrationSummaryReport: Rendition Summary Report Created. }");
	}

	/**
	 * @param workbook
	 * @param assetMap
	 */
	private void logMissingFIle(XSSFWorkbook workbook, Map<String, String> assetMap) {
		XSSFSheet misAssetSheet = workbook.createSheet(MigratorConstants.MISSING_ASSETS_IN_EXCEL);
		int mRowNum = 0;
		Row row = misAssetSheet.createRow(mRowNum++);
		Cell cell = row.createCell(MigratorConstants.ZERO);
		cell.setCellValue(MigratorConstants.ASSET_ID_STR);
		cell = row.createCell(MigratorConstants.ONE);
		cell.setCellValue(MigratorConstants.ASSET_PATH);

		for (Entry<String, String> asset : assetMap.entrySet()) {
			row = misAssetSheet.createRow(mRowNum++);
			cell = row.createCell(MigratorConstants.ZERO);
			if (!asset.getKey().equals(asset.getValue())) {
				cell.setCellValue(asset.getKey());
			}
			cell = row.createCell(MigratorConstants.ONE);
			cell.setCellValue(asset.getValue());
		}
	}

	/**
	 * @param workbook
	 * @param sourceAbbreviation
	 * @param string
	 * @return
	 */
	private XSSFSheet createMigartionReport(XSSFWorkbook workbook, String sheetName, String sourceAbbreviation) {
		XSSFSheet sheet = workbook.createSheet(sheetName);
		Row headerRow = sheet.createRow(MigratorConstants.ZERO);

		Cell cell = headerRow.createCell(MigratorConstants.ZERO);
		cell.setCellValue(MigratorConstants.ABS_TARGET_PATH);

		cell = headerRow.createCell(MigratorConstants.ONE);
		cell.setCellValue(MigratorConstants.VALIDATION_STATUS);

		cell = headerRow.createCell(MigratorConstants.TWO);
		cell.setCellValue(MigratorConstants.VALIDATION_MSG);
		if (!(sheetName).equals(MigratorConstants.NON_MIGRATED_ASSETS)) {
			cell = headerRow.createCell(MigratorConstants.THREE);
			cell.setCellValue(MigratorConstants.RENDITION_MSG);

			cell = headerRow.createCell(MigratorConstants.FOUR);
			cell.setCellValue(MigratorConstants.AEM_RELATED);
		}
		if (!sourceAbbreviation.equals(MigratorConstants.ISILON_SERVER)) {
			cell = headerRow.createCell(MigratorConstants.FIVE);
			cell.setCellValue(MigratorConstants.MIGRATOR_ASSET_ID);
		}
		return sheet;
	}

	/**
	 * @param sheet
	 * @param dto
	 * @param mRowNum
	 * @param sourceAbbreviation
	 * @param sourceAbbreviation
	 */
	private XSSFSheet updateMigrationReport(XSSFSheet sheet, Map<String, String> metaDataMap, int mRowNum,
			String sourceAbbreviation) {
		Row row = sheet.createRow(mRowNum);

		Cell cell = row.createCell(MigratorConstants.ZERO);
		cell.setCellValue(metaDataMap.get(MigratorConstants.ABS_TARGET_PATH));

		cell = row.createCell(MigratorConstants.ONE);
		cell.setCellValue(metaDataMap.get(MigratorConstants.VALIDATION_STATUS));

		cell = row.createCell(MigratorConstants.TWO);
		try {
			cell.setCellValue(metaDataMap.get(MigratorConstants.VALIDATION_MSG));
		} catch (Exception e) {
			cell.setCellValue(MigratorConstants.ERROR_MSG_CELL_LENGTH);
			LOGGER.info(
					"Validator: { logMigrationSummaryReport: Coulmn Size is more than expected for Asset Id: {}, {} }",
					metaDataMap.get(MigratorConstants.MIGRATOR_ASSET_ID), MigratorConstants.ERROR_MSG_CELL_LENGTH);
			LOGGER.info("Validator: { logMigrationSummaryReport: Rendition Summary : Error Msg: {}}",
					metaDataMap.get(MigratorConstants.VALIDATION_MSG));
		}
		cell = row.createCell(MigratorConstants.THREE);
		cell.setCellValue(metaDataMap.get(MigratorConstants.RENDITION_MSG));

		cell = row.createCell(MigratorConstants.FOUR);
		cell.setCellValue(metaDataMap.get(MigratorConstants.AEM_RELATED));

		if (!sourceAbbreviation.equals(MigratorConstants.ISILON_SERVER)) {
			cell = row.createCell(MigratorConstants.FIVE);
			cell.setCellValue(metaDataMap.get(MigratorConstants.MIGRATOR_ASSET_ID));
		}
		return sheet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.elc.validator.service.ValidatorService#createReportFromAem()
	 */
	@Override
	public void createReportFromAem() {
		AemQueryBuilderResponse allAEMAssets;
		StringBuilder file = new StringBuilder(prop.getProperty("migrator.report.dest.path"));
		file.append(MigratorConstants.AEM_ASSET_SUMMARY).append(sdfFile.format(new Date()))
				.append(MigratorConstants.XLSX);
		try (XSSFWorkbook workbook = new XSSFWorkbook();
				FileOutputStream out = new FileOutputStream(new File(file.toString()))) {
			int offSet = 0;
			Map<String, String> metaDataType = new HashMap<>();
			XSSFSheet sheet = workbook.createSheet(MigratorConstants.ASSET_MIGRATION);
			int rowNum = 0;
			LOGGER.info("Validator: { createReportFromAem: Logging Report Started. }");
			do {
				allAEMAssets = this.getAllAEMAssets(offSet);
				LOGGER.info("Validator: { aemAssetSize {}, offSet{} }", allAEMAssets.getAssetList().size(), offSet);
				for (Asset aemAsset : allAEMAssets.getAssetList()) {
					Row row = sheet.createRow(++rowNum);
					createAemMetadataReport(row, aemAsset, metaDataType);
				}
				offSet += Integer.parseInt(prop.getProperty("migrator.validation.limits"));
			} while (allAEMAssets.isMore());

			Row headerRow = sheet.createRow(MigratorConstants.ZERO);
			Cell cell = headerRow.createCell(MigratorConstants.ZERO);
			cell.setCellValue(MigratorConstants.ABS_TARGET_PATH);
			cell = headerRow.createCell(MigratorConstants.ONE);
			cell.setCellValue(MigratorConstants.RENDITIONS_TARGET_KEY_STR);
			for (Entry<String, Integer> header : headersMap.entrySet()) {
				cell = headerRow.createCell(header.getValue());
				cell.setCellValue(header.getKey());
			}

			XSSFSheet dataTypeSheet = workbook.createSheet(MigratorConstants.ASSET_MIGRATION_DATA_TYPE);
			rowNum = 0;
			headerRow = dataTypeSheet.createRow(rowNum);
			cell = headerRow.createCell(MigratorConstants.ZERO);
			cell.setCellValue(MigratorConstants.METADATA);
			cell = headerRow.createCell(MigratorConstants.ONE);
			cell.setCellValue(MigratorConstants.METADATA_DATA_TYPE);
			for (Entry<String, String> metaData : metaDataType.entrySet()) {
				headerRow = dataTypeSheet.createRow(++rowNum);
				cell = headerRow.createCell(MigratorConstants.ZERO);
				cell.setCellValue(metaData.getKey());
				cell = headerRow.createCell(MigratorConstants.ONE);
				cell.setCellValue(metaData.getValue());
			}

			workbook.write(out);
			LOGGER.info("Validator: {Report written successfully on disk at path : {} }",
					prop.getProperty("migrator.report.dest.path"));

			AmazonSESUtil.sendMail(file.toString(), prop.getProperty("migrator.ses.email.toAddress"),
					prop.getProperty("migrator.ses.email.fromAddress"));
		} catch (MigratorServiceException e) {
			LOGGER.error("Validator: {MigratorServiceException }", e);
		} catch (FileNotFoundException e) {
			LOGGER.error("Validator: {FileNotFoundException }", e);
		} catch (IOException e) {
			LOGGER.error("Validator: {IOException }", e);
		}

	}

	/**
	 * @param row
	 * @param aemAsset
	 * @param metaDataType
	 */
	private void createAemMetadataReport(Row row, Asset aemAsset, Map<String, String> metaDataType) {
		Map<String, Object> aemMetadata = aemAsset.getMetadata().getAdditionalProperties();

		Cell cell = row.createCell(0);
		cell.setCellValue(aemAsset.getPath());
		for (Entry<String, Object> aemData : aemMetadata.entrySet()) {
			updateHeadersMap(aemData.getKey());
			updateMetaDataType(metaDataType, aemData.getKey(), aemData.getValue());
			cell = row.createCell(headersMap.get(aemData.getKey()));
			try {
				cell.setCellValue(getStringValue(aemData.getValue()));
			} catch (Exception e) {
				cell.setCellValue(MigratorConstants.ERROR_MSG_CELL_LENGTH
						+ aemData.getValue().toString().substring(0, 32500));
				LOGGER.info("Validator: {createAemMetadataReport: {}, Key : {}}",
						MigratorConstants.ERROR_MSG_CELL_LENGTH, aemData.getKey());
			}

		}
	}

	/**
	 * @param metaDataType
	 * @param header
	 * @param value
	 */
	private void updateMetaDataType(Map<String, String> metaDataType, String header, Object value) {
		if (metaDataType.get(header) == null) {
			metaDataType.put(header, getDataType(value));
		} else if (!metaDataType.get(header).equals(getDataType(value))) {
			String val = getDataType(value) + MigratorConstants.SPECIAL_CHARACTER_UNDERSCORE + metaDataType.get(header);
			if (metaDataType.get(val) == null) {
				metaDataType.put(header + MigratorConstants.SPECIAL_CHARACTER_HASH + val, val);
				LOGGER.info("Validator: {updateMetaDataType: different Data Type for header: {} {}}", header, val);
			}
		}
	}

	/**
	 * @param value
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private String getDataType(Object value) {
		String dataType;
		if (value instanceof ArrayList) {
			dataType = ((ArrayList) value).get(0).getClass().getSimpleName()
					+ MigratorConstants.SPECIAL_CHARACTER_SQUARE_BRACES;
		} else {
			dataType = value.getClass().getSimpleName();
		}
		return dataType;
	}

	/**
	 * @param aemMetadata
	 */
	private void updateHeadersMap(String header) {
		headersMap.computeIfAbsent(header, key -> (headersMap.size() + MigratorConstants.TWO));
	}
}
