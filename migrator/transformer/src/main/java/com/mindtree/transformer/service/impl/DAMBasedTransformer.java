package com.mindtree.transformer.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.mindtree.holoxo.util.HoloxoMetadataUtil;
import com.mindtree.models.dto.BrandMasterMappingDto;
import com.mindtree.transformer.factory.ApplicationFactory;
import com.mindtree.core.service.AppContext;
import com.mindtree.core.service.IDataFileReader;
import com.mindtree.core.service.IMigratorBusiness;
import com.mindtree.core.service.ITransformer;
import com.mindtree.core.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.MasterMetadataMapReader;
import com.mindtree.utils.helper.MigrationReportUtil;
import com.mindtree.utils.helper.MigrationUtil;

/**
 * Transformer for DAM based source systems which provide metadata and
 * renditions. Ex: Global Edit.<br>
 * This transformer consumes following<br>
 * 1. Assets in S3 source bucket - should be in a flat structure<br>
 * 2. Master metadata mapping (XLS)<br>
 * 3. Asset dump sheet (XLS)<br>
 * 4. Metadata Mapping Rules<br>
 * <br>
 * 
 * This transformer outputs following<br>
 * 1. Metadata XLS<br>
 * 2. Folder creator CSV<br>
 * 3. Migration report <br>
 * It also copies assets from source to destination buckets depending on the
 * configuration
 * 
 * @author M1032046
 *
 */
public class DAMBasedTransformer extends AbstractTransformer {

	static final Logger LOGGER = LoggerFactory.getLogger(DAMBasedTransformer.class);
//	private static final Logger LOGGER_UTIL = LoggerFactory.getLogger(StopWatchUtil.class);

	private List<Map<String, String>> finalAssetMetadataMapList = new ArrayList<Map<String, String>>();
	private Set<String> finalAssetMetadataMapKeySet = new HashSet<String>();
	private String migrationAssetId;
	private int assetCounter = 0;
	private String lastProcessedAssetByInterruption = null;
	private String brand = null;
	private String instanceNumber = null;
	private String assetIdBeingProcessed = null;
	private String folderPath = null;
	private IMigratorBusiness migratorBusiness = null;
	Map<String, Integer> csvHeaderMap = new HashMap<>();
	private String sourceFolder = null;

