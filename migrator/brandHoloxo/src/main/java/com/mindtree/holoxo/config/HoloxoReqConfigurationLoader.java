package com.mindtree.holoxo.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.mindtree.transformer.service.AppContext;
import com.mindtree.transformer.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.MigrationUtil;
import com.mindtree.utils.helper.ReadExcel;

/**
 * @author M1032046
 *
 */
public class HoloxoReqConfigurationLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(HoloxoReqConfigurationLoader.class);

	public static final Map<String, Map<String, String>> assetTypeMap = new HashMap<>();
	public static final Map<String, Map<String, String>> categoryMap = new HashMap<>();
	public static final Map<String, Map<String, String>> countryMap = new HashMap<>();
	public static final Map<String, String> rightUsageMap = new HashMap<>();
	public static final Map<String, String> ethnicityMap = new HashMap<>();
	public static final Map<String, String> languageMap = new HashMap<>();
	public static final Map<String, String> regionMap = new HashMap<>();
	public static final Map<String, String> usageMap = new HashMap<>();
	public static final Map<String, String> ppsnCorrectionMap = new HashMap<>();
	public static final Map<String, String> shadeCorrectionMap = new HashMap<>();
	public static final Map<String, String> catAppCorrectionMap = new HashMap<>();
	public static final Map<String, String> retailCorrectionMap = new HashMap<>();
	public static Map<Integer, String> headersMap = new HashMap<>();
	
	public static final Map<String, Map<String, String>> genericMap = new HashMap<>();

	private static String brandAbbreviation = null;
	public static String AEM_Value = null;
	public static String GE_Value = null;

	public static int sheetIndex = 0;
	public static int cellIndex = 0;
	public static int rowIndex = 0;
	public static int maxCell = 0;
	
	/**
	 * Default Constructor
	 */
	public HoloxoReqConfigurationLoader() {
		super();
	}

	static {
		LOGGER.info("holoxoReqConfigurationLoader : Loading static contents : Start..................");
		Properties prop;
		FileInputStream file = null;
		try {
			prop = AppContext.getAppConfig();
			brandAbbreviation = "HX";
			LOGGER.info("holoxoReqConfigurationLoader : current brand "+brandAbbreviation);
			StringBuilder brandPrefix = MigrationUtil.prepareBrandPrefix(brandAbbreviation);
			String devMigrationConfigPath = AppContext.getAppConfig().getProperty(MigratorConstants.DEV_ASSET_MIG_CONFIG_PATH);
			String brandConfigFile = prop.getProperty(brandPrefix + "" + MigratorConstants.BRAND_REQ_CONFIGURATION_FILENAME);
		//	file = new FileInputStream(new File(brandConfigFile));
			//XSSFWorkbook workbook = new XSSFWorkbook(file);
			XSSFWorkbook workbook = ReadExcel.getExcelWorkbook(brandConfigFile, devMigrationConfigPath+"/"+brandAbbreviation);
			
			workbook.forEach(sheet -> {
				if (sheet.getSheetName().equals(sheet.getSheetName()))
					sheetIndex = workbook.getSheetIndex(sheet.getSheetName());
				try {
					loadHoloxoConfigurationMasterMappingMap(workbook, sheet.getSheetName(), sheetIndex);
				} catch (IOException e) {
					LOGGER.error(
							"CliniqueIsilonReqConfigurationLoader : Static block : Unable to load Brand Requirement Configurations:{}",
							e);
				}
			});
			LOGGER.info("CliniqueIsilonReqConfigurationLoader : Loading static contents : Completed..................");
		} catch (MigratorServiceException e) {
			LOGGER.error(
					"CliniqueIsilonReqConfigurationLoader : Static block : Unable to load Brand Requirement Configurations:{}",
					e);
		} finally {
			/*try {
				file.close();
			} catch (IOException e) {
				LOGGER.error(
						"CliniqueIsilonReqConfigurationLoader : Static block : Unable to Close Brand Requirement Configurations:{}",
						e);
			}*/
		}
		LOGGER.info("CliniqueIsilonReqConfigurationLoader : Loading static contents : End..................");
	}

	/**
	 * This method is to load configuration mappings rules as per brand's
	 * requirement.
	 * 
	 * @param sheetIndex
	 * @param workbook
	 * @param sheetname
	 * @throws IOException
	 */
	private static void loadHoloxoConfigurationMasterMappingMap(XSSFWorkbook workbook, String sheetName,
			int sheetIndex) throws IOException {
		XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
		DataFormatter dataFormatter = new DataFormatter();
		sheet.forEach(row -> {
			Map<String, String> aemMap = new HashMap<String, String>();
			row.forEach(cell -> {
				String cellValue = dataFormatter.formatCellValue(cell);
				rowIndex = cell.getRowIndex();
				cellIndex = cell.getColumnIndex();
				if (rowIndex == 0) {
					/**
					 * Read excel headers
					 */
					maxCell = row.getLastCellNum();
					headersMap.put(cellIndex, cellValue);
				} else {
					/**
					 * Read excel Data
					 */
					if (cellIndex == MigratorConstants.ZERO && headersMap.get(cellIndex).equals(MigratorConstants.KEY_NAME)){
						GE_Value = cellValue.toString();
					}
					else {
						if (cellIndex != MigratorConstants.ZERO && cellIndex < maxCell && !(cellValue.equals(""))) {
							AEM_Value = cellValue.toString();
							aemMap.put(headersMap.get(cellIndex), cellValue.toString());
						}
					}
				}
			});
			if (rowIndex != 0) {
				if (sheetName.equals(MigratorConstants.SHEET_NAME_ASSET_TYPE))
					assetTypeMap.put(GE_Value, aemMap);
				else if (sheetName.equals(MigratorConstants.USAGE))
					usageMap.put(GE_Value, AEM_Value);
				else if (sheetName.equals(MigratorConstants.SHEET_NAME_REGION))
					regionMap.put(GE_Value, AEM_Value);
				else if (sheetName.equals(MigratorConstants.COUNTRY))
					countryMap.put(GE_Value, aemMap);
				else if (sheetName.equals(MigratorConstants.SHEET_NAME_LANGUAGE))
					languageMap.put(GE_Value, AEM_Value);
				else if (sheetName.equals(MigratorConstants.CATEGORY))
					categoryMap.put(GE_Value, aemMap);
				else if (sheetName.equals(MigratorConstants.SHEET_NAME_ETHINICITY))
					ethnicityMap.put(GE_Value, AEM_Value);
				else if (sheetName.equals(MigratorConstants.SHEET_NAME_RIGHTS_USAGE))
					rightUsageMap.put(GE_Value, AEM_Value);
				else if (sheetName.equals(MigratorConstants.SHEET_NAME_PPSN))
					ppsnCorrectionMap.put(GE_Value, AEM_Value);
				else if (sheetName.equals(MigratorConstants.SHADE))
					shadeCorrectionMap.put(GE_Value, AEM_Value);
				else if (sheetName.equals(MigratorConstants.SHEET_NAME_CATAPP))
					catAppCorrectionMap.put(GE_Value, AEM_Value);
				else if (sheetName.equals(MigratorConstants.RETAILER))
					retailCorrectionMap.put(GE_Value, AEM_Value);
				
			}
		});
		
		genericMap.put(MigratorConstants.REGION, regionMap);
		genericMap.put(MigratorConstants.LANGUAGE, languageMap);
		genericMap.put(MigratorConstants.PHOTOGRAPHER_RIGHTS_USAGE, rightUsageMap);
		genericMap.put(MigratorConstants.MODEL_ETHNICITY, ethnicityMap);
		genericMap.put(MigratorConstants.USAGE, usageMap);
		genericMap.put(MigratorConstants.PPSN, ppsnCorrectionMap);
		genericMap.put(MigratorConstants.SHADE, shadeCorrectionMap);
		genericMap.put(MigratorConstants.CATEGORY, catAppCorrectionMap);
		genericMap.put(MigratorConstants.RETAILER, retailCorrectionMap);
	}
}

