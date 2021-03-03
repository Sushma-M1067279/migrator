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
@JsonPropertyOrder({ "results", "total", "more", "limit", "offset", "status", "Asset" })
public class AemQueryBuilderResponse implements Serializable {
	@JsonProperty("results")
	private long results;
	@JsonProperty("total")
	private long total;
	@JsonProperty("more")
	private boolean more;
	@JsonProperty("limit")
	private long limit;
	@JsonProperty("offset")
	private long offset;
	@JsonProperty("status")
	private String status;

	@JsonProperty("assetList")
	private List<Asset> assetList = new ArrayList<>();
	@JsonIgnore
	private transient Map<String, Object> additionalProperties = new HashMap<>();
	private static final long serialVersionUID = 6357853349793343021L;

	@JsonProperty("results")
	public long getResults() {
		return results;
	}

	@JsonProperty("results")
	public void setResults(long results) {
		this.results = results;
	}

	public AemQueryBuilderResponse withResults(long results) {
		this.results = results;
		return this;
	}

	@JsonProperty("total")
	public long getTotal() {
		return total;
	}

	@JsonProperty("total")
	public void setTotal(long total) {
		this.total = total;
	}

	public AemQueryBuilderResponse withTotal(long total) {
		this.total = total;
		return this;
	}

	@JsonProperty("more")
	public boolean isMore() {
		return more;
	}

	@JsonProperty("more")
	public void setMore(boolean more) {
		this.more = more;
	}

	public AemQueryBuilderResponse withMore(boolean more) {
		this.more = more;
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

	public AemQueryBuilderResponse withLimit(long limit) {
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

	public AemQueryBuilderResponse withOffset(long offset) {
		this.offset = offset;
		return this;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	public AemQueryBuilderResponse withStatus(String status) {
		this.status = status;
		return this;
	}

	/**
	 * @return the assetList
	 */
	@JsonProperty("assetList")
	public List<Asset> getAssetList() {
		return this.assetList;
	}

	/**
	 * @param assetList
	 *            the assetList to set
	 */
	@JsonProperty("assetList")
	public void setAssetList(List<Asset> assetList) {
		this.assetList = assetList;
	}

	public AemQueryBuilderResponse withAsset(List<Asset> assetList) {
		this.assetList = assetList;
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

	public AemQueryBuilderResponse withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("results", results).append("total", total).append("more", more)
				.append("limit", limit).append("offset", offset).append("status", status).append("assetList", assetList)
				.append("additionalProperties", additionalProperties).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(limit).append(total).append(more).append(results).append(assetList)
				.append(status).append(additionalProperties).append(offset).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if ((other instanceof AemQueryBuilderResponse)) {
			return false;
		}
		AemQueryBuilderResponse rhs = ((AemQueryBuilderResponse) other);
		if (null != rhs) {
			return new EqualsBuilder().append(limit, rhs.limit).append(total, rhs.total).append(more, rhs.more)
					.append(results, rhs.results).append(assetList, rhs.assetList).append(status, rhs.status)
					.append(additionalProperties, rhs.additionalProperties).append(offset, rhs.offset).isEquals();

		} else {
			return false;
		}
	}
}