	/**
	 * The transform holds the logic to transform Excel, XMP and folder specific
	 * rules into Asset meta data.
	 * 
	 * @param AbstractApplicationContext
	 *            , brandAbbreviation
	 * @return Status
	 */
	public boolean transform(String brandAbbreviation, String instanceNumb) {

		instanceNumber = instanceNumb;
		StringBuilder brandPrefix = MigrationUtil.prepareBrandPrefix(brandAbbreviation);
		boolean isSuccess = false;
		LOGGER.info("LegacyDAMBasedMetadataTransformer transform : brandPrefix:{}", brandPrefix);
		try {

			Properties prop = AppContext.getAppConfig();

			String masterBrandMappingFileName = prop.getProperty("migrator.asset.masterBrandMappingFileName");
			String mastetToBrandMappingSheetName = prop.getProperty("migrator.asset.mastetToBrandMappingSheetName");
			/**
			 * Read brand specific properties.
			 */
			String brandAssetDumpFileName = prop.getProperty(brandPrefix + ""
					+ MigratorConstants.BRAND_ASSET_DUMP_FILENAME);
			int assetDumpSheetIndex = Integer.parseInt(prop.getProperty(brandPrefix + ""
					+ MigratorConstants.BRAND_ASSET_DUMP_SHEETINDEX));
			int processAssetsCount = Integer.parseInt(prop.getProperty(brandPrefix + ""
					+ MigratorConstants.BRAND_ASSET_DUMP_PROCESS_COUNT));
			lastProcessedAssetByInterruption = prop.getProperty(brandPrefix + ""
					+ MigratorConstants.BRAND_MIGRATION_FAILURE_LAST_PROCESSED_ID);
			
			HoloxoMetadataUtil.brandPrefix = brandPrefix.toString();

			brand = prop.getProperty(brandPrefix + "" + MigratorConstants.BRAND);
			folderPath = prop.getProperty(brandPrefix + "" + MigratorConstants.XMP_TRANSFORMATION_PATH);
			sourceFolder = prop.getProperty(brandPrefix + "" + MigratorConstants.STORAGE_SOURCE_BUCKET_FOLDER);

			migratorBusiness = ApplicationFactory.getMigratorBusiness(brandAbbreviation);

			// Read master metadata mapping sheet
			Map<String, BrandMasterMappingDto> masterMetadataMap = MasterMetadataMapReader.getBrandMasterMapping(
					masterBrandMappingFileName, mastetToBrandMappingSheetName, brandAbbreviation);

			if(masterMetadataMap == null){
				return false;
			}
			Map<String, String> assetMetadataMap = new HashMap<String, String>();
			Map<String, String> assetKindMap = new HashMap<String, String>();
			boolean needsMigration = false;

			// Read asset dump sheet
			IDataFileReader excelFileReader = new XExcelFileReader(brandAssetDumpFileName, assetDumpSheetIndex,brandAbbreviation);
			int count = 0;
			
			if(excelFileReader == null){
				return false;
			}

			for (String[] currentRow : excelFileReader.readRows(processAssetsCount)) {
//				if (true) {
				++count;
				// first column check
				if (currentRow[0].equalsIgnoreCase(MigratorConstants.COLUMN_FILE_NAME)) {
					int headerIndex = 0;
					// loop thru first row prepare header map
					for (String header : currentRow) {
						if (header != null && !header.isEmpty()) {
							csvHeaderMap.put(header, headerIndex++);
						}

					}
					// subsequent rows
				} else {
					if (lastProcessedAssetByInterruption != null
							&& !lastProcessedAssetByInterruption.equalsIgnoreCase("-1")) {
						// resume transformation process
						needsMigration = resumeLastTransformation(masterMetadataMap, assetMetadataMap,
								assetKindMap, currentRow);
					} else {
						// start transformation process
						needsMigration = startNewTransformation(masterMetadataMap, assetMetadataMap, assetKindMap,
								excelFileReader, count, currentRow);
					}
				}

//				} else {
//					MigrationReportUtil.updateLastProcessedAsset(assetIdBeingProcessed);
//					break;
//				}
			}
			// create migration report
			MigrationReportUtil.createSummaryReport( brandPrefix, migratedAssetsMap, nonMigratedAssetsMap);
			// create output CSV's and replicate assets from source to destination path
			isSuccess = MigrationReportUtil.generateOutputAndReplicateAssets(brandPrefix, 
					finalAssetMetadataMapKeySet, finalAssetMetadataMapList);
		} catch (MigratorServiceException e) {
			LOGGER.error(
					"LegacyDAMBasedMetadataTransformer transform : Asset failed which is being processed is : assetIdBeingProcessed: {}",
					assetIdBeingProcessed);
			LOGGER.error("LegacyDAMBasedMetadataTransformer transform :: MigratorServiceException :{}", e);
			
			return false;
		} catch (Exception e) {
			LOGGER.error(
					"LegacyDAMBasedMetadataTransformer transform : Asset migration failed due to error on excel reading : assetIdBeingProcessed: {}",
					assetIdBeingProcessed);
			LOGGER.error("LegacyDAMBasedMetadataTransformer transform : : Exception :{}", e);
			return false;
		}
		
		return isSuccess;

	}

	private boolean resumeLastTransformation(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMap, Map<String, String> assetKindMap, String[] currentRow)
			throws MigratorServiceException {
		boolean needsMigration = false;
		String assetId = currentRow[MigratorConstants.COLUMN_ASSETID];
		if (assetId.equalsIgnoreCase(lastProcessedAssetByInterruption)) {
			lastProcessedAssetByInterruption = "-1";
			migrationAssetId = MigrationUtil.generateAssetId(brand, instanceNumber, ++assetCounter);
			Thread.currentThread().setName(migrationAssetId);
			assetIdBeingProcessed = assetId;
			needsMigration = processRow(masterMetadataMap, assetMetadataMap, assetKindMap, currentRow);

		}
		return needsMigration;
	}

