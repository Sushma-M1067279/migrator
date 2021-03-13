package com.mindtree.utils.service;

import com.mindtree.core.service.IRule;
import com.mindtree.models.vo.DriveBasedMetadataRuleVO;
import com.mindtree.models.vo.FolderRuleVO;
import com.mindtree.models.vo.RuleVO;

abstract public class AbstractFolderRule implements IRule {
	protected FolderRuleVO ruleVO;
	
	public AbstractFolderRule() {
		
	}

	public AbstractFolderRule(FolderRuleVO ruleVO) {
		this.ruleVO = ruleVO;
	}

	abstract public void apply();
	
	public void setVO(RuleVO ruleVO) {
		this.ruleVO = (FolderRuleVO) ruleVO;
	}
}
