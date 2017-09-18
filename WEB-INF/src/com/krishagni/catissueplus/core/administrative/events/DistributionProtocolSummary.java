package com.krishagni.catissueplus.core.administrative.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.krishagni.catissueplus.core.administrative.domain.DistributionProtocol;
import com.krishagni.catissueplus.core.common.events.UserSummary;

public class DistributionProtocolSummary {
	private Long id;
	
	private String title;

	private String shortTitle;
	
	private UserSummary principalInvestigator;
	
	private Date startDate;
	
	private Date endDate;

	private String defReceivingSiteName;

	private int distributedSpecimensCount;

	private String activityStatus;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	public UserSummary getPrincipalInvestigator() {
		return principalInvestigator;
	}

	public void setPrincipalInvestigator(UserSummary principalInvestigator) {
		this.principalInvestigator = principalInvestigator;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getDefReceivingSiteName() {
		return defReceivingSiteName;
	}

	public void setDefReceivingSiteName(String defReceivingSiteName) {
		this.defReceivingSiteName = defReceivingSiteName;
	}

	public int getDistributedSpecimensCount() {
		return distributedSpecimensCount;
	}

	public void setDistributedSpecimensCount(int distributedSpecimensCount) {
		this.distributedSpecimensCount = distributedSpecimensCount;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}
	
	public static DistributionProtocolSummary from(DistributionProtocol dp) {
		DistributionProtocolSummary summary = new DistributionProtocolSummary();
		copy(dp, summary);
		return summary;
	}
	
	public static List<DistributionProtocolSummary> from(Collection<DistributionProtocol> dps) {
		List<DistributionProtocolSummary> result = new ArrayList<DistributionProtocolSummary>();
		
		for (DistributionProtocol dp : dps) {
			result.add(from(dp));
		}
		
		return result;
	}
	
	public static void copy(DistributionProtocol dp, DistributionProtocolSummary detail) {
		detail.setId(dp.getId());
		detail.setTitle(dp.getTitle());
		detail.setShortTitle(dp.getShortTitle());
		detail.setPrincipalInvestigator(UserSummary.from(dp.getPrincipalInvestigator()));
		detail.setStartDate(dp.getStartDate());
		detail.setEndDate(dp.getEndDate());
		if (dp.getDefReceivingSite() != null) {
			detail.setDefReceivingSiteName(dp.getDefReceivingSite().getName());
		}

		detail.setActivityStatus(dp.getActivityStatus());
	}
	
}
