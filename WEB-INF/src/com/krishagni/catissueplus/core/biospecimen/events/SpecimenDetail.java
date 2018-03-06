package com.krishagni.catissueplus.core.biospecimen.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.util.CollectionUtils;

import com.krishagni.catissueplus.core.administrative.events.StorageLocationSummary;
import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenRequirement;
import com.krishagni.catissueplus.core.biospecimen.domain.Visit;
import com.krishagni.catissueplus.core.common.ListenAttributeChanges;
import com.krishagni.catissueplus.core.common.util.Utility;
import com.krishagni.catissueplus.core.de.events.ExtensionDetail;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
@ListenAttributeChanges
public class SpecimenDetail extends SpecimenInfo {

	private static final long serialVersionUID = -752005520158376620L;

	private CollectionEventDetail collectionEvent;
	
	private ReceivedEventDetail receivedEvent;
	
	private String labelFmt;
	
	private String labelAutoPrintMode;
	
	private Set<String> biohazards;
	
	private String comments;
	
	private Boolean closeAfterChildrenCreation;  
	
	private List<SpecimenDetail> children;

	private Long pooledSpecimenId;
	
	private String pooledSpecimenLabel;

	private List<SpecimenDetail> specimensPool;

	//
	// Properties required for auto-creation of containers
	//
	private StorageLocationSummary containerLocation;

	private Long containerTypeId;

	private String containerTypeName;

	// This is needed for creation of derivatives from BO for closing parent specimen.
	private Boolean closeParent;
	
	private Boolean poolSpecimen;
	
	private String reqCode;

	private ExtensionDetail extensionDetail;

	private boolean reserved;

	//
	// transient variables specifying action to be performed
	//
	private boolean forceDelete;

	private boolean printLabel;

	private Integer incrParentFreezeThaw;

	private String transferComments;

	private boolean autoCollectParents;

	public CollectionEventDetail getCollectionEvent() {
		return collectionEvent;
	}

	public void setCollectionEvent(CollectionEventDetail collectionEvent) {
		this.collectionEvent = collectionEvent;
	}

	public ReceivedEventDetail getReceivedEvent() {
		return receivedEvent;
	}

	public void setReceivedEvent(ReceivedEventDetail receivedEvent) {
		this.receivedEvent = receivedEvent;
	}

	public String getLabelFmt() {
		return labelFmt;
	}

	public void setLabelFmt(String labelFmt) {
		this.labelFmt = labelFmt;
	}

	public String getLabelAutoPrintMode() {
		return labelAutoPrintMode;
	}

	public void setLabelAutoPrintMode(String labelAutoPrintMode) {
		this.labelAutoPrintMode = labelAutoPrintMode;
	}

	public List<SpecimenDetail> getChildren() {
		return children;
	}

	public void setChildren(List<SpecimenDetail> children) {
		this.children = children;
	}

	public Long getPooledSpecimenId() {
		return pooledSpecimenId;
	}

	public void setPooledSpecimenId(Long pooledSpecimenId) {
		this.pooledSpecimenId = pooledSpecimenId;
	}

	public String getPooledSpecimenLabel() {
		return pooledSpecimenLabel;
	}

	public void setPooledSpecimenLabel(String pooledSpecimenLabel) {
		this.pooledSpecimenLabel = pooledSpecimenLabel;
	}

	public List<SpecimenDetail> getSpecimensPool() {
		return specimensPool;
	}

	public void setSpecimensPool(List<SpecimenDetail> specimensPool) {
		this.specimensPool = specimensPool;
	}

	public StorageLocationSummary getContainerLocation() {
		return containerLocation;
	}

	public void setContainerLocation(StorageLocationSummary containerLocation) {
		this.containerLocation = containerLocation;
	}

	public Long getContainerTypeId() {
		return containerTypeId;
	}

	public void setContainerTypeId(Long containerTypeId) {
		this.containerTypeId = containerTypeId;
	}

	public String getContainerTypeName() {
		return containerTypeName;
	}

	public void setContainerTypeName(String containerTypeName) {
		this.containerTypeName = containerTypeName;
	}

	public Set<String> getBiohazards() {
		return biohazards;
	}

