package com.krishagni.catissueplus.core.common.service;

import java.util.List;

import com.krishagni.catissueplus.core.common.events.ConfigPrintRuleDetail;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.repository.ConfigPrintRuleListCriteria;

public interface ConfigPrintRuleService {
	ResponseEvent<List<ConfigPrintRuleDetail>> getConfigPrintRules(RequestEvent<ConfigPrintRuleListCriteria> req);

	ResponseEvent<ConfigPrintRuleDetail> createConfigPrintRule(RequestEvent<ConfigPrintRuleDetail> req);
}