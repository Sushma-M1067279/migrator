package com.mindtree.validator.service;

import com.mindtree.utils.exception.MigratorServiceException;
import com.mindtree.validator.model.AemQueryBuilderResponse;


public interface IValidatorService {

	/**
	 * @param brandAbbreviation 
	 * 
	 */
	void validateUploadsToAEM(String sourceAbbreviation);

	/**
	 * @param validationConfiguration
	 * @param offset
	 * @return
	 * @throws ElcServiceException
	 */
	AemQueryBuilderResponse getAllAEMAssets(long offset) throws MigratorServiceException;

	/**
	 * 
	 */
	void createReportFromAem();

}
