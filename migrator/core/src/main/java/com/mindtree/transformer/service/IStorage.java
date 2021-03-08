package com.mindtree.transformer.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public interface IStorage {

	public IStorage connect() throws MigratorServiceException ;
	
	public String getFileContent(String path);

	public File getFile(String folder, String fileName);
	
	public Map<String, Long> getFileSizes( String folder);
	
	public void replicateAsAEM(String brandPrefix, String src, String dst);
	
	public void uploadToStore(File file, String reportName);
	
	public Properties loadProperties();
	
	public String getFileName(String path);
	
	public char fileSeparator();
	
}
