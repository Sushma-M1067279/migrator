package com.mindtree.transformer;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.transformer.factory.ApplicationFactory;
import com.mindtree.transformer.service.AbstractTransformer;
import com.mindtree.transformer.service.IStorage;
import com.mindtree.transformer.service.ITransformer;
import com.mindtree.utils.exception.MigratorServiceException;
import com.mindtree.utils.helper.MigrationUtils;
import com.mindtree.utils.helper.MigrationUtils.AppVariables;

public class TransformerApp {
	
	static final Logger LOGGER = LoggerFactory.getLogger(TransformerApp.class);
	private boolean initSuccess = false;
	public AppVariables appVariables;
	private IStorage appStorage;
	
	public boolean init() {
		initSuccess = MigrationUtils.initializeConfig();
		if (!initSuccess) {
			LOGGER.error("TransformerApp : init : Errors while initializing application.");
			return false;
		}
		appVariables = MigrationUtils.getAppVariables();
		appStorage = ApplicationFactory.getStorage(appVariables.storageType);
		
		try {
			appStorage = appStorage.connect();
			Properties props = appStorage.loadProperties();
			if (props == null) {throw new MigratorServiceException("");}
			
			MigrationUtils.setProp(props);
			MigrationUtils.setStorageClient(appStorage.getNativeClient());
			
		} catch (MigratorServiceException e) {
			LOGGER.error("TransformerApp : init : Errors while initializing application.");
			return false;			
		}
		
		return initSuccess;
	}

	public boolean execute(String transformationType, String brandCode, String instanceNumber) {
		if(!initSuccess) {
			LOGGER.error("TransformerApp : execute: Application is not initialized yet or failed during initiation.");
			return false;
		}
			
		AbstractTransformer transformer = (AbstractTransformer) ApplicationFactory.getTransformer(transformationType);

		LOGGER.info("TransformerApp : execute: The type of the transformer:{}", transformationType);

		if (transformer == null) {
			LOGGER.error("TransformerApp : execute : Couldn't find transformer. Please check transformer.properties");
			return false;
		}
		
		transformer.setStorage(appStorage);
		
		return transformer.transform( brandCode, instanceNumber);

	}
}
