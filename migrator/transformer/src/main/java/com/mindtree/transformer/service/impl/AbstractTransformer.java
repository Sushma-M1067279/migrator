/**
 * 
 */
package com.mindtree.transformer.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.core.service.IStorage;
import com.mindtree.core.service.ITransformer;

/**
 * @author AdobeDay2
 *
 */
public abstract class AbstractTransformer implements ITransformer {

	protected IStorage storage;
	
	protected Map<String, String> migratedAssetsMap = new HashMap<String, String>();
	protected Map<String, String> nonMigratedAssetsMap = new HashMap<String, String>();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransformer.class);
	
	public void setStorage(IStorage storage) {
		this.storage = storage;
	}
	
	public String getSummary() {
		
		JSONObject jObj = new JSONObject();
		jObj.put("migrated_asset_count", Integer.valueOf(migratedAssetsMap.size()));
		jObj.put("non_migrated_asset_count", Integer.valueOf(nonMigratedAssetsMap.size()));
		
		JSONArray array = new JSONArray();
		array.add(migratedAssetsMap);
		jObj.put("migrated_assets", array);
		array = new JSONArray();
		array.add(nonMigratedAssetsMap);
		jObj.put("non_migrated_assets", array);
		
		StringWriter out = new StringWriter();
		try {
			
			jObj.writeJSONString(out);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.error("Error while generating the summary.");
		}
		
		return out.toString();
	}
	
}
