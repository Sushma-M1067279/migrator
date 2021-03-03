package com.mindtree.transformer.aws;

public class TransformerResponse {
	
	private boolean isSuccess;

	
	public TransformerResponse(boolean isSuccess) {
		super();
		this.isSuccess = isSuccess;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	

}