	private boolean startNewTransformation(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMap, Map<String, String> assetKindMap, IDataFileReader excelFieReader,
			int count, String[] currentRow) throws MigratorServiceException {
//		LOGGER_UTIL.info("LegacyDAMBasedMetadataTransformer transformer :Count:{}", count);
		/**
		 * Process asset row to get metadata from asset dump sheet headers.
		 */
		boolean needsMigration = processRow(masterMetadataMap, assetMetadataMap, assetKindMap, currentRow);

		if (needsMigration) {
			/**
			 * For each assetKindMap entry, process metadata XMP and renditions
			 * if any
			 */
			for (Map.Entry<String, String> assetKindMapEntry : assetKindMap.entrySet()) {
				processXMPAndRenditions(folderPath, masterMetadataMap, assetMetadataMap, assetKindMapEntry);
			}
			MigrationUtil.extractRatingsFromFileName(assetMetadataMap);
			assetMetadataMap.put(MigratorConstants.MIGRATION_ASSET_ID, migrationAssetId);
			assetMetadataMap.put(MigratorConstants.SOURCE,
					MigrationUtil.encode(MigratorConstants.SOURCE_ASSET_MIGRATION));
			finalAssetMetadataMapList.add(new HashMap<String, String>(assetMetadataMap));
			finalAssetMetadataMapKeySet.addAll(assetMetadataMap.keySet());

		}
		/**
		 * Clear last processed asset details and continue processing for next
		 * asset in an row.
		 */
		assetKindMap.clear();
		assetMetadataMap.clear();
		needsMigration = false;
		HoloxoMetadataUtil.folderMappingMap.clear();
		return needsMigration;

	}

	/**
	 * This method processes XMP side car and related/renditions logic.
	 * 
	 * @param folderPath
	 * @param masterMetadataMap
	 * @param excelMap
	 * @param assetMap
	 * @param assetMetadataMap
	 * @param assetKindMapEntry
	 * @throws MigratorServiceException
	 */
	private void processXMPAndRenditions(String folderPath, Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMap, Map.Entry<String, String> assetKindMapEntry)
			throws MigratorServiceException {
		/**
		 * Process XMP Metadata
		 */
		if (assetKindMapEntry.getKey().equalsIgnoreCase(MigratorConstants.XMP_METADATA)
				&& MigrationUtil.getFileExtension(assetKindMapEntry.getValue()).equalsIgnoreCase(
						MigratorConstants.FILE_EXTENTION_XMP)) {
			String xmpFileName = assetKindMapEntry.getValue();

			/**
			 * Read XMP metadata file and convert to a map.
			 */
			String content = storage.getFileContent(folderPath + storage.fileSeparator() + xmpFileName);
			Map<String, String> xmpMetadataMap = MigrationUtil.fetchDataFromXmp(content);
			/**
			 * Iterate over XMP map to match master meta datafield and apply
			 * business rules.
			 */
			Map<String, String> assetMetadataMapFromXMP = new TreeMap<String, String>();
			/**
			 * Clean up XMP metadata and apply business rules
			 */
			assetMetadataMapFromXMP = cleanUpXMPMapAndApplyRules(masterMetadataMap, assetMetadataMapFromXMP,
					xmpMetadataMap, MigratorConstants.CSV_EXPORT_FLOW);
			/**
			 * Add metadata extracted from XMP to main asset metadata map
			 */
			assetMetadataMap.put(MigratorConstants.AEM_PROPERTY_XMPFILE, MigrationUtil.encode(xmpFileName));
			assetMetadataMap.putAll(assetMetadataMapFromXMP);

		}
		/**
		 * Process renditions
		 */
		else if (assetKindMapEntry.getKey().equalsIgnoreCase(MigratorConstants.COLUMN_KIND_RENDITION)) {
			StringBuilder renditions = null;
			String derivative = assetKindMapEntry.getValue();
			if (assetMetadataMap.get(MigratorConstants.AEM_PROPERTY_RENDITIONS) != null) {
				renditions = new StringBuilder(assetMetadataMap.get(MigratorConstants.AEM_PROPERTY_RENDITIONS));
				renditions.append(MigratorConstants.COLON);
				renditions.append(derivative);
			} else {
				renditions = new StringBuilder(derivative);
			}

			assetMetadataMap.put(MigratorConstants.AEM_PROPERTY_RENDITIONS,
					MigrationUtil.encode(renditions.toString()));
		}

	}

