package com.mindtree.validator.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
@JsonPropertyOrder({ "path", "title", "metadata", "derivatives" ,"related" })
public class Asset implements Serializable {
	@JsonProperty("path")
	private String path;
	@JsonProperty("title")
	private String title;
	@JsonProperty("metadata")
	private Metadata metadata;
	@JsonProperty("derivatives")
	private List<Derivative> derivatives = new ArrayList<>();
	
	
	@JsonProperty
	private List<String> related = new ArrayList<>();
	
	@JsonIgnore
	private transient Map<String, Object> additionalProperties = new HashMap<>();
	private static final long serialVersionUID = -1035309073378071397L;

	@JsonProperty("path")
	public String getPath() {
		return path;
	}

	@JsonProperty("path")
	public void setPath(String path) {
		this.path = path;
	}

	public Asset withPath(String path) {
		this.path = path;
		return this;
	}

	@JsonProperty("title")
	public String getTitle() {
		return title;
	}

	@JsonProperty("title")
	public void setTitle(String title) {
		this.title = title;
	}

	public Asset withTitle(String title) {
		this.title = title;
		return this;
	}

	@JsonProperty("metadata")
	public Metadata getMetadata() {
		return metadata;
	}

	@JsonProperty("metadata")
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public Asset withMetadata(Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	@JsonProperty("derivatives")
	public List<Derivative> getDerivatives() {
		return derivatives;
	}

	@JsonProperty("derivatives")
	public void setDerivatives(List<Derivative> derivatives) {
		this.derivatives = derivatives;
	}

	public Asset withDerivatives(List<Derivative> derivatives) {
		this.derivatives = derivatives;
		return this;
	}
	@JsonProperty("related")
	public List<String> getRelated() {
		return related;
	}
	@JsonProperty("related")
	public void setRelated(List<String> related) {
		this.related = related;
	}
	public Asset withRelated(List<String> related) {
		this.related = related;
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

	public Asset withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("path", path).append("title", title).append("metadata", metadata)
				.append("derivatives", derivatives).append("related", related).append("additionalProperties", additionalProperties).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(title).append(additionalProperties).append(path).append(derivatives).append(related)
				.append(metadata).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if ((other instanceof Asset)) {
			return false;
		}
		Asset rhs = ((Asset) other);
		if (null != rhs) {
			return new EqualsBuilder().append(title, rhs.title).append(additionalProperties, rhs.additionalProperties)
					.append(path, rhs.path).append(derivatives, rhs.derivatives).append(related, rhs.related).append(metadata, rhs.metadata)
					.isEquals();
		} else {
			return false;
		}
	}
	

	
}
