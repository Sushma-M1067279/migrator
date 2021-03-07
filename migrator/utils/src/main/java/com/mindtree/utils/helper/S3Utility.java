package com.mindtree.utils.helper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mindtree.transformer.service.MigratorServiceException;

/**
 * @author M1032046
 *
 */
public class S3Utility {

	private S3Utility() {

	}

	private static final Logger LOGGER_UTIL = LoggerFactory.getLogger(S3Utility.class);
	static AmazonS3 s3 = null;

//	public static Map<String, Long> getFileSizeFromS3(String bucketName, String folderKey)
//			throws MigratorServiceException {
//
//		LOGGER_UTIL.info("S3Utility : getFileSizeFromS3 : Getting Assets from S3 : bucketName :{} Folder key:{}",
//				bucketName, folderKey);
//		s3 = AppContext.getAmazonS3Instance();
//
//		ListObjectsRequest listObjectsRequest = null;
//		if (folderKey != null && !folderKey.isEmpty()) {
//			listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(folderKey + "/").withEncodingType("url");;
//		} else {
//			listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withEncodingType("url");;
//
//		}
//
//		ObjectListing listing = s3.listObjects(listObjectsRequest);
//
//		List<S3ObjectSummary> summaries = listing.getObjectSummaries();
//		int count = 0;
//		while (listing.isTruncated()) {
//			listing = s3.listNextBatchOfObjects(listing);
//			count = count + listing.getObjectSummaries().size();
//			LOGGER_UTIL.info("s3 read :::: ", count);
//			summaries.addAll(listing.getObjectSummaries());
//		}
//
//		Map<String, Long> filesSizeMap = summaries.stream()
//				// convert list to stream
//				.filter(s -> !s.getKey().endsWith("/"))
//				// filter - ignore if file size is 0 bytes
//				.filter(s -> !s.getKey().endsWith(".DS_Store"))
//				.collect(Collectors.toMap(x -> x.getKey(), x -> x.getSize()));
//		LOGGER_UTIL.info("S3Utility : getFileSizeFromS3 : Getting Assets from S3 : Total assets:{}",
//				filesSizeMap.size());
//		return filesSizeMap;
//	}

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
		return String.format("%." + digits + "f", bytes) + " " + dictionary[index];
	}

}
