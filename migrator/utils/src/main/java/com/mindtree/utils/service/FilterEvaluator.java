package com.mindtree.utils.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterEvaluator {

	protected final static Logger LOGGER = LoggerFactory.getLogger(FilterEvaluator.class);

	private List<IFilter> filterList;

	public FilterEvaluator() {
		filterList = new ArrayList<IFilter>();
	}

	public FilterEvaluator addFilter(IFilter filter) {
		this.filterList.add(filter);
		return this;
	}

	public boolean evaluateAllFilters() {
		boolean result = false;
		LOGGER.info("Applying filters started");
		for (IFilter filter : this.filterList) {
			LOGGER.info("Applying filter : "+filter.getClass().getName());
			result = filter.apply();
			if (result) {
				LOGGER.info("Filter resulted in success : "+filter.getClass().getName());
				continue;
			} else {
				LOGGER.info("Filter resulted in failure : "+filter.getClass().getName());
				break;
			}
		}
		LOGGER.info("Applying filters finished");
		return result;
	}
}
