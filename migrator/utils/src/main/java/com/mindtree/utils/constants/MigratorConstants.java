package com.mindtree.utils.constants;


/**
 * @author M1028078
 *
 */
public final class MigratorConstants {
	
	
	//First column header in asset dump sheet, TBD : can be put to property file
	public static final String COLUMN_ASSET_ID = "assetId";
	
	public static final String PROXY_ENABLE="migrator.proxy.enable";

	public static final String BRAND_ASSET_DUMP_FILENAME = "brandAssetDumpFileName";

	public static final String BRAND_ASSET_DUMP_SHEETINDEX = "assetDumpSheetIndex";

	public static final String COLON = ":";
	
//	public static final String DEV_ASSET_MIG_CONFIG_PATH = "migrator.dev.asset.migration.config.path";
	
	public static final String CURRENT_BRAND_PROCESSING = "migrator.current.brand.procesing";


	public static final String HYPHEN = "-";

	public static final String BRAND = "brand";
	
	public static final String MASTER_NAME_SPACE = "master";

	public static final String SOURCE_ASSET_MIGRATION = "ASSET_MIIGRATION";

	public static final String ASSET_MIGRATION = "Asset_Migration";

	public static final String SOURCE = "migSource";

	public static final String MIGRATOR_ASSET_ID = "AssetID";
	
	public static final String MIGRATOR_ABBR = "migrator.";

	public static final Integer ZERO = 0;

	public static final String ZERO_STR = "0";

	public static final Integer ONE = 1;

	public static final Integer TWO = 2;

	public static final Integer THREE = 3;

	public static final long NUMBER_1024 = 1024L;

	public static final String LOCAL_PROXY_HOST = "172.22.218.218";

	public static final Integer LOCAL_PROXY_PORT = 8085;

	public static final String TOTAL_TIME_TAKEN_IN_SEC = " : Total Time Taken in seconds: ";

	public static final String XMP_TRANSFORMATION_PATH = "XMP.transformation.path";

	public static final String NOT_AVAILABLE = "NA";

	public static final String COLUMN_MASTER_METADATA = "Metadata_Master";

	public static final String PREFIX_METADATA = "Metadata_";

	public static final String COLUMN_FIELD_TYPE = "Type";

	public static final String COLUMN_AEM_PROPERTY = "AEM Properties";

	public static final int COLUMN_ASSETID = 0;

	public static final int COLUMN_FINAL_FILE_NAME = 3;

	public static final String COLUMN_KIND_ASSET = "asset";

	public static final String MIGRATOR_ABBREVIATION = "migrator.";

	public static final String DOT = ".";

	public static final String PATH_COLUMN = "path";

	public static final String BLUENOID_DESTINATION_MIGRATION_PATH_FOR_NON_PROGRAMS = "Bluenoid/Final/Migrated/";

	public static final String XMP_METADATA = "xmp_metadata";

	public static final String FILE_EXTENTION_XMP = "xmp";

	public static final String AEM_PROPERTY_XMPFILE = "xmpFile";

	public static final String COLUMN_KIND = "kind";
	
	public static final String COLUMN_KIND_RENDITION = "rendition";


	public static final String AEM_PROPERTY_RENDITIONS = "renditions";

	public static final String BLUENOID_DESTINATION_MIGRATION_PATH_FOR_PROGRAMS = "Bluenoid/Final/FY/PN/Migrated";

	public static final int COLUMN_KIND_INDEX = 2;

	

	public static final String ASSET_ELIGIBLE_FOR_MIGRATION = "Eligible_for_Migration :";

	public static final String ASSET_NOT_ELIGIBLE_FOR_MIGRATION = "Not_eligible_for_Migration :";

	public static final String MIGRATION_ASSET_ID = "mig:assetId";

	public static final String XMP_FIELD_RATING = "Rating";

	public static final String AEM_XMP_FIELD_KILL = "xmp:Rating";

	public static final String XMP_FIELD_LABEL = "Label";

	public static final String AEM_XMP_VALUE_SECOND = "Second";

	public static final String XMP_VALUE_ALT = "Alt";

