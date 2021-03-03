package com.mindtree.bluenoid.util;

import java.util.Map;

import com.mindtree.bluenoid.business.BluenoidBusinessImpl;
import com.mindtree.bluenoid.config.BluenoidReqConfigurationLoader;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.BusinessRulesUtil;
import com.mindtree.utils.helper.S3Utility;

/**
 * @author M1032046
 *
 */
public class FilterUtil {

	/**
	 * Applying Size Filter Rule : Ignore files if size is 0 bytes.
	 * 
	 * @param assetPath
	 * @param assetSize
	 * @param nonMigratedAssetsMap
	 * @return
	 */
	public static boolean filterEmptyAssets(String assetPath, Long assetSize, Map<String, String> nonMigratedAssetsMap) {
		boolean eligibleForMigration = true;

		if (assetSize <= 0) {
			eligibleForMigration = false;
			nonMigratedAssetsMap.put(assetPath, MigratorConstants.ASSET_NOT_ELIGIBLE_FOR_MIGRATION + assetPath
					+ ": Asset Size is:" + assetSize + " bytes");
		}

		return eligibleForMigration;
	}

	/**
	 * Applying Blank File Size Filter Rule : If file extension is blank then
	 * ignore files which has size less than 1 MB.
	 * 
	 * @param assetPath
	 * @param assetSize
	 * @param nonMigratedAssetsMap
	 * @param migratedAssetsMap
	 * @return
	 */
	public static boolean filterBlankExtensionAssets(String assetPath, Long assetSize,
			Map<String, String> migratedAssetsMap, Map<String, String> nonMigratedAssetsMap) {
		boolean eligibleForMigration = true;

		/**
		 * Applying Blank File Size Filter Rule : If file extension is blank
		 * then ignore files which has size less than 1 MB.
		 */
		if (assetSize < MigratorConstants.SIZE_1MB) {
			eligibleForMigration = false;
			migratedAssetsMap.remove(assetPath);
			nonMigratedAssetsMap.put(assetPath, MigratorConstants.ASSET_NOT_ELIGIBLE_FOR_MIGRATION + assetPath
					+ ":Asset Size is :" + S3Utility.format(assetSize, 2) + ":Blank File Extension");
		} else {
			if (!migratedAssetsMap.containsKey(assetPath)) {
				migratedAssetsMap.put(assetPath, MigratorConstants.ASSET_ELIGIBLE_FOR_MIGRATION + assetPath
						+ ":Asset Size is: " + S3Utility.format(assetSize, 2) + ":Blank File Extension");
			}
		}

		return eligibleForMigration;
	}

	/**
	 * Applying File extensions Filter Rule : Only file type which are marked as
	 * Migrate ='Yes' to be considered for Migration and ignore rest all.
	 * 
	 * @param assetPath
	 * @param assetSize
	 * @param nonMigratedAssetsMap
	 * @param migratedAssetsMap
	 * @return
	 */
	public static boolean filterAssetsByToBeMigratedFileTypes(String assetPath, Long assetSize, String fileExtension,
			Map<String, String> migratedAssetsMap, Map<String, String> nonMigratedAssetsMap) {
		boolean eligibleForMigration = true;

		/**
		 * Applying File extensions Filter Rule : Only file type which are
		 * marked as Migrate ='Yes' to be considered for Migration and ignore
		 * rest all.
		 */
		if (BluenoidReqConfigurationLoader.fileTypesToMigrate != null
				&& BluenoidReqConfigurationLoader.fileTypesToMigrate.size() > 0) {

			if (!BluenoidReqConfigurationLoader.fileTypesToMigrate.contains(fileExtension.toLowerCase())) {

				eligibleForMigration = false;

				migratedAssetsMap.remove(assetPath);

				nonMigratedAssetsMap.put(assetPath, MigratorConstants.ASSET_NOT_ELIGIBLE_FOR_MIGRATION + assetPath
						+ ":Extensions doesn't match to be migrated extensions List:" + fileExtension);
			} else {
				/**
				 * At this point - Asset is eligible for migration. Apply
				 * Business rules - Metadata & Folder mapping rules.
				 */
				if (!migratedAssetsMap.containsKey(assetPath)) {
					migratedAssetsMap.put(assetPath, MigratorConstants.ASSET_ELIGIBLE_FOR_MIGRATION + assetPath
							+ ": Asset Size is: " + S3Utility.format(assetSize, 2) + ":" + fileExtension);
				}

			}

		}

		return eligibleForMigration;
	}

