package com.mindtree.utils.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindtree.core.service.IRule;

public class RulesEvaluator {
	
	protected final static Logger LOGGER = LoggerFactory.getLogger(RulesEvaluator.class);

	private List<IRule> ruleList;

	public RulesEvaluator() {
		ruleList = new ArrayList<IRule>();
	}

	public RulesEvaluator addRule(IRule rule) {
		this.ruleList.add(rule);
		return this;
	}

	public void evaluateAllRules() {
//		LOGGER.info("Applying rules started");
		for (IRule rule : this.ruleList) {
//			LOGGER.info("Applying rule : "+rule.getClass().getName());
			rule.apply();
		}
//		LOGGER.info("Applying rules finished");
	}
}