	/**
	 * Iterate over XMP map to match master meta data field and apply business
	 * rules.
	 * 
	 * @param masterMetadataMap
	 * @param assetMetadataMapFromXMP
	 * @param xmpMap
	 * @param exportFlowFlag
	 */
	private Map<String, String> cleanUpXMPMapAndApplyRules(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMapFromXMP, Map<String, String> xmpMap, String exportFlowFlag) {

		for (Map.Entry<String, String> xmpMetadata : xmpMap.entrySet()) {
			if (xmpMetadata.getValue() != null && !xmpMetadata.getValue().isEmpty()) {
				String masterMetadataHeader = xmpMetadata.getKey();
				String masterMetadataHeaderTemp = masterMetadataHeader;
				if (masterMetadataHeaderTemp.contains(MigratorConstants.OPEN_FLOWET_BRACES)) {
					String[] tempName = masterMetadataHeaderTemp.split(Pattern
							.quote(MigratorConstants.OPEN_FLOWET_BRACES));
					masterMetadataHeaderTemp = tempName[0];
				}
				// Extract value of the metadata
				String metadataHeader = masterMetadataHeaderTemp.substring(
						masterMetadataHeaderTemp.lastIndexOf(MigratorConstants.SPECIAL_CHARACTER_COLON) + 1,
						masterMetadataHeaderTemp.length());
				// apply brand rules
				if (null != metadataHeader && masterMetadataMap.containsKey(metadataHeader)) {

					if (migratorBusiness != null) {
						migratorBusiness.applyBrandXMPMetadataRules(masterMetadataMap, assetMetadataMapFromXMP,
								exportFlowFlag, xmpMetadata, metadataHeader);
					}
				} else {
					assetMetadataMapFromXMP.put(masterMetadataHeader, MigrationUtil.encode(xmpMetadata.getValue()));
				}
			}
		}

		return assetMetadataMapFromXMP;
	}

	/**
	 * This method each row of asset dump and updated assetKind map for further
	 * processing.
	 * 
	 * @param masterMetadataMap
	 * @param assetMetadataMap
	 * @param assetKindMap
	 * @param needsMigration
	 * @param currentRow
	 * @param kind
	 * @param fileNameFinal
	 * @return
	 * @throws MigratorServiceException
	 */
	private boolean processRow(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMap, Map<String, String> assetKindMap, String[] currentRow)
			throws MigratorServiceException {
		// Map<String, Integer> headers = csvHeaderMap;
		String assetKind = currentRow[MigratorConstants.COLUMN_KIND_INDEX];
		String fileName = currentRow[MigratorConstants.COLUMN_ORIGINAL_FILE_NAME];
		/**
		 * Process main asset. If there is no indication if the file is main
		 * asset or a rendition, the file will be processed as an asset
		 */
		boolean needsMigration = false;
		if (assetKind != null
				&& (assetKind.trim().equalsIgnoreCase(MigratorConstants.COLUMN_KIND_ASSET) || assetKind.isEmpty())) {
			/**
			 * Exclude XMP's when Asset Kind column is blank
			 */
			if (assetKind.isEmpty()
					&& MigrationUtil.getFileExtension(fileName).equalsIgnoreCase(MigratorConstants.FILE_EXTENTION_XMP)) {
				assetKindMap.put(MigratorConstants.XMP_METADATA, fileName);
			} else {
				needsMigration = processAssetRow(masterMetadataMap, assetMetadataMap, assetKindMap, currentRow);
			}
		}

		/**
		 * Process renditions later
		 */

		else if (assetKind != null && assetKind.trim().equalsIgnoreCase(MigratorConstants.COLUMN_KIND_RENDITION)) {
			assetKindMap.put(MigratorConstants.COLUMN_KIND_RENDITION, fileName);
		} else {
			assetKindMap.put(assetKind, fileName);
		}
		return needsMigration;
	}

