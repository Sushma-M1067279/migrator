package com.mindtree.transformer.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.mindtree.models.dto.BrandMasterMappingDto;
import com.mindtree.transformer.factory.ApplicationFactory;
import com.mindtree.core.service.AppContext;
import com.mindtree.core.service.IMigratorBusiness;
import com.mindtree.core.service.ITransformer;
import com.mindtree.core.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.BusinessRulesUtil;
import com.mindtree.utils.helper.MasterMetadataMapReader;
import com.mindtree.utils.helper.MigrationReportUtil;
import com.mindtree.utils.helper.MigrationUtil;
import com.mindtree.utils.helper.S3Utility;

/**
 * Transformer for Drive based source systems which do not provide metadata and
 * renditions. Ex: Isilon<br>
 * This transformer consumes following<br>
 * 1. Assets in S3 source bucket - should be in a folder hierarchy<br>
 * 2. Master metadata mapping (XLS)<br>
 * 3. Metadata mapping rules
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
public class DriveBasedTransformer extends AbstractTransformer {

	private Map<String, String> migratedAssetsMap = new HashMap<String, String>();
	private Map<String, String> nonMigratedAssetsMap = new HashMap<String, String>();
	private List<Map<String, String>> assetMetadataMapList = new ArrayList<Map<String, String>>();
	private Set<String> finalHeadersSet = new HashSet<String>();
	private List<String> missingAssets = new ArrayList<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(DriveBasedTransformer.class);
	private int assetCounter = 0;

	private String brand = null;

	@SuppressWarnings("unchecked")
	@Override
	public boolean transform(String brandAbbreviation, String instanceNumber) {
		boolean isSuccess = false;
		try {

			StringBuilder brandPrefix = MigrationUtil.prepareBrandPrefix(brandAbbreviation);
			LOGGER.info("DriveBasedTransformer transform : brandPrefix:{}", brandPrefix);

			Properties prop = AppContext.getAppConfig();
			/**
			 * Read brand specific properties.
			 */
			brand = prop.getProperty(brandPrefix+ MigratorConstants.BRAND);
			String masterBrandMappingFileName = prop.getProperty("migrator.asset.masterBrandMappingFileName");
			String mastetToBrandMappingSheetName = prop.getProperty("migrator.asset.mastetToBrandMappingSheetName");
			
			LOGGER.info("DriveBasedTransformer transform : brandPrefix:{}", brandPrefix);
			LOGGER.info("DriveBasedTransformer transform : prop size:{}", prop.size());
			String srcFolder = prop.getProperty(brandPrefix + MigratorConstants.STORAGE_SOURCE_BUCKET_FOLDER);

			Map<String, Long> fileSizes = storage.getFileSizes(srcFolder);
			LOGGER.info("DriveBasedTransformer transform : Assets size::{}", fileSizes.size());
			BusinessRulesUtil.assetsPathsSet.addAll(fileSizes.keySet());

			IMigratorBusiness migratorBusiness = ApplicationFactory.getMigratorBusiness(brandAbbreviation);

			Map<String, BrandMasterMappingDto> masterMetadataMap = MasterMetadataMapReader.getBrandMasterMapping(
					masterBrandMappingFileName, mastetToBrandMappingSheetName, brandAbbreviation);
			int skippedFileCount = 0;
			int count = 0;

			if(masterMetadataMap == null){
				return false;
			}
			
			Map<String, Object> outputMap = new HashMap<String, Object>();
			for (Map.Entry<String, Long> assetFile : fileSizes.entrySet()) {
				LOGGER.info("Processing : "+assetFile.getKey());
				if (assetFile.getKey().trim().startsWith(MigratorConstants.IGNORE_FOLDER_SPECIALTY_MULTI_CHANNEL)) {
					skippedFileCount++;
					continue;
				}

				String migrationAssetId = MigrationUtil.generateAssetId(brand, instanceNumber, ++assetCounter);
				Thread.currentThread().setName(migrationAssetId);
				
				outputMap = migratorBusiness.applyBrandSpecificRules(masterMetadataMap, assetFile, brand,
						brandPrefix.toString());
				if (outputMap.get(MigratorConstants.OUTPUT_MIGRATED_ASSETS_MAP) != null) {
					LOGGER.info("OUTPUT_MIGRATED_ASSETS_MAP size : "
							+ ((Map<String, String>) outputMap.get(MigratorConstants.OUTPUT_MIGRATED_ASSETS_MAP))
									.size());
					migratedAssetsMap.putAll((Map<String, String>) outputMap
							.get(MigratorConstants.OUTPUT_MIGRATED_ASSETS_MAP));
				}
				if (outputMap.get(MigratorConstants.OUTPUT_NON_MIGRATED_ASSETS_MAP) != null) {
					LOGGER.info("OUTPUT_NON_MIGRATED_ASSETS_MAP size : "
							+ ((Map<String, String>) outputMap
									.get(MigratorConstants.OUTPUT_NON_MIGRATED_ASSETS_MAP)).size());
					nonMigratedAssetsMap.putAll((Map<String, String>) outputMap
							.get(MigratorConstants.OUTPUT_NON_MIGRATED_ASSETS_MAP));
				}
				if (outputMap.get(MigratorConstants.OUTPUT_ASSET_METADATA_MAP) != null) {
					LOGGER.info("OUTPUT_ASSET_METADATA_MAP size : "
							+ ((Map<String, String>) outputMap.get(MigratorConstants.OUTPUT_ASSET_METADATA_MAP))
									.size());
					assetMetadataMapList.add((Map<String, String>) outputMap
							.get(MigratorConstants.OUTPUT_ASSET_METADATA_MAP));
					finalHeadersSet.addAll(((Map<String, String>) outputMap
							.get(MigratorConstants.OUTPUT_ASSET_METADATA_MAP)).keySet());
				}
				if (outputMap.get(MigratorConstants.OUTPUT_MISSING_ASSETS) != null) {
					LOGGER.info("OUTPUT_MISSING_ASSETS size : "
							+ ((List<String>) outputMap.get(MigratorConstants.OUTPUT_MISSING_ASSETS)).size());
					missingAssets.addAll((List<String>) outputMap.get(MigratorConstants.OUTPUT_MISSING_ASSETS));
				}

			}

			LOGGER.info("--------------------- DriveBasedTransformer transform : Summary ---------------------------");
			LOGGER.info("DriveBasedTransformer transform : skippedFileCount size::{}", skippedFileCount);
			LOGGER.info("DriveBasedTransformer transform : migratedAssetsMap size::{}",
					migratedAssetsMap.size());
			LOGGER.info("DriveBasedTransformer transform : nonMigratedAssetsMap size::{}",
					nonMigratedAssetsMap.size());
			LOGGER.info("DriveBasedTransformer transform : migrationAssetsList size::{}",
					assetMetadataMapList.size());

			LOGGER.info("--------------------- DriveBasedTransformer transform : Summary ---------------------------");
			MigrationReportUtil.logMigrationSummaryReport( brandPrefix, migratedAssetsMap, nonMigratedAssetsMap);
			isSuccess = MigrationReportUtil.generateOutputAndReplicateAssets(brandPrefix, finalHeadersSet, assetMetadataMapList);
		} catch (MigratorServiceException e) {
			LOGGER.error("DriveBasedTransformer transform :: ElcServiceException :{}", e);
			return false;
		}
		return isSuccess;

	}

}
