package com.mindtree.transformer.factory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.core.service.IMigratorBusiness;
import com.mindtree.core.service.IRule;
import com.mindtree.core.service.IStorage;
import com.mindtree.core.service.ITransformer;
import com.mindtree.transformer.service.impl.AbstractTransformer;
import com.mindtree.core.service.IFilter;

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
        String className = brandsMappings.get("transformer."+transformationType);
 
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
        String className = brandsMappings.get("storage."+storageType);
 
        IStorage storage = null;
 
        try {
            if( className!=null) {
                Class cls = Class.forName(className);
                storage = (IStorage)cls.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
        	LOGGER.error("ApplicationFactory :getStorage : Error While getting storage:{}"+e);
        }
 
        return storage;
    }

	/**
	 * This method returns migrator instance based on brand name.
	 * @param migrator
	 * @return
	 */
	public static IMigratorBusiness getMigratorBusiness(String brand) {
        String className = brandsMappings.get("migrator."+brand);
 
        IMigratorBusiness migrator = null;
 
        try {
            if( className!=null) {
                Class cls = Class.forName(className);
                migrator = (IMigratorBusiness) cls.getDeclaredConstructor().newInstance();
                migrator.setFilters(getIFilters(brand));
                migrator.setRules(getIRules(brand));
                
                LOGGER.info("Migrator loaded : "+ className);
            }
        } catch (Exception e) {
        	LOGGER.error("ApplicationFactory :getMigratorBusiness : Error While getting migrator:{}"+e);
        }
 
        return migrator;
    }
	
	private static List<IFilter> getIFilters(String brand) {
		
		String filter = "filter."+brand+".";
		boolean loop = true; int i=1;
		List<IFilter> filterList = new ArrayList<IFilter>();
		
		while(loop) {
			String className = brandsMappings.get(filter + i++);
			if(className == null) {
				break;
			}
			 
	        try {
	        	IFilter filterCls = (IFilter) Class.forName(className).getDeclaredConstructor().newInstance();
				LOGGER.info("Adding filter : "+className);
				filterList.add(filterCls);
	        } catch (Exception e) {
	        	LOGGER.error("ApplicationFactory :getFilters : Error While getting filters:{}"+e);
	        }
			
		}
 
        return filterList;
		
	}

	private static List<IRule> getIRules(String brand) {
		
		String rule = "rule."+brand+".";
		int i=1;
		List<IRule> ruleList = new ArrayList<IRule>();
		
		while(true) {
			String className = brandsMappings.get(rule + i++);
			if(className == null) {
				break;
			}
			 
	        try {
	        	IRule ruleCls = (IRule) Class.forName(className).getDeclaredConstructor().newInstance();
	        	LOGGER.info("Adding Rule : "+className);
				ruleList.add(ruleCls);
	        } catch (Exception e) {
	        	LOGGER.error("ApplicationFactory :getRules : Error While getting rules :{}"+e);
	        }
			
		}
 
        return ruleList;
		
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
