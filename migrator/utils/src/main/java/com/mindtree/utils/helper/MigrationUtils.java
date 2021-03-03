package com.mindtree.utils.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.internal.xmp.XMPException;
import com.adobe.internal.xmp.XMPMeta;
import com.adobe.internal.xmp.XMPMetaFactory;
import com.adobe.internal.xmp.impl.XMPIteratorImpl;
import com.adobe.internal.xmp.properties.XMPPropertyInfo;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.exception.MigratorServiceException;

/**
 * This is Migration Utility classes. This will perform all common operations
 * which can be used other utility.
 * 
 * @author M1032046
 *
 */
public class MigrationUtils {
	
	public static class AppVariables {

		public final String configBucketName;
		public final String configFolder;
		public final String awsKey;
		public final String awsSecret;
		public final String storageType;

		AppVariables(String v1, String v2, String v3, String v4, String v5){
			this.configBucketName = v1;
			this.configFolder = v2;
			this.awsKey = v3;
			this.awsSecret = v4;
			this.storageType = v5;
		}
	}
	
	private static AppVariables appVariables;

	private static AWSCredentialsProvider awsCredential;
	private static ClientConfiguration awsClientConfig;
	private static Object storageClient;


	private static final Logger LOGGER = LoggerFactory.getLogger(MigrationUtils.class);
	private static final Logger EXCEPTION_LOGGER = LoggerFactory.getLogger(MigratorServiceException.class);

	private static Properties prop = null;
	
	/**
	 * @return the storageClient
	 */
	public static Object getStorageClient() {
		return storageClient;
	}

	/**
	 * @param storageClient the storageClient to set
	 */
	public static void setStorageClient(Object storageClient) {
		MigrationUtils.storageClient = storageClient;
	}

	/**
	 * @return the prop
	 */
	public static Properties getProp() {
		return prop;
	}

	/**
	 * @param prop the prop to set
	 */
	public static void setProp(Properties prop) {
		MigrationUtils.prop = prop;
	}

	private MigrationUtils() {
	}

	/**
	 * @return the awsCredentials
	 */
	public static AWSCredentialsProvider getAwsCredential() {
		return awsCredential;
	}

	/**
	 * @return the awsClientConfiguration
	 */
	public static ClientConfiguration getAwsClientConfig() {
		return awsClientConfig;
	}


	/**
	 * @return the appVariables
	 */
	public static AppVariables getAppVariables() {
		return appVariables;
	}
	
	


	/**
	 * @return the config
	 */
	public static boolean initializeConfig() {

		return readSystemVariables();

	}