	public static final String COLUMN_FILE_NAME = "file_name";
	
	public static final int COLUMN_FILE_NAME_INDEX = 0;


	public static final String PLUS_SIGN = "+";

	public static final String AEM_PROPERTY_BOOST_SEARCH_CSV = "dam:search_promote{{String : multi}}";

	public static final String AEM_PROPERTY_BOOST_SEARCH = "dam:search_promote";

	public static final String SPECIAL_CHARACTER_COMMA = ",";

	public static final String SPECIAL_CHARACTER_COLON = ":";

	public static final String SPECIAL_CHARACTER_PIPE = "|";

	public static final String SPECIAL_CHARACTER_PIPE_SPLIT = "\\|";

	public static final String SPECIAL_CHARACTER_CURLY_BRACES = "{{";

	public static final String SPECIAL_CHARACTER_SQUARE_BRACES = "[]";

	public static final String SPECIAL_CHARACTER_HASH = "##";

	public static final String SPECIAL_CHARACTER_DOLLER = "$$";

	public static final String SPECIAL_CHARACTER_SLASH = "/";

	public static final String SPECIAL_CHARACTER_SLASH_STRING = "/";

	public static final String S3_BUCKET_NAME = "la-mer";

	public static final String EXCEL_COLUMN_KEYWORDS = "Keywords";

	public static final String EXCEL_COLUMN_LOCATION = "Location";

	public static final String BRAND_USAGE_MAPPING_SHEETNAME = "migrator.BN.usagemapping.sheetname";

	public static final String USAGE_RANDOM_PACKSHOTS = "Product Shot|Packshot";

	public static final String USAGE_EDUCATION = "Education - All";

	public static final String USAGE_CONSUMER_EVENTS = "Consumer Events - All";

	public static final String USAGE_CRM = "CRM - All";

	public static final String USAGE_VISUAL_MARCHANDISE = "Visual Merchandising - All";

	public static final String TRAVEL_RETAIL = "Travel Retail - All";

	public static final String FOLDER_PROGRAMS = "Programs";

	public static final String FOLDER_DIGITAL = "Digital";

	public static final String FOLDER_IMAGES = "Images";

	public static final String CSV_EXPORT_FLOW = "CSV Export Flow";

	public static final String DB_EXPORT_FLOW = "DB Export Flow";

	public static final String CSV_COLUMN_ASSET_ID = "AssetID";

	public static final String RENDITIONS_TARGET_KEY = "renditions_target_path";

	public static final String RENDITIONS_TARGET_KEY_STR = "renditions_target_path{{ String }}";

//	public static final String S3_SOURCE_BUCKET_NAME = "S3.source.bucket.name";
	public static final String STORAGE_SOURCE_BUCKET_FOLDER = "storage.source.bucket.folder";


	public static final String STORAGE_DESTINATION_BUCKET_FOLDER = "storage.destination.bucket.folder";

	public static final String MIGRATION_CSV_REPORT_PATH = "migration.csv.report.path";

	public static final String MIGRATION_SUMMARY_REPORT_PATH = "miration.summary.report.path";

	public static final String MIGRATION_RENDITION_TARGET_PATH = "storage.renditions.target.path";

	public static final int COLUMN_ORIGINAL_FILE_NAME = 0;

	public static final String MIGRATOR_NAME_SPACE = "elc";

	public static final String SPECIAL_CHARACTER_UNDERSCORE = "_";

	public static final String CSV_IMPORTER_COLUMN_SRC_REL_PATH = "relSrcPath";

	public static final String AEM_NAME_SPACE_SHEETNAME = "migrator.BN.namespace.sheetname";

	public static final String BRAND_ASSET_DUMP_PROCESS_COUNT = "asset.process.count";

	public static final String TYPE_MULTI_STRING = "{{ String : multi }}";

	public static final CharSequence NODE_DOCUMENT_ANCESTORS = "photoshop:DocumentAncestors";

	public static final String MIGRATION_INTERRUPT_LAST_PROCESSED_ASSET_FILE_PATH = "migrator.asset.migration.failure.file.path";

