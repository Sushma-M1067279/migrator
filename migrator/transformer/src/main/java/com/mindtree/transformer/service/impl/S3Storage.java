/**
 * 
 */
package com.mindtree.transformer.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mindtree.transformer.TransformerApp;
import com.mindtree.transformer.service.IStorage;
import com.mindtree.utils.exception.MigratorServiceException;
import com.mindtree.utils.helper.MigrationUtils;
import com.mindtree.utils.helper.MigrationUtils.AppVariables;

/**
 * @author M1003467
 *
 */
public class S3Storage implements IStorage {
	
	static final Logger LOGGER = LoggerFactory.getLogger(S3Storage.class);
	
	private AmazonS3 s3Client;

	private AppVariables appVars = MigrationUtils.getAppVariables();
	
	@Override
	public IStorage connect() throws MigratorServiceException {
		// TODO Auto-generated method stub
		try {
			if (null == s3Client) {
				
				AWSCredentials credentials = new BasicAWSCredentials(appVars.awsKey, appVars.awsSecret);
				LOGGER.info("creating new AWS instance");
				s3Client = AmazonS3ClientBuilder.standard().withClientConfiguration(new ClientConfiguration())
						.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.US_EAST_2)
						.build();
			}
		} catch (AmazonServiceException ase) {
			LOGGER.error("Caught an AmazonServiceException, which means your request made to Amazon S3, but was rejected with an error response.");
			LOGGER.error("Error Message: " + ase.getMessage() + ", HTTP Status Code: " + ase.getStatusCode()
					+ ", AWS Error Code:   " + ase.getErrorCode());
			throw new MigratorServiceException(
					"getAmazonS3Instance: request made to Amazon S3 was rejected with an error response: " + ase);
		} catch (AmazonClientException ace) {
			LOGGER.error("The client encountered an internal error while trying to communicate with S3, such as not being able to access the network. Error Message: "
					+ ace.getMessage());
			throw new MigratorServiceException(
					"getAmazonS3Instance: internal error while trying to communicate with S3 with an error response: "
							+ ace);
		}
		return this;
	}

	@Override
	public String getFileContent(String bucketName, String path) {
		LOGGER.info("S3Storage: getFileContent :XMP path/key:{}", path);
		
		return s3Client.getObjectAsString(bucketName, path);
	}

	@Override
	public File getFile(String folder, String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Long> getFileSizes(String bucketName, String folderKey) {
		LOGGER.info("S3Storage : getFileSizeFromS3 : Getting Assets from S3 : bucketName :{} Folder key:{}",
				bucketName, folderKey);

		ListObjectsRequest listObjectsRequest = null;
		if (folderKey != null && !folderKey.isEmpty()) {
			listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(folderKey + "/").withEncodingType("url");;
		} else {
			listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withEncodingType("url");;

		}

		ObjectListing listing = s3Client.listObjects(listObjectsRequest);

		List<S3ObjectSummary> summaries = listing.getObjectSummaries();
		int count = 0;
		while (listing.isTruncated()) {
			listing = s3Client.listNextBatchOfObjects(listing);
			count = count + listing.getObjectSummaries().size();
			LOGGER.info("s3 read :::: ", count);
			summaries.addAll(listing.getObjectSummaries());
		}

		Map<String, Long> filesSizeMap = summaries.stream()
				// convert list to stream
				.filter(s -> !s.getKey().endsWith("/"))
				// filter - ignore if file size is 0 bytes
				.filter(s -> !s.getKey().endsWith(".DS_Store"))
				.collect(Collectors.toMap(x -> x.getKey(), x -> x.getSize()));
		LOGGER.info("S3Storage : getFileSizes : Getting Assets from S3 : Total assets:{}",
				filesSizeMap.size());
		return filesSizeMap;
	}

	@Override
	public void replicateAsAEM(StringBuilder brandPrefix, String src, String dst) {
		// TODO Auto-generated method stub

	}

	@Override
	public void uploadToStore(File file, String migrationCSVReportName) {
		// TODO Auto-generated method stub

	}

	@Override
	public Properties loadProperties() {
		if (s3Client == null) {
			return null;
		}
		// TODO Auto-generated method stub
		LOGGER.info("Config Bucket name : " + appVars.configBucketName);
		String fileKey = appVars.configFolder + "/" + "config.properties";
		LOGGER.info("Config fileKey : " + fileKey);
		Properties prop = new Properties();
		S3Object s3object = s3Client.getObject(new GetObjectRequest(appVars.configBucketName, fileKey));
		try (S3ObjectInputStream s3ObjectInputStream = s3object.getObjectContent()) {
			prop.load(s3ObjectInputStream);
			LOGGER.info("Propp size : " + prop.size());
		} catch ( IOException e) {
			LOGGER.error("Error loading file. " + e);
			return null;
		}
		return prop;
	}

	@Override
	public Object getNativeClient() {
		// TODO Auto-generated method stub
		return s3Client;
	}

}
