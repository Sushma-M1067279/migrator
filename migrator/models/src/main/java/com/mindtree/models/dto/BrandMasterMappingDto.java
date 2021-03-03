package com.mindtree.models.dto;

/**
 * This class is a java representation of Master Asset Metadata mapping
 * spreadsheet which contains mapping between master metadata fields and brand
 * metadata fields.
 * 
 * @author M1032046
 * 
 *
 */
public class BrandMasterMappingDto {

	private String brandMetadata;

	private String masterMetadata;

	private String fieldType;

	private String aemPropertyName;

	/**
	 * @return the aemPropertyName
	 */
	public String getAemPropertyName() {
		return aemPropertyName;
	}

	/**
	 * @param aemPropertyName
	 *            the aemPropertyName to set
	 */
	public void setAemPropertyName(String aemPropertyName) {
		this.aemPropertyName = aemPropertyName;
	}

	/**
	 * @return the fieldType
	 */
	public String getFieldType() {
		return fieldType;
	}

	/**
	 * @param fieldType
	 *            the fieldType to set
	 */
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	/**
	 * @return the brandMetadata
	 */
	public String getBrandMetadata() {
		return brandMetadata;
	}

	/**
	 * @param brandMetadata
	 *            the brandMetadata to set
	 */
	public void setBrandMetadata(String brandMetadata) {
		this.brandMetadata = brandMetadata;
	}

	/**
	 * @return the masterMetadata
	 */
	public String getMasterMetadata() {
		return masterMetadata;
	}

	/**
	 * @param masterMetadata
	 *            the masterMetadata to set
	 */
	public void setMasterMetadata(String masterMetadata) {
		this.masterMetadata = masterMetadata;
	}

	@Override
	public String toString() {
		return "BrandMasterMappingDto [brandMetadata=" + brandMetadata + ", masterMetadata=" + masterMetadata
				+ ", fieldType=" + fieldType + ", aemPropertyName=" + aemPropertyName + "]";
	}

}
