/**
 * 
 */
package com.mindtree.transformer.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mindtree.core.service.AppContext;
import com.mindtree.core.service.IStorage;
import com.mindtree.core.service.MigratorServiceException;
import com.mindtree.core.service.AppContext.AppVariables;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.helper.BusinessRulesUtil;

/**
 * @author AdobeDay2
 *
 */
public class LocalStorage extends AbstractStorage {
	
	static final Logger LOGGER = LoggerFactory.getLogger(LocalStorage.class);

	@Override
	public IStorage connect() throws MigratorServiceException {
		File f = new File(appVars.bucketName);
		if(! f.exists()) {
			LOGGER.error("Given bucket names does not exist.");
			return null;
		}
		
		return this;
	}

	@Override
	public String getFileContent(String path) {
		// TODO Auto-generated method stub
		try {
			return FileUtils.readFileToString(new File(appVars.bucketName + fileSeparator() +path), "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Errors in reading file content for "+ path);
		}
		return null;
	}

	@Override
	public File getFile(String folder, String fileName) {
		// TODO Auto-generated method stub
		String AbsFile = appVars.bucketName + fileSeparator() + folder+ fileSeparator() + fileName;
		return new File(AbsFile);
	}

	@Override
	public Map<String, Long> getFileSizes(String sourceFolder) {
		
		String rootFolder = appVars.bucketName;
		LOGGER.info("LocalStorage : getFileSizes : Getting Assets from local: folder :{} path:{}",
				rootFolder, sourceFolder);

		Collection<File> files = FileUtils.listFiles(new File(rootFolder+ fileSeparator() +sourceFolder), null, true);
		
		Map<String, Long> filesSizeMap = new HashMap<String, Long>();
		
		Iterator<File> iter = files.iterator();
		while( iter.hasNext()) {
			File f = iter.next();
			String key = this.tuncateRoot(rootFolder, f.getAbsolutePath());
			filesSizeMap.put( key, f.length());
		}
		
		return filesSizeMap;

	}

	@Override
	public void replicateAsAEM(String brandPrefix, String src, String dst) {

		Properties prop = null;
		String srcFolder = null;
		String dstFolder = null;
		String mimeType = null;
		try {
			prop = AppContext.getAppConfig();
			srcFolder = prop.getProperty(brandPrefix + MigratorConstants.STORAGE_SOURCE_BUCKET_FOLDER);
			srcFolder = appVars.bucketName+ fileSeparator() + srcFolder;
			dstFolder = prop.getProperty(brandPrefix + MigratorConstants.STORAGE_DESTINATION_BUCKET_FOLDER);
			dstFolder = appVars.bucketName+ fileSeparator() + dstFolder;

			LOGGER.info("-------------Replication Start----------------");
			LOGGER.info("LocalStorage replicateAsAEM :srcBucket:{} - src:{}", srcFolder, src);
			LOGGER.info("LocalStorage replicateAsAEM :dstBucket:{} - dst:{}", dstFolder, dst);
			
			File srcFile = new File(appVars.bucketName + fileSeparator() + src);
			File destFile = new File(dstFolder + fileSeparator() + dst);
			destFile.getParentFile().mkdirs();
			
			FileUtils.copyFile(srcFile, destFile);

		} catch (Exception e) {
			LOGGER.error("AppContext : replicateAsAEM : replication failed :Exception : {} : Src Key:{}", e,
					src);
		}
		LOGGER.info("-------------Replication End----------------");

		
	}

	@Override
	public void uploadToStore(File file, String reportName) {

		String destName = this.getReportFileName(reportName, true);
		LOGGER.info("Uploading report file : "+ destName);
		
		try {
			File destFile = new File(destName);
			destFile.getParentFile().mkdirs();
			FileUtils.moveFile(file, destFile);
		} catch (IOException e) {
			LOGGER.error(" Errors while writing file to: "+ destName);
			e.printStackTrace();
		}

	}

	@Override
	public Properties loadProperties() {
		// TODO Auto-generated method stub
		LOGGER.info("Root folder name : " + appVars.bucketName);
		String configFile = appVars.bucketName+ fileSeparator()+ appVars.configFolder + fileSeparator() + "config.properties";
		
		Properties prop = new Properties();
		try (InputStream is = new FileInputStream(configFile)) {
			prop.load(is);
			LOGGER.info("Propp size : " + prop.size());
		} catch ( IOException e) {
			LOGGER.error("Error loading file. " + e);
			return null;
		}
		return prop;
	}

	@Override
	public String getFileName(String path) {
		return this.getName(path, fileSeparator());
	}
	
	@Override
	public char fileSeparator() {
		// TODO Auto-generated method stub
		return File.separatorChar;
	}
	
	private String tuncateRoot(String root, String absFilePath) {
		File rootF = new File(root);
		String rootPath = rootF.getAbsolutePath();
		return absFilePath.substring(rootPath.length()+1);
	}

}
