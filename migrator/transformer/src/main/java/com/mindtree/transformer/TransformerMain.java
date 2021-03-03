package com.mindtree.transformer;

import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.transformer.factory.ApplicationFactory;
import com.mindtree.transformer.service.ITransformer;
import com.mindtree.utils.helper.MigrationUtils;

/**
 * This is the entry point for the transformation process.
 * 
 * @author M1032046
 * 
 * 
 *
 */
public class TransformerMain {

	static final Logger LOGGER = LoggerFactory.getLogger(TransformerMain.class);

	/**
	 * This is the main method to execute Transformer utility to transform brand
	 * specific meta data to common meta data to be migrated. <br>
	 * Inputs: <br>
	 * brandCode - short for a brand (Holoxo - hx, Bluenoid - bn) <br>
	 * transformationType - metadata source (csv - extract metadata from csv and
	 * xmp, path - extract metadata from file path and rules )<br>
	 * instanceNumber - instance number
	 * 
	 * @param args
	 * 
	 * @throws IOException
	 * @throws EncryptedDocumentException
	 * @throws InvalidFormatException
	 */
	public static void main(String[] args) {
		LOGGER.info("Transformer Main : main : args:{}", args);

		TransformerApp transformerApp = new TransformerApp();
		
		if(! transformerApp.init()) {
			LOGGER.error("TransformMain : main : Errrors while initiating the application.");
			return;
		}
		
		if (args != null && args.length > 0) {

			String brandCode = args[0];
			String transformationType = args[1];
			String instanceNumber = args[2];
			
			transformerApp.execute(transformationType, brandCode, instanceNumber);

		}

	}

}