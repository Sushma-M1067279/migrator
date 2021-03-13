package com.mindtree.utils.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.core.service.IRule;
import com.mindtree.models.vo.DriveBasedMetadataRuleVO;
import com.mindtree.models.vo.ExtensionFilterVO;
import com.mindtree.models.vo.RuleVO;

public abstract class AbstractDriveBasedRule implements IRule {

	protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractDriveBasedRule.class);

	protected DriveBasedMetadataRuleVO ruleVO;
	
	public AbstractDriveBasedRule() {
		
	}

	public AbstractDriveBasedRule(DriveBasedMetadataRuleVO ruleVO) {
		this.ruleVO = ruleVO;
	}

	abstract public void apply();
	
	public void setVO(RuleVO ruleVO) {
		this.ruleVO = (DriveBasedMetadataRuleVO) ruleVO;
	}
}
