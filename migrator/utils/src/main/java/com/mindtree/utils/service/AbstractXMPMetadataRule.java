package com.mindtree.utils.service;

import com.mindtree.core.service.IRule;
import com.mindtree.models.vo.RuleVO;
import com.mindtree.models.vo.XLSMetadataRuleVO;
import com.mindtree.models.vo.XMPMetadataRuleVO;

abstract public class AbstractXMPMetadataRule implements IRule {

	protected XMPMetadataRuleVO ruleVO;

	public AbstractXMPMetadataRule() {
		// TODO Auto-generated constructor stub
	}
	
	public AbstractXMPMetadataRule(XMPMetadataRuleVO ruleVO) {
		this.ruleVO = ruleVO;
	}

	abstract public void apply();
	
	public void setVO(RuleVO ruleVO) {
		this.ruleVO = (XMPMetadataRuleVO) ruleVO;
	}

}