	public static final String BRAND_MIGRATION_FAILURE_LAST_PROCESSED_ID = "asset.migration.failure.last.processed.assetId";

	public static final String DERIVATIVE_A = "DERIVATIVE_A";

	public static final String DERIVATIVE_NA = "DERIVATIVE_NA";

	public static final String ASSET_NA = "ASSET_NA";

	public static final String ASSET_A_METADATA_NA = "ASSET_A_METADATA_NA";

	public static final String ASSET_A_METADATA_PA = "ASSET_A_METADATA_PA";

	public static final String ASSET_A_METADATA_A_DERIV_NA = "ASSET_A_METADATA_A_DERIV_NA";

	public static final String ASSET_A_METADATA_NA_DERIV_A = "ASSET_A_METADATA_NA_DERIV_A";

	public static final String SUCCESS_ASSET_A_METADATA_A_DERIV_A = "ASSET_A_METADATA_A_DERIV_A";

	public static final String SUCCESS_ASSET_A_METADATA_A = "ASSET_A_METADATA_A";

	public static final String VALIDATION_STATUS = "validationStatus";

	public static final String VALIDATION_MSG = "validationMsg";

	public static final String ERROR_MSG_CELL_LENGTH = "Error: The length of cell contents (text) exeeded 32,767 characters. Please check Log for discrepancy.";

	public static final String VAL_MIS_FILES_IN_EXCEL = "validation-missing-files-in-excel";

	public static final String DC_TITLE = "dc:title{{ String : multi }}";

	public static final String XMP_FIELD_PHOTOSHOP_URGENCY = "Urgency";

	public static final String TYPE_STRING = "{{ String }}";

	public static final String METADATA_PREFIX_AUX = "aux";

	public static final String METADATA_PREFIX_PSAUX = "psAux";

	public static final String OPEN_FLOWET_BRACES = "{{";

	public static final CharSequence PATH_TEMP_TRANSFER = "temporary transfer";

	public static final String GET_AEM_ASSET_RESOURCE_URL = "/bin/assetQuery";

	public static final String ASSET = "asset";

	public static final String ASSET_PATH = "Asset Path";

	public static final String ASSET_ID_STR = "Asset Id";

	public static final String RENDITION_UPLOAD_STATUS = "Rendition Upload Status";

	public static final String TARGET_ASSET_ID = "Target Asset Id";

	public static final String RENDITION_PATH = "Rendition Path";

	public static final String RENDITION_SUMMARY_REPORT = "Rendition_Summary_Report";

	public static final String NON_MIGRATED_ASSETS_COUNT = "Non Migrated Assets Count";

	public static final String MIGRATED_ASSETS_COUNT = "Migrated Assets Count";

	public static final String RENDITION_COUNT = "Rendition Count";

	public static final String MIGRATION_SUMMARY_REPORT = "Migration_Summary_Report";

	public static final String MISSING_ASSETS_IN_EXCEL = "Missing Assets in Excel";

	public static final String ASSET_MIGRATION_DATA_TYPE = "ASSET_MIGRATION_DATA_TYPE";

	public static final String ASSET_MIGRATION_SUMMARY = "Asset_migration_summary_";

	public static final String AEM_ASSET_SUMMARY = "AEM_Asset_summary_";

	public static final String METADATA = "Metadata";

	public static final String METADATA_DATA_TYPE = "Metadata Data Type";

	public static final String XLSX = ".xlsx";

	public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

	public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy_MM_dd_HH_mm_ss";

	public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS_SSSZ = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public static final String DATE_FORMAT_EEE_MMM_DD_YYYY_HH_MM_SS = "EEE MMM dd yyyy HH:mm:ss 'GMT'z";

	public static final String MIGRATED_ASSETS = "Migrated_Assets";

	public static final String NON_MIGRATED_ASSETS = "Non_Migrated_Assets";

	public static final String JCR_CREATED = "jcr:created";

	public static final String XMP_CREATE_DATE = "xmp:CreateDate";

	public static final String XMP_METADATADATE = "xmp:MetadataDate";

	public static final String XMP_MODIFYDATE = "xmp:ModifyDate";