	/**
	 * If asset(with blank extension) is eligible for Migration then apply Rule
	 * - If file extension is blank then consider custom extensions defined by
	 * OA in sheet 'Optimity File Extension'.
	 * 
	 * @param assetPath
	 * @param assetSize
	 * @param fileName
	 * @param eligibleToMigrate
	 */
//	public static boolean applyCustomExtensionsToBlankAssets(String assetPath, Long assetSize, String fileName) {
//		boolean eligibleForMigration = true;
//
//		if (BluenoidReqConfigurationLoader.customExtensionsMap.keySet().contains(assetPath)) {
//			Map<String, String> customExtensionMap = BluenoidReqConfigurationLoader.customExtensionsMap.get(assetPath);
//
//			if (customExtensionMap != null) {
//				String mainFileExtension = customExtensionMap.get("Main FileType");
//				String customExtension = customExtensionMap.get("File Type");
//
//				if (customExtension != null) {
//
//					if (customExtension.equalsIgnoreCase("delete")) {
//						// BluenoidBusinessImpl.migratedAssetsMap.remove(assetPath);
//						eligibleForMigration = false;
//						BluenoidBusinessImpl.nonMigratedAssetsMap.put(assetPath,
//								MigratorConstants.ASSET_NOT_ELIGIBLE_FOR_MIGRATION + assetPath + ":Asset Size is:"
//										+ S3Utility.format(assetSize, 2) + ":Blank File Extension - Marked as Delete");
//					} else {
//
//						String oldAssetPath = assetPath;
//
//						getAllFiles(assetPath, fileName, mainFileExtension, customExtension);
//
//						// BluenoidBusinessImpl.migratedAssetsMap.remove(oldAssetPath);
//						eligibleForMigration = true;
//						BluenoidBusinessImpl.migratedAssetsMap.put(oldAssetPath,
//								MigratorConstants.ASSET_ELIGIBLE_FOR_MIGRATION + oldAssetPath + ":Asset Size is:"
//										+ S3Utility.format(assetSize, 2) + ":" + customExtension
//										+ ":Custom File Extension");
//					}
//				}
//
//			}
//
//		}
//		return eligibleForMigration;
//	}

	private static void getAllFiles(String assetPath, String fileName, String mainFileExtension, String customExtension) {
		if (mainFileExtension.isEmpty() || mainFileExtension.equalsIgnoreCase(MigratorConstants.NO_EXT)) {
			getCustomExtensionFiles(assetPath, fileName, customExtension);
		} else {
			filenameWithNoExt(assetPath, fileName, customExtension);
		}
	}

	private static void filenameWithNoExt(String assetPath, String fileName, String customExtension) {
		fileName = fileName.substring(0, fileName.lastIndexOf(MigratorConstants.DOT));
		assetPath = assetPath.substring(0, fileName.lastIndexOf(MigratorConstants.DOT));

		if (!customExtension.isEmpty() && customExtension.equalsIgnoreCase(MigratorConstants.NO_EXT)) {
			fileName = fileName.concat(MigratorConstants.DOT).concat(customExtension);
			assetPath = assetPath.concat(MigratorConstants.DOT).concat(customExtension);
		}
	}

	private static void getCustomExtensionFiles(String assetPath, String fileName, String customExtension) {
		if (!fileName.endsWith(MigratorConstants.DOT)) {
			fileName = fileName.concat(MigratorConstants.DOT);
			assetPath = assetPath.concat(MigratorConstants.DOT);
		}

		fileName = fileName.concat(customExtension);
		assetPath = assetPath.concat(customExtension);
	}

	public static String applyCustomExtensions(String assetPath, String mainFileExtension, String customExtension) {
		String fileName = assetPath.substring(assetPath.lastIndexOf('/') + 1, assetPath.length()).trim();
		if (mainFileExtension.isEmpty() || mainFileExtension.equalsIgnoreCase(MigratorConstants.NO_EXT)) {
			if (!fileName.endsWith(MigratorConstants.DOT)) {
				fileName = fileName.concat(MigratorConstants.DOT);
				assetPath = assetPath.concat(MigratorConstants.DOT);
			}

			fileName = fileName.concat(customExtension);
			assetPath = assetPath.concat(customExtension);
			// fileExtension = customExtension;
		} else {

			// fileExtension = "";

			if (!customExtension.isEmpty() && !customExtension.equalsIgnoreCase(MigratorConstants.NO_EXT)) {
				// fileName =
				// fileName.substring(0,fileName.lastIndexOf(MigratorConstants.DOT));
				// assetPath =
				// assetPath.substring(0,assetPath.lastIndexOf(MigratorConstants.DOT));

				fileName = fileName.concat(MigratorConstants.DOT).concat(customExtension);
				assetPath = assetPath.concat(MigratorConstants.DOT).concat(customExtension);
				// fileExtension = customExtension;
			} else {
				fileName = fileName.concat(MigratorConstants.DOT);
				assetPath = assetPath.concat(MigratorConstants.DOT);
			}
		}

		if (assetPath
				.equalsIgnoreCase("Creative Jobs/2018/FH18/FH18 Luxe Matte Liquid Lip/CREATIVE/NEW/Luxe/Design/Emails/R3 PSDs (From Marielle)/01 ProductLaunch 02 Luxe Liquid.psd")) {
			System.out.println("match1");
		}
		if (BusinessRulesUtil.assetsPathsSet != null && BusinessRulesUtil.assetsPathsSet.contains(assetPath)) {
			assetPath = assetPath.concat(MigratorConstants.DOT).concat(customExtension);
		}

		return assetPath;
	}

}