	/**
	 * This method process assets whose Kind starts with 'assets_'.
	 * 
	 * @param headers
	 * @param masterMetadataMap
	 * @param assetMetadataMap
	 * @param assetKindMap
	 * @param currentRow
	 * @param assetId
	 * @param kind
	 * @param fileNameFinal
	 * @param createdDateString
	 * @return
	 * @throws MigratorServiceException
	 */
	private boolean processAssetRow(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMap, Map<String, String> assetKindMap, String[] currentRow)
			throws MigratorServiceException {

		String kind = currentRow[MigratorConstants.COLUMN_KIND_INDEX];
		String fileName = currentRow[MigratorConstants.COLUMN_ORIGINAL_FILE_NAME];
		String brandMetadataHeader = "";
		String brandMetadataValue = "";
		boolean needsMigration = true;

		// for the given row, loop thru all available headers
		for (Entry<String, Integer> headerMap : csvHeaderMap.entrySet()) {
			brandMetadataHeader = headerMap.getKey();

			if (csvHeaderMap.get(brandMetadataHeader) < currentRow.length) {
				brandMetadataValue = currentRow[csvHeaderMap.get(brandMetadataHeader)];

				if (brandMetadataValue != null && !brandMetadataValue.isEmpty()) {
					// process cell : This cell value is a metadata
					processCell(masterMetadataMap, assetMetadataMap, brandMetadataHeader, brandMetadataValue);
				}
			}

		}
		
		assetMetadataMap.put(MigratorConstants.CSV_IMPORTER_COLUMN_SRC_REL_PATH, 
				this.sourceFolder + AppContext.getStorage().fileSeparator() + fileName);

		// apply brand specific folder rules
		if (migratorBusiness != null) {
			migratorBusiness.applyBrandFolderRules(masterMetadataMap, assetMetadataMap);
		}
		migratedAssetsMap.put(
				fileName,
				MigratorConstants.ASSET_ELIGIBLE_FOR_MIGRATION
						+ assetMetadataMap.get(MigratorConstants.ABS_TARGET_PATH) + ":Migrate_All_asset");
		assetKindMap.put(kind, fileName);
		return needsMigration;
	}

	/**
	 * This method fetches AEM Property name and updates the assets meta data
	 * map with AEM property name as key.
	 * 
	 * @param assetId
	 * @param needsMigration
	 * @param masterMetadataMap
	 * @param excelMDMap
	 * @param brandMetadataHeader
	 * @param brandMetadataValue
	 * @param fileNameFinal
	 * @return
	 * @throws MigratorServiceException
	 */
	private void processCell(Map<String, BrandMasterMappingDto> masterMetadataMap,
			Map<String, String> assetMetadataMap, String brandMetadataHeader, String brandMetadataValue)
			throws MigratorServiceException {
		if (migratorBusiness != null) {
			// apply brand specific rules for this metadata
			migratorBusiness.applyBrandXLSMetadataRules(masterMetadataMap, assetMetadataMap, brandMetadataHeader,
					brandMetadataValue.trim());
		} else {
			throw new MigratorServiceException("Please pass the brand! Brand argument is missing!");
		}
	}

	/**
	 * This method is used to update asset metadata mapper entity columns to
	 * save into database.
	 * 
	 * @param brand
	 * @param brandMetadataMap
	 */
	private void updateAssetMetadataTableColumns(Map<String, String> brandMetadataMap) {
		brandMetadataMap.put(MigratorConstants.MIGRATION_ASSET_ID, migrationAssetId);
		brandMetadataMap.put(MigratorConstants.SOURCE, MigrationUtil.encode(MigratorConstants.SOURCE_ASSET_MIGRATION));
	}

}
