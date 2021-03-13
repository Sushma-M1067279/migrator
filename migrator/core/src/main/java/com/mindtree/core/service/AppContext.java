package com.mindtree.core.service;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is Migration Utility classes. This will perform all common operations
 * which can be used other utility.
 * 
 * @author M1032046
 *
 */
public class AppContext {
	
	public static class AppVariables {

		public final String bucketName;
		public final String configFolder;
		public final String storageKey;
		public final String storageSecret;
		public final String storageType;
		public final String storageAccountName;

		AppVariables(String v1, String v2, String v3, String v4, String v5, String v6){
			this.bucketName = v1;
			this.configFolder = v2;
			this.storageKey = v3;
			this.storageSecret = v4;
			this.storageType = v5;
			this.storageAccountName = v6;
		}
	}
	
	private static AppVariables appVariables;
	private static IStorage storage;


	/**
	 * @return the storage
	 */
	public static IStorage getStorage() {
		return storage;
	}

	/**
	 * @param storage the storage to set
	 */
	public static void setStorage(IStorage storage) {
		AppContext.storage = storage;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(AppContext.class);

	private static Properties appConfig = null;
	

	public static Properties getAppConfig() {
		return appConfig;
	}

	/**
	 * @param prop the prop to set
	 */
	public static void setAppConfig(Properties appConfig) {
		AppContext.appConfig = appConfig;
	}

	private AppContext() {
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
				String awsKey = (null != System.getenv("storage_key"))?
						System.getenv("storage_key"):System.getProperty("aws_key");
				String awsSecret = (null != System.getenv("storage_secret"))?
						System.getenv("storage_secret"):System.getProperty("aws_secret");
				String storageType = (null != System.getenv("storage_type"))?
						System.getenv("storage_type"):System.getProperty("storage_type");
				String storageAcName = (null != System.getenv("storage_account_name"))?
						System.getenv("storage_account_name"):System.getProperty("storage_account_name");
			
//				if (storageType == null) { 
//					storageType = "local"; 
//				}
				
//				if (configBucketName == null || configFolder == null || awsKey == null || awsSecret == null ) {
//					LOGGER.error("Unable to find environment variables");
//					return false;
//				}
				appVariables = new AppVariables(configBucketName, configFolder, awsKey, 
						awsSecret, storageType, storageAcName);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to find environment variables ", e);
			return false;
		}
		return true;
	}


}
