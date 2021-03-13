package com.mindtree.core.service;

import com.mindtree.models.vo.RuleVO;

/**
 * An interface to standardize the way rules are written.
 * 
 * @author M1032046
 *
 */
public interface IFilter {
	
	public boolean apply();
	
	public void setVO(RuleVO ruleVO);

}
