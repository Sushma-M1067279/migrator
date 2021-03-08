package com.mindtree.transformer.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;
import com.mindtree.transformer.service.AppContext;
import com.mindtree.transformer.service.IStorage;
import com.mindtree.transformer.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;

public class BlobStorage extends AbstractStorage {
	
	static final Logger LOGGER = LoggerFactory.getLogger(BlobStorage.class);
	
	private CloudBlobClient blobClient;
	private CloudBlobContainer container;

	@Override
	public IStorage connect() throws MigratorServiceException {
		// TODO Auto-generated method stub
		String connString = "DefaultEndpointsProtocol=https;"
				+ "AccountName="+ appVars.storageAccountName
				+ ";AccountKey="+ appVars.storageKey;
		
		CloudStorageAccount account;
		try {
			account = CloudStorageAccount.parse(connString);
		
			blobClient = account.createCloudBlobClient();

	        // Container name must be lower case.
	        container = blobClient.getContainerReference(appVars.bucketName);
		} catch (InvalidKeyException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new MigratorServiceException("Error while initializing "+ e.getMessage());
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new MigratorServiceException("Error while initializing "+ e.getMessage());
		}
        return this;
	}

	@Override
	public String getFileContent(String path) {
		// TODO Auto-generated method stub
		File file = this.getFile(null, path);
		try {
			return FileUtils.readFileToString(file, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Errors in reading file content for "+ path);
		}
		return null;
	}

	@Override
	public File getFile(String folder, String fileName) {
		// TODO Auto-generated method stub
		File file = null;
		try {
			String fullPath = (folder == null)?fileName: folder + fileSeparator() + fileName;
			CloudBlockBlob blob = container.getBlockBlobReference(fullPath);
			file = File.createTempFile("azure", "");
			blob.downloadToFile(file.getAbsolutePath());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return file;
	}

	@Override
	public Map<String, Long> getFileSizes(String folder) {
		// TODO Auto-generated method stub
		Map<String, Long> fileSizes = new HashMap<String, Long>();
		try {

			CloudBlobDirectory dir = container.getDirectoryReference(folder);
			return getFileSizesRecur(dir, folder, fileSizes);
		} catch (URISyntaxException | StorageException e) {
			LOGGER.error("Error getting file sizes. " + e);			
			e.printStackTrace();
			return null;
		}

	}
	
	private Map<String, Long> getFileSizesRecur(CloudBlobDirectory dir, String folder,
						Map<String, Long> fileSizes) throws URISyntaxException, StorageException{
		
			Iterator it =  dir.listBlobs().iterator();
	
			while (it.hasNext()) {
				ListBlobItem blob = (ListBlobItem) it.next();
				if (blob instanceof CloudBlobDirectory) {
					CloudBlobDirectory dBlob = (CloudBlobDirectory) blob;
					String _folder = folder + fileSeparator() + getBlobName(dBlob.getUri(), false);
					getFileSizesRecur(dBlob, _folder, fileSizes);
				} else {
					CloudBlockBlob cBlob = (CloudBlockBlob) blob;
					BlobProperties props = cBlob.getProperties();
					String path = folder + fileSeparator() + getBlobName(blob.getUri(), true);
					LOGGER.info("Path :"+ path);
					LOGGER.info("File size :"+ props.getLength());
					fileSizes.put(path, props.getLength());
				}
			}

		return fileSizes;
	}
	
	private String getBlobName(URI uri, boolean isFile) {
		
		String[] paths = uri.getPath().split("\\/");
		if (isFile)
			return paths[paths.length-1];
		
		if (paths.length>1) 
			return (paths[paths.length-1].length()!=0)? paths[paths.length-1]:paths[paths.length-2];
		
		return "";
	}

	@Override
	public void replicateAsAEM(String brandPrefix, String src, String dst) {
		Properties prop = null;
		String srcFolder = null;
		String dstFolder = null;

		try {
			prop = AppContext.getAppConfig();
			srcFolder = prop.getProperty(brandPrefix + MigratorConstants.S3_SOURCE_BUCKET_NAME);
			dstFolder = prop.getProperty(brandPrefix + MigratorConstants.S3_DESTINATION_BUCKET_NAME);

			LOGGER.info("-------------Replication Start----------------");
			LOGGER.info("LocalStorage replicateAsAEM :srcFolder:{} - src:{}", srcFolder, src);
			LOGGER.info("LocalStorage replicateAsAEM :dstFolder:{} - dst:{}", dstFolder, dst);
			
			CloudBlockBlob srcBlob = container.getBlockBlobReference(src);
			CloudBlockBlob destBlob = container.getBlockBlobReference(dstFolder + fileSeparator() + dst);
			
			destBlob.startCopy(srcBlob);

		} catch (Exception e) {
			LOGGER.error("AppContext : replicateAsAEM : replication failed :Exception : {} : Src Key:{}", e,
					src);
		}
		LOGGER.info("-------------Replication End----------------");

	}

	@Override
	public void uploadToStore(File file, String reportName) {
		// TODO Auto-generated method stub
		String destFile = this.getReportFileName(reportName, false);

		LOGGER.info("Uploading a new object to azure from a file");
		if (file == null) {
			LOGGER.error("Source file to upload is null.");
			return;
		}
		
		try {
			CloudBlockBlob blob = container.getBlockBlobReference(destFile);
			blob.uploadFromFile(file.getAbsolutePath());
		} catch (URISyntaxException | StorageException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Errors while uploading to blob."+ e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Errors while uploading to blob."+ e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public Properties loadProperties() {
		if (container == null) {
			return null;
		}

		LOGGER.info("Config Bucket name : " + appVars.bucketName);
		String fileKey = appVars.configFolder + fileSeparator() + "config.properties";
		LOGGER.info("Config fileKey : " + fileKey);
		Properties prop = new Properties();
		try {
			CloudBlockBlob blob = container.getBlockBlobReference(fileKey);
			prop.load(blob.openInputStream());
	
			LOGGER.info("Propp size : " + prop.size());
		} catch ( IOException e) {
			LOGGER.error("Error loading file. " + e);
			return null;
		} catch (URISyntaxException | StorageException e) {
			LOGGER.error("Error loading file. " + e);			
			e.printStackTrace();
			return null;
		}
		
		return prop;
	}


	@Override
	public String getFileName(String path) {
		
		try {
			return getBlobName( container.getBlockBlobReference(path).getUri(), true);
		} catch (URISyntaxException | StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public char fileSeparator() {
		// TODO Auto-generated method stub
		return '/';
	}

}
