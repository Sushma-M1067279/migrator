package com.mindtree.utils.helper;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.internal.xmp.XMPException;
import com.adobe.internal.xmp.XMPMeta;
import com.adobe.internal.xmp.XMPMetaFactory;
import com.adobe.internal.xmp.impl.XMPIteratorImpl;
import com.adobe.internal.xmp.properties.XMPPropertyInfo;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.mindtree.core.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;

public class MigrationUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MigrationUtil.class);
	
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
	
	public static StringBuilder prepareBrandPrefix(String brandAbbreviation) {
		StringBuilder brandPrefix = new StringBuilder(MigratorConstants.MIGRATOR_ABBR);
		brandPrefix.append(brandAbbreviation);
		brandPrefix.append(MigratorConstants.DOT);
		return brandPrefix;
	}
	
	public static String generateAssetId(String brand, String instance, int count) {
		StringBuilder assetId = new StringBuilder().append(System.currentTimeMillis()).append(MigratorConstants.HYPHEN);
		assetId.append(brand).append(MigratorConstants.HYPHEN).append(instance).append(MigratorConstants.HYPHEN)
				.append(count);
		return assetId.toString();
	}
	
	public static String getFileExtension(String fileName) {
		if (fileName.lastIndexOf('.') != -1 && fileName.lastIndexOf('.') != 0)
			return fileName.substring(fileName.lastIndexOf('.') + 1);
		else
			return "";
	}

	/**
	 * @return Date.
	 */
	public static String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd");
		return sdf.format(new Date());
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

}
