package com.mindtree.utils.service;

import com.mindtree.models.vo.XMPMetadataRuleVO;

abstract public class AbstractXMPMetadataRule implements IRule {

	protected XMPMetadataRuleVO ruleVO;

	public AbstractXMPMetadataRule(XMPMetadataRuleVO ruleVO) {
		this.ruleVO = ruleVO;
	}

	abstract public void apply();

}
