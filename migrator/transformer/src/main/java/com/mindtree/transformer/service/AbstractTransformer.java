/**
 * 
 */
package com.mindtree.transformer.service;

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
