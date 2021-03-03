package com.mindtree.transformer.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.mindtree.utils.exception.MigratorServiceException;

public interface IStorage {

	public IStorage connect() throws MigratorServiceException ;
	
	public String getFileContent(String bucketName, String path);

	public File getFile(String folder, String fileName);
	
	public Map<String, Long> getFileSizes(String bucketName, String folderKey);
	
	public void replicateAsAEM(StringBuilder brandPrefix, String src, String dst);
	
	public void uploadToStore(File file, String migrationCSVReportName);
	
	public Properties loadProperties();
	
	public Object getNativeClient();
	
}
