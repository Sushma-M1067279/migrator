package com.mindtree.validator.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "dateLowerBound", "dateUpperBound", "limit", "offset", "paths" })
public class AemRequest {
	@JsonProperty("dateLowerBound")
	private String dateLowerBound;
	@JsonProperty("dateUpperBound")
	private String dateUpperBound;
	@JsonProperty("limit")
	private long limit;
	@JsonProperty("offset")
	private long offset;
	@JsonProperty("path")
	private List<String> path = new ArrayList<>();
	
	@JsonProperty("type")
	private String type;

	private static final long serialVersionUID = -6712040919589076311L;

	@JsonProperty("dateLowerBound")
	public String getDateLowerBound() {
		return dateLowerBound;
	}

	@JsonProperty("dateLowerBound")
	public void setDateLowerBound(String dateLowerBound) {
		this.dateLowerBound = dateLowerBound;
	}

	public AemRequest withDateLowerBound(String dateLowerBound) {
		this.dateLowerBound = dateLowerBound;
		return this;
	}

	@JsonProperty("dateUpperBound")
	public String getDateUpperBound() {
		return dateUpperBound;
	}

	@JsonProperty("dateUpperBound")
	public void setDateUpperBound(String dateUpperBound) {
		this.dateUpperBound = dateUpperBound;
	}

	public AemRequest withDateUpperBound(String dateUpperBound) {
		this.dateUpperBound = dateUpperBound;
		return this;
	}

	@JsonProperty("limit")
	public long getLimit() {
		return limit;
	}

	@JsonProperty("limit")
	public void setLimit(long limit) {
		this.limit = limit;
	}

	public AemRequest withLimit(long limit) {
		this.limit = limit;
		return this;
	}

	@JsonProperty("offset")
	public long getOffset() {
		return offset;
	}

	@JsonProperty("offset")
	public void setOffset(long offset) {
		this.offset = offset;
	}

	public AemRequest withOffset(long offset) {
		this.offset = offset;
		return this;
	}

	@JsonProperty("path")
	public List<String> getPath() {
		return path;
	}

	@JsonProperty("path")
	public void setPaths(List<String> path) {
		this.path = path;
	}	

	/**
	 * @return the type
	 */
	@JsonProperty("type")
	public String getType() {
		return this.type;
	}

	/**
	 * @param type the type to set
	 */
	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	public AemRequest withPath(List<String> path) {
		this.path = path;
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("dateLowerBound", dateLowerBound)
				.append("dateUpperBound", dateUpperBound).append("limit", limit).append("offset", offset)
				.append("path", path).append("type", type).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(limit).append(dateUpperBound).append(path).append(offset)
				.append(dateLowerBound).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if ((other instanceof AemRequest)) {
			return false;
		}
		AemRequest rhs = ((AemRequest) other);
		if (null != rhs) {
			return new EqualsBuilder().append(limit, rhs.limit).append(dateUpperBound, rhs.dateUpperBound)
					.append(path, rhs.path).append(offset, rhs.offset).append(dateLowerBound, rhs.dateLowerBound)
					.isEquals();
		} else {
			return false;
		}
	}
}
