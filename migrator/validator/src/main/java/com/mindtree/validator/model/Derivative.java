package com.mindtree.validator.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "size","path" })
public class Derivative implements Serializable {
	@JsonProperty("name")
	private String name;
	@JsonProperty("size")
	private long size;
	
	@JsonIgnore
	private transient Map<String, Object> additionalProperties = new HashMap<>();
	private static final long serialVersionUID = -714573596707089990L;

	@JsonProperty("name")
	public String getName() {
		return name;
	}
	

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	public Derivative withName(String name) {
		this.name = name;
		return this;
	}

	@JsonProperty("size")
	public long getSize() {
		return size;
	}

	@JsonProperty("size")
	public void setSize(long size) {
		this.size = size;
	}

	public Derivative withSize(long size) {
		this.size = size;
		return this;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	public Derivative withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", name).append("size", size)
				.append("additionalProperties", additionalProperties).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(additionalProperties).append(name).append(size).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if ((other instanceof Derivative)) {
			return false;
		}
		Derivative rhs = ((Derivative) other);
		if (null != rhs) {
			return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).append(name, rhs.name)
					.append(size, rhs.size).isEquals();
		} else {
			return false;
		}
	}
}