	public static final String EXIF_DATETIME_ORIGINAL = "exif:DateTimeOriginal";

	public static final String MIGRATOR_EXPIRATION_DATE = "expiration_date";

	public static final String PHOTOSHOP_DATECREATED = "photoshop:DateCreated";

	public static final String UNAVAILABLE_IN_AEM = "UNAVAILABLE_IN_AEM";

	public static final String UTF_8 = "UTF-8";

	public static final String AEM_DEFAULT_METADATA_TYPE_SHEETNAME = "migrator.metadata.datatype.sheetname";

	public static final String MIGRATION_STORAGE_REPLICATION_ON_OFF_FLAG = "storage.replication.on.off.flag";

	public static final String AEM_PROPERTY_CALENDAY_YEAR = "calendarSeasonYear{{String : multi}}";

	public static final String MIGRATION_FOLDER_CREATOR_REPORT_PATH = "migration.folder.creator.report.path";

	public static final String FILE_SEPARETOR = "\\/";

	public static final String TRANSFORMER = "TRANSFORMER UTIL";

	public static final String BRAND_BLUENOID = "Bluenoid";
	
	public static final String BRAND_BLUENOID_PREFIX = "BN";

	public static final String BRAND_HOLOXO = "Holoxo";

	public static final String BRAND_REQ_CONFIGURATION_FILENAME = "configuration.file";

	public static final String MIGRATOR_CURRENT_BRAND_PROCESSING = "migrator.current.brand.procesing";

	public static final String BRAND_FOLDER_MAPPING_SHEETNAME = "folder.mapping.sheetname";

	public static final String BRAND_METADATA_MAPPING_SHEETNAME = "metadata.mapping.sheetname";

	public static final String BRAND_FILETYPES_TO_MIGRATE_SHEETNAME = "filetypes.to.migrate.sheetname";

	public static final String BRAND_BLANK_EXTENSIONS_SHEETNAME = "blank.file.extensions.sheetname";


	public static final long SIZE_1MB = 1048576;


	public static final String METADATA_PROGRAM_NAME = "Program Name";

	public static final String OUTPUT_ASSET_METADATA_MAP="assetMetadataMap";
	public static final String OUTPUT_MIGRATED_ASSETS_MAP="migratedAssetsMap";
	public static final String OUTPUT_NON_MIGRATED_ASSETS_MAP ="nonMigratedAssetsMap";
	public static final String OUTPUT_MISSING_ASSETS ="missingAssets";

	public static final String MIGRATION_BATCH = "migration_batch";


	public static final String IGNORE_FOLDER_SPECIALTY_MULTI_CHANNEL = "SPECIALTY_MULTI_CHANNEL";

	public static final String FILTER_EMPTY_ASSET_ZERO_BYTE = "filter.empty.asset.zero.byte";

	public static final String FILTER_BLANK_ASSET_LESS_THAN_1MB = "filter.blank.asset.less.than.1MB";

	public static final String FILTER_ASSET_BY_EXTENSIONS = "filter.asset.by.extensions";

	public static final String APPLY_CUSTOM_EXTENSIONS = "apply.asset.custom.extensions";

	public static final String ON = "ON";

	public static final String PROGRAM_FOLDER_PATH_SHEETNAME = "program.folder.paths.sheetname";

	public static final String PROGRAM_SUB_FOLDER_RULE_PATH = "program.sub.folder.rule.paths";

	public static final Object PATH_KEY_SEARCH_TYPE = "Search Type";

	public static final String OPERATION_EQUALS = "equals";

	public static final String KEYWORD_BROW = "brow";

	public static final String KEYWORD_BROWN = "brown";

	public static final String EXCEL_COLUMN_DRIVE_PATH = "Drive Path";

	public static final String FISCAL_YEAR_2018 = "2018";

	// Validator

	public static final int FOUR = 4;

	public static final String RENDITION_MSG = "Rendition Status";

	public static final String RENDITION_A = "RENDITION_A";

	public static final String RENDITION_NA = "RENDITION_NA";

	public static final String AEM_RELATED = "Related Status";

	public static final int FIVE = 5;

