package com.mindtree.utils.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.core.service.IFilter;
import com.mindtree.models.vo.RuleVO;
import com.mindtree.models.vo.ExtensionFilterVO;

public abstract class AbstractFilter implements IFilter {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractFilter.class);
	
	protected ExtensionFilterVO filterVO;
	
	public AbstractFilter() {
		
	}
	
	public AbstractFilter(ExtensionFilterVO filterVO) {
		this.filterVO = filterVO;
	}
	
	abstract public boolean apply();
	
	public void setVO(RuleVO filterVO) {
		this.filterVO = (ExtensionFilterVO) filterVO;
	}

}