	private static boolean readSystemVariables() {

		LOGGER.info("Reading system variables");

		try {
			if (appVariables == null) {
				String configBucketName = (null != System.getenv("config_bucket"))?
						System.getenv("config_bucket"):System.getProperty("config_bucket"); 
				String configFolder = (null != System.getenv("config_folder"))?
						System.getenv("config_folder"):System.getProperty("config_folder");
				String awsKey = (null != System.getenv("aws_key"))?
						System.getenv("aws_key"):System.getProperty("aws_key");
				String awsSecret = (null != System.getenv("aws_secret"))?
						System.getenv("aws_secret"):System.getProperty("aws_secret");
				String storageType = (null != System.getenv("storage_type"))?
						System.getenv("storage_type"):System.getProperty("storage_type");
			
				if (storageType == null) { 
					storageType = "s3"; 
				}
				
				if (configBucketName == null || configFolder == null || awsKey == null || awsSecret == null ) {
					LOGGER.error("Unable to find environment variables");
					return false;
				}
				appVariables = new AppVariables(configBucketName, configFolder, awsKey, awsSecret, storageType);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to find environment variables ", e);
			return false;
		}
		return true;
	}


	public static String generateAssetId(String brand, String instance, int count) {
		StringBuilder assetId = new StringBuilder().append(System.currentTimeMillis()).append(MigratorConstants.HYPHEN);
		assetId.append(brand).append(MigratorConstants.HYPHEN).append(instance).append(MigratorConstants.HYPHEN)
				.append(count);
		return assetId.toString();
	}

	public static Properties getPropValues() throws MigratorServiceException {
		return prop;
	}

	static AWSCredentialsProvider getAWSCredentials() {
		return awsCredential;
	}

	static ClientConfiguration getAWSClientConfiguration() {
		if (null == awsClientConfig) {
			LOGGER.info("creating new AWS instance 2");
			awsClientConfig = new ClientConfiguration();

		}
		return awsClientConfig;

	}

//	public static AmazonS3 getAmazonS3Instance() throws MigratorServiceException {
//		if (! (storageClient instanceof AmazonS3)) {
//			throw new MigratorServiceException("S3 client not found");
//		}
//		return (AmazonS3) storageClient;
//	}

	/**
	 * Method to read data from Xmp file. Pass absolute path of file.
	 * 
	 * @param path
	 * @param masterMetadataMap
	 * @param csvExportFlow
	 * @return
	 * @throws MigratorServiceException
	 * @throws FileNotFoundException
	 * @throws XMPException
	 */
	public static Map<String, String> fetchDataFromXmp(String content) throws MigratorServiceException {
		// Map<String, String> assetMetadataMapFromXMP = new TreeMap<String,
		// String>();
		HashMap<String, String> xmpMetadataMap = null;
		try {
			xmpMetadataMap = createBrandMetadataMapFromXMP(content);
		} catch (AmazonServiceException e) {
			LOGGER.error("fetchDataFromXmp: AmazonServiceException occured while XMP file reading:{} ", e.getMessage());
//			EXCEPTION_LOGGER.error("Missing file in S3:{}", path);
		} catch (SdkClientException e) {
			LOGGER.error("fetchDataFromXmp: SdkClientException occured while XMP file reading:{} ", e.getMessage());
		} catch (XMPException e) {
			LOGGER.error("fetchDataFromXmp: XMPException occured while XMP file reading:{} ", e.getMessage());
		}

		return xmpMetadataMap;
	}

	/**
	 * This method is to read xmp files and prepare metadata map along with
	 * rules.
	 * 
	 * @param path
	 * @param masterMetadataMap
	 * @param assetMetadataMapFromXMP
	 * @param exportFlowFlag
	 * @throws SdkClientException
	 * @throws AmazonServiceException
	 * @throws MigratorServiceException
	 * @throws XMPException
	 */
	private static HashMap<String, String> createBrandMetadataMapFromXMP(String content) throws AmazonServiceException,
			SdkClientException, MigratorServiceException, XMPException {
		

		// Parse string data to XMP metadata
		XMPMeta xmpMetadatas = XMPMetaFactory.parseFromString(content);
		StringBuilder str = new StringBuilder();
		StringBuilder val = new StringBuilder();
		HashMap<String, String> xmpMetadataMap = new HashMap<String, String>();
		boolean flag = false;

		XMPIteratorImpl nsIter = (XMPIteratorImpl) xmpMetadatas.iterator();

		/**
		 * Read XMP file into Map.
		 */
		while (nsIter.hasNext()) {
			XMPPropertyInfo prop = (XMPPropertyInfo) nsIter.next();
			if (null != prop.getPath() && !prop.getOptions().isQualifier()
					&& !prop.getPath().contains(MigratorConstants.NODE_DOCUMENT_ANCESTORS)) {
				String dataType = null;
				if (null != prop.getNamespace() && null != prop.getOptions() && prop.getOptions().isArray()) {
					str = new StringBuilder();
					str.append(prop.getPath());
					val = new StringBuilder();
					flag = true;
				} else {
					if (null != prop.getPath() && str.length() > 1 && prop.getPath().contains(str) && flag) {
						dataType = MigratorConstants.TYPE_MULTI_STRING;
						val.append(MigratorConstants.SPECIAL_CHARACTER_PIPE);
					} else {
						str = new StringBuilder();
						val = new StringBuilder();
						flag = false;
						str.append(prop.getPath());

					}
					val.append(prop.getValue());
				}
				putValueIntoMap(str, val, xmpMetadataMap, dataType);
			}
		}

		return xmpMetadataMap;

	}

	/**
	 * This method is to put XMP key values into map.
	 * 
	 * @param str
	 * @param val
	 * @param xmpMap
	 */
	private static void putValueIntoMap(StringBuilder str, StringBuilder val, HashMap<String, String> xmpMap,
			String dataType) {
		String value = val.toString().startsWith(MigratorConstants.SPECIAL_CHARACTER_PIPE) ? val.toString()
				.replaceFirst(MigratorConstants.SPECIAL_CHARACTER_PIPE_SPLIT, "") : val.toString();

		if (str != null) {
			String keyMetadata = str.toString();
			if (keyMetadata != null && !keyMetadata.isEmpty() && keyMetadata.contains("/")) {
				String[] keys = keyMetadata.split("/");
				if (keys.length > 1) {
					keyMetadata = keys[1];
				}
			}
			if (dataType != null && keyMetadata != null) {
				xmpMap.put(keyMetadata.concat(dataType), value);
			} else {
				xmpMap.put(keyMetadata, value);
			}
		}

	}

	private static void manageColorLabel(Map<String, String> assetMetadataMapFromXMP, Entry<String, String> xmpMetadata) {
		String colorLabelCode = xmpMetadata.getValue();

		if (null != colorLabelCode && !colorLabelCode.isEmpty()) {
			Map<String, String> colorLabelMap = BusinessRulesUtil.colorLabelMap;
			if (colorLabelMap != null && colorLabelMap.get(colorLabelCode) != null) {
				assetMetadataMapFromXMP.put(xmpMetadata.getKey().concat(MigratorConstants.TYPE_STRING),
						colorLabelMap.get(colorLabelCode));
			}
		}

	}

	/**
	 * This method is to manage country as boost search tag.
	 * 
	 * @param assetMetadataMapFromXMP
	 * @param xmpMetadata
	 */
	private static void manageCountry(Map<String, String> assetMetadataMapFromXMP, Map.Entry<String, String> xmpMetadata) {
		String country = xmpMetadata.getValue();
		if (assetMetadataMapFromXMP.get(MigratorConstants.AEM_PROPERTY_BOOST_SEARCH) != null
				&& !assetMetadataMapFromXMP.get(MigratorConstants.AEM_PROPERTY_BOOST_SEARCH).isEmpty()) {
			country = assetMetadataMapFromXMP.get(MigratorConstants.AEM_PROPERTY_BOOST_SEARCH)
					.concat(MigratorConstants.SPECIAL_CHARACTER_PIPE).concat(country);
		}
		assetMetadataMapFromXMP.put(MigratorConstants.AEM_PROPERTY_BOOST_SEARCH, country);
	}

	/**
	 * This method is to handle label - Approve, select, Alt values.
	 * 
	 * @param assetMetadataMapFromXMP
	 * @param xmpMetadata
	 * @param masterMetadataHeader
	 */
	private static void manageLabel(Map<String, String> assetMetadataMapFromXMP, Map.Entry<String, String> xmpMetadata,
			String masterMetadataHeader) {
		String selectAltApprove = xmpMetadata.getValue();
		if (null != selectAltApprove && selectAltApprove.equalsIgnoreCase(MigratorConstants.XMP_VALUE_ALT)) {
			selectAltApprove = MigratorConstants.AEM_XMP_VALUE_SECOND;
		}
		assetMetadataMapFromXMP.put(masterMetadataHeader, MigrationUtils.encode(selectAltApprove));
	}


	/**
	 * This method is to calculate ratings from filename using count of plus
	 * signs.
	 * 
	 * @param map
	 */
	public static void extractRatingsFromFileName(Map<String, String> map) {
		if (null == map.get(MigratorConstants.AEM_XMP_FIELD_KILL)) {
			String fileName = map.get(MigratorConstants.COLUMN_FILE_NAME);
			if (null != fileName && fileName.startsWith(MigratorConstants.PLUS_SIGN)) {
				Integer rating = StringUtils.countMatches(fileName, MigratorConstants.PLUS_SIGN);
				map.put(MigratorConstants.AEM_XMP_FIELD_KILL, rating.toString());
			}
		}
	}

	public static String getFileExtension(String fileName) {
		if (fileName.lastIndexOf('.') != -1 && fileName.lastIndexOf('.') != 0)
			return fileName.substring(fileName.lastIndexOf('.') + 1);
		else
			return "";
	}

	/**
	 * This method is used to encode a value with UTF-8 standard.
	 * 
	 * @param value
	 * @return
	 */
	public static String encode(String value) {

		return value;
	}

	public static StringBuilder prepareBrandPrefix(String brandAbbreviation) {
		StringBuilder brandPrefix = new StringBuilder(MigratorConstants.MIGRATOR_ABBR);
		brandPrefix.append(brandAbbreviation);
		brandPrefix.append(MigratorConstants.DOT);
		return brandPrefix;
	}

	/**
	 * @return Date.
	 */
	static String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd");
		return sdf.format(new Date());
	}

	public String trimExtraSlash(String path) {
		if (!path.trim().replace("\\", "/").endsWith("/")) {
			path = path.concat("/");
		}
		return path;
	}

	/**
	 * Method to format bytes in human readable format
	 * 
	 * @param bytes
	 *            - the value in bytes
	 * @param digits
	 *            - number of decimals to be displayed
	 * @return human readable format string
	 */
	public static String format(double bytes, int digits) {
		String[] dictionary = { "bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
		int index = 0;
		for (index = 0; index < dictionary.length; index++) {
			if (bytes < 1024) {
				break;
			}
			bytes = bytes / 1024;
		}
		String format = "%.".concat(String.valueOf(digits)).concat("f");
		return String.format(format, bytes) + " " + dictionary[index];
	}

}