	public static final String AEM_MIME_TYPE_SHEETNAME = "migrator.mimetype.sheetname";

	public static final String FILE_NAME = "filename";

	public static final String SPECIAL_CHARACTER_HYPHEN = "-";

	public static final String FOLDER = "folder";

	public static final String FILE_SEPARETOR_KEY = "file.separator";

	public static final String GLOBAL_EDIT = "GE";

	public static final String ISILON_SERVER = "IS";

	public static final String SPECIAL_CHARACTER_SINGLE_HASH = "#";

	public static final String K_FOLDER = "kFolder";

	public static final String TRAVEL_RETAIL_STRING = "Travel Retail";

	public static final String MM_DD_YYYY_HH_MM_A = "MM/dd/yyyy HH:MM a";

	public static final String MMM_DD_YYYY_HH_MM_A = "MMM dd yyyy HH:mma";

	public static final String YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	public static final String YYYY_MM_DD_HH_MM_SS = "yyyy:MM:dd HH:mm:ss";

	public static final String YYYY_MM_DD_T_HH_MM = "yyyy-MM-dd'T'HH:mm";

	public static final String YYYY_MM_DD_T_HH_MM_SS_SSS = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	public static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";

	public static final String BRAND_DESTINATION_PATH_FOR_PROGRAMS = "aem.destination.path.programs";

	public static final String BRAND_DESTINATION_PATH_FOR_NON_PROGRAMS = "aem.destination.path.nonprograms";

	public static final String BRAND_DESTINATION_TAXONOMY_FORMAT = "aem.taxonomy.format";

	public static final String MIGRATOR_BRAND = "brand{{String}}";

	public static final String KEYWORD_BROW_PATH = "/brow/";

	public static final String NO_EXT = "noExt";

	public static final String SHEET_NAME_REGION = "Region";

	public static final String SHEET_NAME_RIGHTS_USAGE = "RightsUsage";

	public static final String SHEET_NAME_ETHINICITY = "Ethnicity";

	public static final String SHEET_NAME_LANGUAGE = "Langauge";

	public static final String SHEET_NAME_ASSET_TYPE = "AssetType";

	public static final String KEY_NAME = "LegacyDAM";

	public static final String ASSET_TYPE = "asset_type";

	public static final String FINAL = "FINAL";

	public static final String WIP = "WIP";

	public static final String RAW = "RAW";

	public static final Object AEM = "AEM";

	public static final String REGION = "region";

	public static final String COUNTRY = "country";

	public static final String LANGUAGE = "language";

	public static final String PHOTOGRAPHER_RIGHTS_USAGE = "photographer_rights_usage";

	public static final String MODEL_ETHNICITY = "ethnicity";

	public static final String USAGE = "usage";

	public static final String CATEGORY = "category";

	public static final String CALENDAR_SEASON_YEAR = "calendar_season_year";

	public static final String PPSN = "product_program_story_name";

	public static final String PRODUCT_ICON = "Product Icon";

	public static final String BRAND_ASSET_MIGRATION_TYPE = "asset.migration.type";

	public static final String AEM_FOLDER_MAPPING = "FolderMapping";

	public static final String AEM_PROPERTY_REGION = "region{{ String : multi }} ";

	public static final String SHEET_NAME_PPSN = "PPSN";

	public static final String SHADE = "shade";

	public static final String SHEET_NAME_CATAPP = "CatApp";

	public static final String RETAILER = "retailer";

	public static final String AGENCY_ASSET_NO_METADATA = "agency_no_metadata";

	public static final Object AGENCY_TEMP_FOLDER_FOR_NO_METADATA_ASSETS = "Agency_Other_Assets";

	public static final String AGENCY = "Agency";

	public static final String SPECIAL_CHARACTER_COMMA_AND_SLASH = "[,\\/]";

	public static final String ASSET_STATUS_FINAL = "Final";



	public static final Object MIGRATOR_ISILON_PATH = null;


	public static final String EXCEL_READ_PATH = "D:\\Software\\ADF\\ADF_Metadata\\ADF_Assets_Metadata_Updated1.0.xlsx";

