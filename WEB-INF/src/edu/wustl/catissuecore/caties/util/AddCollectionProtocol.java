package edu.wustl.catissuecore.caties.util;


import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import edu.wustl.catissuecore.bean.CollectionProtocolEventBean;
import edu.wustl.catissuecore.bean.SpecimenRequirementBean;
import edu.wustl.catissuecore.domain.CollectionProtocol;
import edu.wustl.catissuecore.domain.CollectionProtocolEvent;
import edu.wustl.catissuecore.domain.SpecimenCollectionRequirementGroup;
import edu.wustl.catissuecore.domain.User;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.util.logger.Logger;


/**
 * Program to add default Collection Protocol for caTIES
 */
public class AddCollectionProtocol
{
	/**
	 * Default entry oint of program
	 * @param args command line arguments list
	 */
    public static void main(String[] args) 
	{
		try
		{
			//ApplicationServiceProvider applicationServiceProvider = new ApplicationServiceProvider(); 
			Utility.init();
			CaCoreAPIService.initialize();
			Logger.out.info("*** Add Collection protocol...");
			
			CollectionProtocol collectionProtocol=initCollectionProtocol();
			CaCoreAPIService.createObject(collectionProtocol);
			Logger.out.info("*** Default Collection protocol added to system...");
		}
		catch(Exception ex)
		{
			System.out.println("Exception in AddCollectionProtocol = "+ ex.getMessage());
			Logger.out.error("Exception in AddCollectionProtocol = "+ ex);
		}
	}
    
    /**
     * Method to instantiate and initialize values for collection protocol
     * @return collectionProtocol object of CollectionProtocol
     * @throws Exception Generic exception occured
     */
	public static CollectionProtocol initCollectionProtocol() throws Exception
	{
	    CollectionProtocol collectionProtocol = new CollectionProtocol();

		collectionProtocol.setAliquotInSameContainer(new Boolean(false));
		collectionProtocol.setDescriptionURL("");
		collectionProtocol.setActivityStatus(Constants.ACTIVITY_STATUS_ACTIVE);
		
		collectionProtocol.setTitle(CaTIESProperties.getValue(CaTIESConstants.COLLECTION_PROTOCOL_TITLE));
		collectionProtocol.setShortTitle(CaTIESProperties.getValue(CaTIESConstants.COLLECTION_PROTOCOL_TITLE));
		
		collectionProtocol.setStartDate(edu.wustl.common.util.Utility.parseDate("08/15/2003", edu.wustl.common.util.Utility.datePattern("08/15/1975")));
		
		Collection collectionProtocolEventList = new LinkedHashSet();
		CollectionProtocolEvent collectionProtocolEvent = new CollectionProtocolEvent();
		setCollectionProtocolEvent(collectionProtocolEvent);
		
		collectionProtocolEvent.setCollectionProtocol(collectionProtocol);
		collectionProtocolEventList.add(collectionProtocolEvent);
		
		collectionProtocol.setCollectionProtocolEventCollection(collectionProtocolEventList);
		User principleInvestigator=new User();
		principleInvestigator.setId(new Long(1));
		collectionProtocol.setPrincipalInvestigator(principleInvestigator);
		
		return collectionProtocol;
	}
	
	/**
	 * Method to create collection protocol event
	 * @param collectionProtocolEvent
	 * @throws Exception Generic exception occured
	 */
	private static void setCollectionProtocolEvent(CollectionProtocolEvent collectionProtocolEvent) throws Exception
	{
		collectionProtocolEvent.setStudyCalendarEventPoint(new Double(1));
		collectionProtocolEvent.setCollectionPointLabel("Collection Point Label 1");
		collectionProtocolEvent.setClinicalStatus((String)CaCoreAPIService.getDefaultValue(Constants.DEFAULT_CLINICAL_STATUS));
		collectionProtocolEvent.setActivityStatus(Constants.ACTIVITY_STATUS_ACTIVE);
		collectionProtocolEvent.setClinicalDiagnosis((String)CaCoreAPIService.getDefaultValue(Constants.DEFAULT_CLINICAL_DIAGNOSIS));

		
		Collection specimenCollection =null;
		CollectionProtocolEventBean cpEventBean = new CollectionProtocolEventBean();
		SpecimenRequirementBean specimenRequirementBean = createSpecimenBean();
		
		cpEventBean.addSpecimenRequirementBean(specimenRequirementBean);
		Map specimenMap =(Map)cpEventBean.getSpecimenRequirementbeanMap();
		if (specimenMap!=null && !specimenMap.isEmpty())
		{
			specimenCollection =edu.wustl.catissuecore.util.CollectionProtocolUtil.getReqSpecimens(
					specimenMap.values()
					,null, collectionProtocolEvent);	
		}
		collectionProtocolEvent.setRequirementSpecimenCollection(specimenCollection);
	}
	
	/**
	 * Method to create specimen requirement bean
	 * @return specimenRequirementBean object of SpecimenRequirementBean
	 * @throws Exception Generic exception occured
	 */
	private static SpecimenRequirementBean createSpecimenBean() throws Exception
	{
		SpecimenRequirementBean specimenRequirementBean = new SpecimenRequirementBean();
		specimenRequirementBean.setUniqueIdentifier("E1_S0");
		specimenRequirementBean.setDisplayName("Specimen_E1_S0");
		specimenRequirementBean.setLineage("New");
		specimenRequirementBean.setClassName(Constants.TISSUE);
		specimenRequirementBean.setType(Constants.FIXED_TISSUE);
		specimenRequirementBean.setTissueSide((String)CaCoreAPIService.getDefaultValue(Constants.DEFAULT_TISSUE_SIDE));
		specimenRequirementBean.setTissueSite((String)CaCoreAPIService.getDefaultValue(Constants.DEFAULT_TISSUE_SITE));
		specimenRequirementBean.setPathologicalStatus((String)CaCoreAPIService.getDefaultValue(Constants.DEFAULT_PATHOLOGICAL_STATUS));
		specimenRequirementBean.setQuantity("0");
		specimenRequirementBean.setStorageContainerForSpecimen("Virtual");
	
		//Collected and received events
		specimenRequirementBean.setCollectionEventUserId(1);
		specimenRequirementBean.setReceivedEventUserId(1);
		specimenRequirementBean.setCollectionEventContainer((String)CaCoreAPIService.getDefaultValue(Constants.DEFAULT_CONTAINER));
		specimenRequirementBean.setReceivedEventReceivedQuality((String)CaCoreAPIService.getDefaultValue(Constants.DEFAULT_RECEIVED_QUALITY));
		specimenRequirementBean.setCollectionEventCollectionProcedure((String)CaCoreAPIService.getDefaultValue(Constants.DEFAULT_COLLECTION_PROCEDURE));
		
		specimenRequirementBean.setNoOfDeriveSpecimen(0);
		specimenRequirementBean.setDeriveSpecimen(null);
		return specimenRequirementBean;
	}
}


