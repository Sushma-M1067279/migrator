package com.mindtree.utils.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.models.vo.DriveBasedMetadataRuleVO;

public abstract class AbstractDriveBasedRule implements IRule {

	protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractDriveBasedRule.class);

	protected DriveBasedMetadataRuleVO ruleVO;

	public AbstractDriveBasedRule(DriveBasedMetadataRuleVO ruleVO) {
		this.ruleVO = ruleVO;
	}

	abstract public void apply();
}
