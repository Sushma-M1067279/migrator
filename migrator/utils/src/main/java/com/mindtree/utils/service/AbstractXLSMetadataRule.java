package com.mindtree.utils.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.core.service.IRule;
import com.mindtree.models.vo.FolderRuleVO;
import com.mindtree.models.vo.RuleVO;
import com.mindtree.models.vo.XLSMetadataRuleVO;

public abstract class AbstractXLSMetadataRule implements IRule {
	
	protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractXLSMetadataRule.class);

	protected XLSMetadataRuleVO ruleVO;
	
	public AbstractXLSMetadataRule() {
		// TODO Auto-generated constructor stub
	}

	public AbstractXLSMetadataRule(XLSMetadataRuleVO ruleVO) {
		this.ruleVO = ruleVO;
	}

	abstract public void apply();
	
	public void setVO(RuleVO ruleVO) {
		this.ruleVO = (XLSMetadataRuleVO) ruleVO;
	}

}
