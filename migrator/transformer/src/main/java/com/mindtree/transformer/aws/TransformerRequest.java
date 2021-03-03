package com.mindtree.transformer.aws;

public class TransformerRequest {
	
	private String brandCode;
	private String sourceType;
	private String instanceNumber;
	private String configPath;
	
	public TransformerRequest() {
	}
	
	public String getBrandCode() {
		return brandCode;
	}
	public void setBrandCode(String brandCode) {
		this.brandCode = brandCode;
	}
	public String getSourceType() {
		return sourceType;
	}
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	public String getInstanceNumber() {
		return instanceNumber;
	}
	public void setInstanceNumber(String instanceNumber) {
		this.instanceNumber = instanceNumber;
	}
	

	@Override
	public String toString() {
		return "TransformerRequest [brandCode=" + brandCode + ", sourceType=" + sourceType + ", instanceNumber="
				+ instanceNumber + ", configPath=" + configPath + "]";
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}
	
	

}
