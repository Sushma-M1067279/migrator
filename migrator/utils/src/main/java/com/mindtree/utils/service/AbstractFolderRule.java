package com.mindtree.utils.service;

import com.mindtree.models.vo.FolderRuleVO;

abstract public class AbstractFolderRule implements IRule {
	protected FolderRuleVO ruleVO;

	public AbstractFolderRule(FolderRuleVO ruleVO) {
		this.ruleVO = ruleVO;
	}

	abstract public void apply();
}
