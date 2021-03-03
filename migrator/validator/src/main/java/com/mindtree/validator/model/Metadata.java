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

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metadata implements Serializable {
	@JsonIgnore
	private transient Map<String, Object> additionalProperties = new HashMap<>();
	private static final long serialVersionUID = -8424512441017106418L;
   
   

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	public Metadata withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}
	

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("additionalProperties", additionalProperties).toString();
	}
	
	

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(additionalProperties).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if ((other instanceof Metadata)) {
			return false;
		}
		Metadata rhs = ((Metadata) other);
		if (null != rhs) {
			return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).isEquals();
		} else {
			return false;
		}
	}
}
