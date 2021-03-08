package com.mindtree.transformer.factory;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.transformer.service.IStorage;
import com.mindtree.transformer.service.ITransformer;
import com.mindtree.transformer.service.impl.AbstractTransformer;

/**
 * @author M1032046
 *
 */
public class ApplicationFactory {
	
	static final Logger LOGGER = LoggerFactory.getLogger(ApplicationFactory.class);
	private static final String TRANSFORMER_CONFIGURATION = "transformer";
    private static Map<String, String> brandsMappings = new HashMap<String, String>();
 
    
    static {
        try {
        	loadBrandsMappings();
        } catch (Exception e) {
        	LOGGER.error("TransformationFactory :static block : Error While loadBrandsMappings:{} "+e);
        }
    }
 
    
    /**
	 * The constructor.
	 */
	private ApplicationFactory() {
		super();
	}

	/**
	 * This method returns Transformer instance based on Transformer type.
	 * @param transformationType
	 * @return
	 */
	public static AbstractTransformer getTransformer(String transformationType) {
        String className = brandsMappings.get(transformationType);
 
        AbstractTransformer transformer = null;
 
        try {
            if( className!=null) {
                Class cls = Class.forName(className);
                transformer = (AbstractTransformer) cls.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
        	LOGGER.error("ApplicationFactory :getTransformer : Error While getting bean:{}"+e);
        }
 
        return transformer;
    }
	
	/**
	 * This method returns Storage instance based on storage type.
	 * @param storageType
	 * @return
	 */
	public static IStorage getStorage(String storageType) {
        String className = brandsMappings.get(storageType);
 
        IStorage storage = null;
 
        try {
            if( className!=null) {
                Class cls = Class.forName(className);
                storage = (IStorage)cls.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
        	LOGGER.error("ApplicationFactory :getStorage : Error While getting bean:{}"+e);
        }
 
        return storage;
    }
 
	/**
	 * Loads Transformer configurations from property file.
	 */
    private static void loadBrandsMappings() {
        ResourceBundle rb = ResourceBundle.getBundle(TRANSFORMER_CONFIGURATION, Locale.getDefault());
        for (Enumeration e = rb.getKeys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            brandsMappings.put(key, rb.getString(key));
        }
    }

}
