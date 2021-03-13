package com.mindtree.core.service;

public class MigratorServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5009439244735379986L;

	public MigratorServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public MigratorServiceException(String message) {
		super(message);
	}
	
	public MigratorServiceException(Throwable cause) {
		super(cause);
	}

	
}
