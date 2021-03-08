/**
 * 
 */
package com.mindtree.transformer.service.impl;

import com.mindtree.transformer.service.IStorage;
import com.mindtree.transformer.service.ITransformer;

/**
 * @author AdobeDay2
 *
 */
public abstract class AbstractTransformer implements ITransformer {

	protected IStorage storage;
	
	public void setStorage(IStorage storage) {
		this.storage = storage;
	}
	
}
