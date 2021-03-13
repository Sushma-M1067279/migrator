package com.mindtree.core.service;


import java.util.List;

/**
 * @author M1032046
 *
 */
public interface IDataFileReader {

	/**
	 * Returns the row number processed till now.
	 * @return
	 */
	public int rowNum(); // current row number!

	/**
	 * This method returns the row data upto batch size specified.
	 * @param batchSize
	 * @return
	 * @throws MigratorServiceException 
	 * @throws Exception
	 */
	public List<String[]> readRows(int batchSize) throws MigratorServiceException;
	
	/**
	 * This method returns the row data upto batch size specified.
	 * @param batchSize
	 * @return
	 * @throws MigratorServiceException 
	 * @throws Exception
	 */
	public List<String[]> readRowsByOffset(int batchNumber, int batchSize) throws MigratorServiceException;
}
