
package com.krishagni.catissueplus.core.biospecimen.events;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.krishagni.catissueplus.core.administrative.events.Mergeable;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolRegistration;
import com.krishagni.catissueplus.core.biospecimen.domain.ConsentTierResponse;
import com.krishagni.catissueplus.core.biospecimen.domain.CpConsentTier;
import com.krishagni.catissueplus.core.common.AttributeModifiedSupport;
import com.krishagni.catissueplus.core.common.ListenAttributeChanges;
import com.krishagni.catissueplus.core.common.events.UserSummary;
import com.krishagni.catissueplus.core.common.util.Status;

@ListenAttributeChanges
public class ConsentDetail extends AttributeModifiedSupport implements Mergeable<String, ConsentDetail>, Serializable {
	
	private static final long serialVersionUID = -6534649433745006211L;

	private Long cprId;
	
	private Long cpId;
	
	private String cpShortTitle;
	
	private String ppid;
	
	private Date consentSignatureDate;

	private UserSummary witness;
	
	private String comments;
	
	private List<ConsentTierResponseDetail> consentTierResponses = new ArrayList<ConsentTierResponseDetail>();
	
	private String consentDocumentName;

	//For BO
	@JsonIgnore
	private String statement;
	
	@JsonIgnore
	private String response;

	@JsonIgnore
	private File documentFile;

	public Long getCprId() {
		return cprId;
	}

	public void setCprId(Long cprId) {
		this.cprId = cprId;
	}

	public Long getCpId() {
		return cpId;
	}

	public void setCpId(Long cpId) {
		this.cpId = cpId;
	}
	
	public String getCpShortTitle() {
		return cpShortTitle;
	}

	public void setCpShortTitle(String cpShortTitle) {
		this.cpShortTitle = cpShortTitle;
	}

	public String getPpid() {
		return ppid;
	}

	public void setPpid(String ppid) {
		this.ppid = ppid;
	}

	public Date getConsentSignatureDate() {
		return consentSignatureDate;
	}

	public void setConsentSignatureDate(Date consentSignatureDate) {
		this.consentSignatureDate = consentSignatureDate;
	}

	public UserSummary getWitness() {
		return witness;
	}

	public void setWitness(UserSummary witness) {
		this.witness = witness;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public List<ConsentTierResponseDetail> getConsentTierResponses() {
		return consentTierResponses;
	}

	public void setConsentTierResponses(List<ConsentTierResponseDetail> consenTierStatements) {
		this.consentTierResponses = consenTierStatements;
	}

	public String getConsentDocumentName() {
		return consentDocumentName;
	}

	public void setConsentDocumentName(String consentDocumentName) {
		this.consentDocumentName = consentDocumentName;
	}
	
	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}
	
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public File getDocumentFile() {
		return documentFile;
	}

	public void setDocumentFile(File documentFile) {
		this.documentFile = documentFile;
	}

	public static ConsentDetail fromCpr(CollectionProtocolRegistration cpr, boolean excludePhi) {
		ConsentDetail consent = new ConsentDetail();
		consent.setCpShortTitle(cpr.getCpShortTitle());
		consent.setPpid(cpr.getPpid());
		consent.setConsentSignatureDate(cpr.getConsentSignDate());
		consent.setComments(cpr.getConsentComments());
		
		String fileName = cpr.getSignedConsentDocumentName();
		if (fileName != null) {
			fileName = excludePhi ? "###" : fileName.split("_", 2)[1];
		}
		consent.setConsentDocumentName(fileName);
		
		if (cpr.getConsentWitness() != null) {
			consent.setWitness(UserSummary.from(cpr.getConsentWitness()));
		}

		Map<String, ConsentTierResponse> respMap = cpr.getConsentResponses().stream()
			.collect(Collectors.toMap(ConsentTierResponse::getStatementCode, item -> item));

		for (CpConsentTier consentTier : cpr.getCollectionProtocol().getConsentTier()) {
			ConsentTierResponse answer = respMap.get(consentTier.getStatement().getCode());
			if (consentTier.getActivityStatus().equals(Status.ACTIVITY_STATUS_CLOSED.getStatus()) && answer == null) {
				//
				// Don't show closed consent tiers if response is not given already.
				//
				continue;
			}

			ConsentTierResponseDetail response = new ConsentTierResponseDetail();
			response.setCode(consentTier.getStatement().getCode());
			response.setStatement(consentTier.getStatement().getStatement());

			if (answer != null) {
				response.setResponse(answer.getResponse());
			}

			consent.getConsentTierResponses().add(response);
		}

		return consent;
	}

	@Override
	@JsonIgnore
	public String getMergeKey() {
		return cpShortTitle + '_' + ppid;
	}

	@Override
	public void merge(ConsentDetail other) {
		if (StringUtils.isBlank(other.getStatement())) {
			return;
		}

		ConsentTierResponseDetail response = new ConsentTierResponseDetail();
		response.setStatement(other.getStatement());
		response.setResponse(other.getResponse());
		
		getConsentTierResponses().add(response);
	}
}