	public static final String SHEET_NAME = "ADF Brand Assets Metadata";

	public static final String ASSET_STATUS = "assetStatus{{ String }}";

	public static final String SUB_BRAND = "SubBrand";

	public static final String BRAND_MISSING = "Orphan(Brand)";

	public static final String CSY = "calendarSeasonYear{{ String : multi }}";

	public static final String CSY_MISSING = "Orphan(CSY)";

	public static final String PRODUCT_NAME = "productName{{ String }}";

	public static final String PRODUCT_MISSING_KEY = "Orphan(PN)";

	public static final String BRAND_OR_CAMPAIGN = "Brand_or_Campaign";

	public static final String FILE_NAME_STRING = "file_name_original";

	public static final String ASSETTYPE_STRING = "assetType{{ String }}";

	public static final String EXCEL_REPORT_BB = null;

	public static final String CONFIG_EXCEL_PATH = "D:\\Software\\test\\test_Metadata\\config_test_updated.xlsx";

	public static final String AEM_VALUE = "AEM_Value";

	public static final String SOURCE_VALUE = "Source_Value";

	public static final String CATEGORY_APPLICATION = "cq:tags{{ String : multi }}";

	public static final String AEM_PATH = "/content/dam";

	public static final String ABS_TARGET_PATH = "absTargetPath";

	public static final String BRAND_REQ_METADATA_FILENAME = "metadata.file";
	
	public static final String STATUS = "Asset Migration";

	public static final String SHEET_NAME_MD = "Sheet1";

	public static final String ORIGINS="ORIGINS";



	public static final String EXCEL_REPORT_DRIVE = "D:\\MediaFiles\\SubDrives\\Report\\DriveReport.xlsx";

	public static final String DRIVE_REPORT_FILE = "D:\\MultiThread\\Drive_Report_Excel.xlsx";

	public static final String EXCEL_Report_Result = "D:\\testExcel\\bobibrownDriveReport.xlsx";

	public static final String EXCEL_Report = "D:\\Files\\Report OF test\\S3Folder\\ReportOfExcel.xlsx";

	public static final String EXCEL_Report_S3 = "D:\\12Oct\\test_non_priorty\\test_s3_report.xlsx";
	// Validator

	
	public static final String AEM_PROPERTY_DESTINATION_PATHS = "absTargetPath";



	public static final String Sheet_Name_AssetType = "AssetType";

	public static final String Sheet_Name_Country = "Country";

	public static final String Sheet_Name_MD = "Sheet1";

	public static final String Key_Name = "GE";

	public static final String Key_Name_MF = "absTargetPath";


	public static final String EXCEL_READ_PATH_Missing = "D:\\Software\\AEMData.xlsx";

	public static final String CL_Configuration_Mapping = "D:\\Backup\\Holoxo\\Holoxo_Configuration.xlsx";

	public static final String Sheet_Name_Category = "Category";

	public static final String Sheet_Name_Usage = "Usage";

	public static final String Sheet_Name_Region = "Region";

	public static final String Sheet_Name_RightUsage = "RightsUsage";

	public static final String Sheet_Name_Ethincity = "Ethnicity";

	public static final String Sheet_Name_Language = "Langauge";
	
	public static final String Sheet_Name = "Brand Assets Metadata";

	public static final int EL = 11;

	public static final String File = "D:\\\\MultiThread\\\\input_Directories.txt";

	public static final String MIGRATION_FOLDER_PATH = "G:/";

	public static final double Excel_BreakPoint = 0;

	
	public static final String Excel_Output_Path = "D:\\Report\\test\\";
	
	public static final String EXCEL_MERGE_PATH="D:\\MediaFiles\\test\\ExcelWrite_demo.xlsx";
	
	public static final String EXCEL_WRITE_PATH="D:\\MediaFiles\\test\\ExcelRead_demo_copy.xlsx";
	

	public static final String Excel_Read = "D:\\testExcel\\testDriveReport.xlsx";

	
	public static final String Excel_Division = "_division_";
	
	public static final String Excel_Extension = ".xlsx";



	
	private MigratorConstants() {
	}
}