	public void setBiohazards(Set<String> biohazards) {
		this.biohazards = biohazards;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Boolean getCloseAfterChildrenCreation() {
		return closeAfterChildrenCreation;
	}

	public void setCloseAfterChildrenCreation(Boolean closeAfterChildrenCreation) {
		this.closeAfterChildrenCreation = closeAfterChildrenCreation;
	}

	public Boolean getCloseParent() {
		return closeParent;
	}

	public void setCloseParent(Boolean closeParent) {
		this.closeParent = closeParent;
	}

	public Boolean getPoolSpecimen() {
		return poolSpecimen;
	}

	public void setPoolSpecimen(Boolean poolSpecimen) {
		this.poolSpecimen = poolSpecimen;
	}
	
	public String getReqCode() {
		return reqCode;
	}

	public void setReqCode(String reqCode) {
		this.reqCode = reqCode;
	}

	public boolean closeParent() {
		return closeParent == null ? false : closeParent;
	}

	public ExtensionDetail getExtensionDetail() {
		return extensionDetail;
	}

	public void setExtensionDetail(ExtensionDetail extensionDetail) {
		this.extensionDetail = extensionDetail;
	}

	public boolean isReserved() {
		return reserved;
	}

	public void setReserved(boolean reserved) {
		this.reserved = reserved;
	}

	@JsonIgnore
	public boolean isForceDelete() {
		return forceDelete;
	}

	public void setForceDelete(boolean forceDelete) {
		this.forceDelete = forceDelete;
	}
	
	//
	// Do not serialise printLabel from interaction object to response JSON. Therefore @JsonIgnore
	// However, deserialise, if present, from input request JSON to interaction object. Hence @JsonProperty
	//
	@JsonIgnore
	public boolean isPrintLabel() {
		return printLabel;
	}

	@JsonProperty
	public void setPrintLabel(boolean printLabel) {
		this.printLabel = printLabel;
	}

	@JsonIgnore
	public Integer getIncrParentFreezeThaw() {
		return incrParentFreezeThaw;
	}

	@JsonProperty
	public void setIncrParentFreezeThaw(Integer incrParentFreezeThaw) {
		this.incrParentFreezeThaw = incrParentFreezeThaw;
	}

	@JsonIgnore
	public String getTransferComments() {
		return transferComments;
	}

	@JsonProperty
	public void setTransferComments(String transferComments) {
		this.transferComments = transferComments;
	}

	@JsonIgnore
	public boolean isAutoCollectParents() {
		return autoCollectParents;
	}

	@JsonProperty
	public void setAutoCollectParents(boolean autoCollectParents) {
		this.autoCollectParents = autoCollectParents;
	}

	public static SpecimenDetail from(Specimen specimen) {
		return from(specimen, true, true);
	}

	public static SpecimenDetail from(Specimen specimen, boolean partial, boolean excludePhi) {
		return from(specimen, partial, excludePhi, false);
	}

	public static SpecimenDetail from(Specimen specimen, boolean partial, boolean excludePhi, boolean excludeChildren) {
		SpecimenDetail result = new SpecimenDetail();
		SpecimenInfo.fromTo(specimen, result);
		
		SpecimenRequirement sr = specimen.getSpecimenRequirement();
		if (!excludeChildren) {
			if (sr == null) {
				List<SpecimenDetail> children = Utility.nullSafeStream(specimen.getChildCollection())
					.map(child -> from(child, partial, excludePhi, excludeChildren))
					.collect(Collectors.toList());
				sort(children);
				result.setChildren(children);
			} else {
				if (sr.isPooledSpecimenReq()) {
					result.setSpecimensPool(getSpecimens(specimen.getVisit(), sr.getSpecimenPoolReqs(), specimen.getSpecimensPool(), partial, excludePhi, excludeChildren));
				}
				result.setPoolSpecimen(sr.isSpecimenPoolReq());

				result.setChildren(getSpecimens(specimen.getVisit(), sr.getChildSpecimenRequirements(), specimen.getChildCollection(), partial, excludePhi, excludeChildren));
			}

			if (specimen.getPooledSpecimen() != null) {
				result.setPooledSpecimenId(specimen.getPooledSpecimen().getId());
				result.setPooledSpecimenLabel(specimen.getPooledSpecimen().getLabel());
			}
		}

		result.setLabelFmt(specimen.getLabelTmpl());
		if (sr != null && sr.getLabelAutoPrintModeToUse() != null) {
			result.setLabelAutoPrintMode(sr.getLabelAutoPrintModeToUse().name());
		}

		result.setReqCode(sr != null ? sr.getCode() : null);
		result.setBiohazards(new HashSet<>(specimen.getBiohazards()));
		result.setComments(specimen.getComment());
		result.setReserved(specimen.isReserved());

		if (!partial) {
			result.setExtensionDetail(ExtensionDetail.from(specimen.getExtension(), excludePhi));

			if (specimen.isPrimary()) {
				result.setCollectionEvent(CollectionEventDetail.from(specimen.getCollectionEvent()));
				result.setReceivedEvent(ReceivedEventDetail.from(specimen.getReceivedEvent()));
			}
		}
		
		return result;
	}
	
	public static List<SpecimenDetail> from(Collection<Specimen> specimens) {
		List<SpecimenDetail> result = new ArrayList<>();
		
		if (CollectionUtils.isEmpty(specimens)) {
			return result;
		}
		
		for (Specimen specimen : specimens) {
			result.add(SpecimenDetail.from(specimen));
		}
		
		return result;
	}
	
	public static SpecimenDetail from(SpecimenRequirement anticipated) {
		SpecimenDetail result = new SpecimenDetail();		
		SpecimenInfo.fromTo(anticipated, result);
		
		if (anticipated.isPooledSpecimenReq()) {
			result.setSpecimensPool(fromAnticipated(anticipated.getSpecimenPoolReqs()));
		}
		
		result.setPoolSpecimen(anticipated.isSpecimenPoolReq());
		result.setChildren(fromAnticipated(anticipated.getChildSpecimenRequirements()));
		result.setLabelFmt(anticipated.getLabelTmpl());
		if (anticipated.getLabelAutoPrintModeToUse() != null) {
			result.setLabelAutoPrintMode(anticipated.getLabelAutoPrintModeToUse().name());
		}
		result.setReqCode(anticipated.getCode());
		return result;		
	}

	public static List<SpecimenDetail> fromAnticipated(Collection<SpecimenRequirement> anticipatedSpecimens) {
		List<SpecimenDetail> result = new ArrayList<SpecimenDetail>();
		
		if (CollectionUtils.isEmpty(anticipatedSpecimens)) {
			return result;
		}
		
		for (SpecimenRequirement anticipated : anticipatedSpecimens) {
			result.add(SpecimenDetail.from(anticipated));
		}
		
		return result;
	}	
	
	public static void sort(List<SpecimenDetail> specimens) {
		Collections.sort(specimens);
		
		for (SpecimenDetail specimen : specimens) {
			if (specimen.getChildren() != null) {
				sort(specimen.getChildren());
			}
		}
	}
	
	public static List<SpecimenDetail> getSpecimens(
			Visit visit,
			Collection<SpecimenRequirement> anticipated,
			Collection<Specimen> specimens,
			boolean partial,
			boolean excludePhi,
			boolean excludeChildren) {
		List<SpecimenDetail> result = Utility.stream(specimens)
			.map(s -> SpecimenDetail.from(s, partial, excludePhi, excludeChildren))
			.collect(Collectors.toList());

		merge(visit, anticipated, result, null, getReqSpecimenMap(result));
		SpecimenDetail.sort(result);
		return result;
	}

	private static Map<Long, SpecimenDetail> getReqSpecimenMap(List<SpecimenDetail> specimens) {
		Map<Long, SpecimenDetail> reqSpecimenMap = new HashMap<Long, SpecimenDetail>();
						
		List<SpecimenDetail> remaining = new ArrayList<SpecimenDetail>();
		remaining.addAll(specimens);
		
		while (!remaining.isEmpty()) {
			SpecimenDetail specimen = remaining.remove(0);
			Long srId = (specimen.getReqId() == null) ? -1 : specimen.getReqId();
			reqSpecimenMap.put(srId, specimen);
			
			remaining.addAll(specimen.getChildren());
		}
		
		return reqSpecimenMap;
	}
	
	private static void merge(
			Visit visit,
			Collection<SpecimenRequirement> anticipatedSpecimens, 
			List<SpecimenDetail> result, 
			SpecimenDetail currentParent,
			Map<Long, SpecimenDetail> reqSpecimenMap) {
		
		for (SpecimenRequirement anticipated : anticipatedSpecimens) {
			SpecimenDetail specimen = reqSpecimenMap.get(anticipated.getId());
			if (specimen != null) {
				merge(visit, anticipated.getChildSpecimenRequirements(), result, specimen, reqSpecimenMap);
			} else {
				specimen = SpecimenDetail.from(anticipated);
				specimen.setVisitId(visit != null ? visit.getId() : null);
				
				if (currentParent == null) {
					result.add(specimen);
				} else {
					specimen.setParentId(currentParent.getId());
					currentParent.getChildren().add(specimen);
				}				
			}						
		}
	}
}
