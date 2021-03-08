/**
 * 
 */
package com.mindtree.transformer.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.CopyPartRequest;
import com.amazonaws.services.s3.model.CopyPartResult;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mindtree.transformer.TransformerApp;
import com.mindtree.transformer.service.AppContext;
import com.mindtree.transformer.service.IStorage;
import com.mindtree.transformer.service.MigratorServiceException;
import com.mindtree.transformer.service.AppContext.AppVariables;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.BusinessRulesUtil;
import com.mindtree.utils.helper.MigrationUtil;

/**
 * @author M1003467
 *
 */
public class S3Storage extends AbstractStorage{
	
	static final Logger LOGGER = LoggerFactory.getLogger(S3Storage.class);
	
	private AmazonS3 s3Client;

	@Override
	public IStorage connect() throws MigratorServiceException {
		// TODO Auto-generated method stub
		try {
			if (null == s3Client) {
				
				AWSCredentials credentials = new BasicAWSCredentials(appVars.storageKey, appVars.storageSecret);
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
	public String getFileContent(String path) {
		LOGGER.info("S3Storage: getFileContent :XMP path/key:{}", path);
		
		return s3Client.getObjectAsString(appVars.bucketName, path);
	}

	@Override
	public File getFile(String folder, String fileName) {
		File file = null;
		String devMigrationBucketName = "";
		try {

			devMigrationBucketName = AppContext.getAppConfig().getProperty(
					"migrator.dev.asset.migration.bucket.name");
			S3Object s3object = s3Client.getObject(new GetObjectRequest(devMigrationBucketName, 
					folder + fileSeparator() + fileName));
			try (InputStream inputStream = s3object.getObjectContent()) {
				file = File.createTempFile("s3test", "");
				try (FileOutputStream outputStream = new FileOutputStream(file)) {
					int read;
					byte[] bytes = new byte[1024];
					while ((read = inputStream.read(bytes)) != -1) {
						outputStream.write(bytes, 0, read);
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	@Override
	public Map<String, Long> getFileSizes(String folder) {


		ListObjectsRequest listObjectsRequest = null;
		
		String bucketName = appVars.bucketName;
		LOGGER.info("S3Storage : getFileSizeFromS3 : Getting Assets from S3 : bucketName :{} Folder key:{}",
				bucketName, folder);
		
		if (folder != null && !folder.isEmpty()) {
			listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(folder + "/").withEncodingType("url");;
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
	public void replicateAsAEM(String brandPrefix, String src, String dst) {
		// TODO Auto-generated method stub
		Properties prop = null;
		String srcFolder = null;
		String dstFolder = null;
		String mimeType = null;
		try {
			prop = AppContext.getAppConfig();
			srcFolder = prop.getProperty(brandPrefix + MigratorConstants.S3_SOURCE_BUCKET_NAME);
			dstFolder = prop.getProperty(brandPrefix + MigratorConstants.S3_DESTINATION_BUCKET_NAME);

			LOGGER.info("-------------S3 Replication Start----------------");
			LOGGER.info("AppContext replicateS3AsAEM :srcBucket:{} - src:{}", srcFolder, src);
			LOGGER.info("AppContext replicateS3AsAEM :dstBucket:{} - dst:{}", dstFolder, dst);

			String fileExtension = MigrationUtil.getFileExtension(src);

			if (!fileExtension.isEmpty() && BusinessRulesUtil.MimeTypeMap != null
					&& BusinessRulesUtil.MimeTypeMap.size() > 0
					&& BusinessRulesUtil.MimeTypeMap.containsKey(fileExtension.toLowerCase())) {
				mimeType = BusinessRulesUtil.MimeTypeMap.get(fileExtension.toLowerCase());
			}

			s3MultiPartUpload(srcFolder, dstFolder, src, dst, mimeType);
		} catch (AmazonS3Exception ase) {
			LOGGER.error(
					"AppContext : replicateS3AsAEM : S3 replication failed :AmazonS3Exception : {} : Src Key:{}",
					ase, src);
			src = trySecondAttemptToUpload(src, dst, srcFolder, dstFolder, mimeType);
		} catch (AmazonServiceException ase) {
			LOGGER.error(
					"AppContext : replicateS3AsAEM : S3 replication failed :AmazonServiceException : {}: Src Key:{}",
					ase, src);
		} catch (AmazonClientException ace) {
			LOGGER.error(
					"AppContext : replicateS3AsAEM : S3 replication failed :AmazonClientException : {} : Src Key:{}",
					ace, src);
		} catch (Exception e) {
			LOGGER.error("AppContext : replicateS3AsAEM : S3 replication failed :Exception : {} : Src Key:{}", e,
					src);
		}
		LOGGER.info("-------------S3 Replication End----------------");

	}

	@Override
	public void uploadToStore(File file, String migrationCSVReportName) {
		Format formatter = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
		String dateString = formatter.format(new Date());

		String devMigrationBucketName = appVars.bucketName;
		String devMigrationReportPath = AppContext.getAppConfig().getProperty(
				"migrator.asset.migration.report.path");

		LOGGER.info("Uploading a new object to S3 from a file\n");
		if (file != null) {
			String fileName = migrationCSVReportName.split("\\.")[0];
			String extn = migrationCSVReportName.split("\\.")[1];

			PutObjectResult res = s3Client.putObject(new PutObjectRequest(devMigrationBucketName, devMigrationReportPath
					+ "/" + fileName.concat("." + dateString).concat("." + extn), file));
			LOGGER.info("PutObjectResult res:{}", res);
		}
	}

	@Override
	public Properties loadProperties() {
		if (s3Client == null) {
			return null;
		}
		// TODO Auto-generated method stub
		LOGGER.info("Config Bucket name : " + appVars.bucketName);
		String fileKey = appVars.configFolder + "/" + "config.properties";
		LOGGER.info("Config fileKey : " + fileKey);
		Properties prop = new Properties();
		S3Object s3object = s3Client.getObject(new GetObjectRequest(appVars.bucketName, fileKey));
		try (S3ObjectInputStream s3ObjectInputStream = s3object.getObjectContent()) {
			prop.load(s3ObjectInputStream);
			LOGGER.info("Propp size : " + prop.size());
		} catch ( IOException e) {
			LOGGER.error("Error loading file. " + e);
			return null;
		}
		return prop;
	}

	private void s3MultiPartUpload(String sourceFolder, String targetFolder, String sourceFile,
			String targetFile, String mimeType) {
		List<CopyPartResult> copyResponses = new ArrayList<CopyPartResult>();

		ObjectMetadata metadata = new ObjectMetadata();
		if (mimeType != null) {
			metadata.setContentType(mimeType);
		}

		InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(targetFolder,
				targetFile, metadata);

		InitiateMultipartUploadResult initResult = s3Client.initiateMultipartUpload(initiateRequest);

		// Get object size.
		GetObjectMetadataRequest metadataRequest = new GetObjectMetadataRequest(sourceFolder, sourceFile);

		ObjectMetadata metadataResult = s3Client.getObjectMetadata(metadataRequest);
		long objectSize = metadataResult.getContentLength(); // in bytes

		// Copy parts.
		long partSize = 4096 * (long) Math.pow(2.0, 20.0); // 4 GB

		long bytePosition = 0;

		LOGGER.info("AppContext replicateS3AsAEM s3MultiPartUpload :src : {}  -  objectSize:{}", sourceFile,
				objectSize);
		for (int i = 1; bytePosition < objectSize; i++) {
			CopyPartRequest copyRequest = new CopyPartRequest()
					.withDestinationBucketName(targetFolder)
					.withDestinationKey(targetFile)
					.withSourceBucketName(sourceFolder)
					.withSourceKey(sourceFile)
					.withUploadId(initResult.getUploadId())
					.withFirstByte(bytePosition)
					.withLastByte(
							bytePosition + partSize - 1 >= objectSize ? objectSize - 1 : bytePosition + partSize - 1)
					.withPartNumber(i);

			copyResponses.add(s3Client.copyPart(copyRequest));
			bytePosition += partSize;

		}
		CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(targetFolder,
				targetFile, initResult.getUploadId(), GetETags(copyResponses));
		CompleteMultipartUploadResult completeUploadResponse = s3Client.completeMultipartUpload(completeRequest);
		LOGGER.info("AppContext replicateS3AsAEM s3MultiPartUpload :CopyObjectResult:{}", completeUploadResponse);
	}
	
	private String trySecondAttemptToUpload(String src, String dst, String srcFolder, String dstFolder, String mimeType) {
		try {
			String[] filename = src.split("\\.");
			if (filename.length > 1) {
				if (isLowerCase(filename[1])) {
					src = src.toUpperCase();
				} else {
					src = src.toLowerCase();
				}
				s3MultiPartUpload(srcFolder, dstFolder, src, dst, mimeType);
			}

		} catch (Exception e) {
			LOGGER.error(
					"AppContext : replicateS3AsAEM : S3 replication failed :Exception(2nd Attempt) : {} : Src Key:{}",
					e, src);
		}
		return src;
	}

	// Helper function that constructs ETags.
	private List<PartETag> GetETags(List<CopyPartResult> responses) {
		List<PartETag> etags = new ArrayList<PartETag>();
		for (CopyPartResult response : responses) {
			etags.add(new PartETag(response.getPartNumber(), response.getETag()));
		}
		return etags;
	}

	private boolean isLowerCase(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (Character.isAlphabetic(s.charAt(i)) && !Character.isLowerCase(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getFileName(String path) {
		return this.getName(path, '/');
	}

	@Override
	public char fileSeparator() {
		// TODO Auto-generated method stub
		return '/';
	}


}
