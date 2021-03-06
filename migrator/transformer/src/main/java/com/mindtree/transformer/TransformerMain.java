package com.mindtree.transformer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.transformer.factory.ApplicationFactory;
import com.mindtree.core.service.AppContext;
import com.mindtree.core.service.IStorage;
import com.mindtree.core.service.ITransformer;

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
		
		if(! transformerApp.init(args)) {
			LOGGER.error("TransformMain : main : Errrors while initiating the application.");
			return;
		}

		if (args != null && args.length > 0) {

			String brandCode = args[0];
			String transformationType = args[1];
			String instanceNumber = args[2];
			
			boolean result = transformerApp.execute(transformationType, brandCode, instanceNumber);
			
			if(result) {
				LOGGER.info(transformerApp.getSummary());
			} else {
				LOGGER.error("Something went wrong. Redo migration.");
			}

		}

	}

}