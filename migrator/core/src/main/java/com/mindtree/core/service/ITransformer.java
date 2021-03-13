package com.mindtree.core.service;



/**
 * @author M1032046
 *
 */
public interface ITransformer {
	
	/**
	 * Transforms the gicen set of input data into migration asset metadata.
	 * @param context
	 * @param brandAbbreviation
	 * @param instanceNumber 
	 */
	public boolean transform(/*AbstractApplicationContext context,*/
			String brandAbbreviation, String instanceNumber);

}
