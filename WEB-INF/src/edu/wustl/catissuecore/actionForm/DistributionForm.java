/**
 * <p>Title: DistributionForm Class</p>
 * <p>Description:  This Class handles the Distribution..
 * <p> It extends the EventParametersForm class.    
 * Copyright:    Copyright (c) 2005
 * Company: Washington University, School of Medicine, St. Louis.
 * @author Jyoti Singh
 * @version 1.00
 * Created on Aug 10, 2005
 */

package edu.wustl.catissuecore.actionForm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.domain.CellSpecimen;
import edu.wustl.catissuecore.domain.DistributedItem;
import edu.wustl.catissuecore.domain.Distribution;
import edu.wustl.catissuecore.domain.FluidSpecimen;
import edu.wustl.catissuecore.domain.MolecularSpecimen;
import edu.wustl.catissuecore.domain.Quantity;
import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.domain.SpecimenArray;
import edu.wustl.catissuecore.domain.TissueSpecimen;
import edu.wustl.catissuecore.util.MultipleSpecimenValidationUtil;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.catissuecore.util.global.Utility;
import edu.wustl.common.actionForm.AbstractActionForm;
import edu.wustl.common.domain.AbstractDomainObject;
import edu.wustl.common.util.global.ApplicationProperties;
import edu.wustl.common.util.global.Validator;
import edu.wustl.common.util.logger.Logger;

/**
 *
 * Description:  This Class handles the Distribution..
 */
public class DistributionForm extends AbstractActionForm implements ConsentTierData
{
	private long specimenId;
	/**
	 * Time in hours for the Event Parameter.
	 * */
	protected String timeInHours;

	/**
	 * Time in minutes for the Event Parameter.
	 * */
	protected String timeInMinutes;

	/**
	 * Date of the Event Parameter.
	 * */
	protected String dateOfEvent;

	/**
	 * Id of the User.   
	 */
	protected long userId;

	/**
	 * Comments on the event parameter.   
	 */
	protected String comments;

	//Consent Tracking Module---Virender Mehta
	/**
	 * Map for Storing responses for Consent Tiers.
	 */
	protected Map consentResponseForDistributionValues = new HashMap();
	/**
	 * No of Consent Tier
	 */
	private int consentTierCounter = 0;
	/**
	 * Signed Consent URL
	 */
	protected String signedConsentUrl = "";
	/**
	 * Witness name that may be PI
	 */
	protected String witnessName;
	/**
	 * Consent Date, Date on which Consent is Signed
	 */
	protected String consentDate = "";
	/**
	 * Counter for sequence no
	 */
	private int outerCounter = 0;

	//Consent Tracking Module---(Virender Mehta)

	//private String fromSite;
	private String toSite;

	private int counter = 0;
	private String distributionProtocolId;
	private boolean idChange = false;
	private int rowNo = 0;

	private Integer distributionType = new Integer(Constants.SPECIMEN_DISTRIBUTION_TYPE);

	private Integer distributionBasedOn = new Integer(Constants.LABEL_BASED_DISTRIBUTION);

	/**
	 * Map to handle values of all Events
	 */
	protected Map values = new HashMap();

	/**
	 * @return DISTRIBUTION_FORM_ID return distribution form id
	 */
	public int getFormId()
	{
		return Constants.DISTRIBUTION_FORM_ID;
	}

	/**
	 * @param abstractDomain An AbstractDomainObject object
	 */
	public void setAllValues(AbstractDomainObject abstractDomain)
	{
		//super.setAllValues(abstractDomain);

		Logger.out.debug("setAllValues of DistributionForm");

		Distribution distributionObject = (Distribution) abstractDomain;

		this.comments = Utility.toString(distributionObject.getComment());
		this.id = distributionObject.getId().longValue();

		Calendar calender = Calendar.getInstance();
		if (distributionObject.getTimestamp() != null)
		{
			calender.setTime(distributionObject.getTimestamp());
			this.timeInHours = Utility.toString(Integer.toString(calender.get(Calendar.HOUR_OF_DAY)));
			this.timeInMinutes = Utility.toString(Integer.toString(calender.get(Calendar.MINUTE)));
			this.dateOfEvent = Utility.parseDateToString(distributionObject.getTimestamp(), Constants.DATE_PATTERN_MM_DD_YYYY);
		}
		this.userId = distributionObject.getDistributedBy().getId().longValue();

		if (distributionObject.getSpecimen() != null)
		{
			specimenId = distributionObject.getSpecimen().getId().longValue();
		}

		this.distributionProtocolId = String.valueOf(distributionObject.getDistributionProtocol().getId());
		this.toSite = String.valueOf(distributionObject.getToSite().getId());
		this.activityStatus = Utility.toString(distributionObject.getActivityStatus());
		Logger.out.debug("this.activityStatus " + this.activityStatus);
 
		if (distributionObject.getDistributedItemCollection().size() != 0)
		{
			Iterator itr = distributionObject.getDistributedItemCollection().iterator();
			while (itr.hasNext())
			{
				DistributedItem distributedItem = (DistributedItem) itr.next();
				if (distributedItem.getSpecimen() != null)
				{
					this.distributionType = new Integer(Constants.SPECIMEN_DISTRIBUTION_TYPE);
					populateMapForSpecimen(distributionObject.getDistributedItemCollection());
				}
				else
				{
					this.distributionType = new Integer(Constants.SPECIMEN_ARRAY_DISTRIBUTION_TYPE);
					populateMapForArray(distributionObject.getSpecimenArrayCollection());

				}
			}

		}

		Logger.out.debug("Display Map Values" + values);

		//At least one row should be displayed in ADD MORE therefore
		/*		if(counter == 0)
		 counter = 1;
		 */}

	/**
	 * Populates map for Array
	 * @param specimenArrayCollection Collection of Specimen Array 
	 */
	private void populateMapForArray(Collection specimenArrayCollection)
	{
		if (specimenArrayCollection != null)
		{
			values = new HashMap();
			Iterator it = specimenArrayCollection.iterator();
			int i = 1;
			while (it.hasNext())
			{

				String key1 = "SpecimenArray:" + i + "_id";
				String key2 = "DistributedItem:" + i + "_Specimen_barcode";
				String key3 = "DistributedItem:" + i + "_Specimen_label";

				String key4 = "DistributedItem:" + i + "_quantity";

				SpecimenArray array = (SpecimenArray) it.next();
				values.put(key1, array.getId().toString());
				values.put(key2, array.getBarcode());
				values.put(key3, array.getName());
				values.put(key4, "1");
				i++;
			}
			counter = specimenArrayCollection.size();
		}
	}

	/**
	 * Populates Map for specimen
	 * @param distributedItemCollection Collection of distributed items
	 */
	private void populateMapForSpecimen(Collection distributedItemCollection)
	{
		if (distributedItemCollection != null)
		{
			values = new HashMap();

			Iterator it = distributedItemCollection.iterator();
			int i = 1;

			while (it.hasNext())
			{
				String key1 = "DistributedItem:" + i + "_id";
				String key2 = "DistributedItem:" + i + "_Specimen_id";
				String key3 = "DistributedItem:" + i + "_quantity";
				String key9 = "DistributedItem:" + i + "_availableQty";
				String key10 = "DistributedItem:" + i + "_previousQuantity";
				String key12 = "DistributedItem:" + i + "_Specimen_barcode";
				String key13 = "DistributedItem:" + i + "_Specimen_label";

				DistributedItem dItem = (DistributedItem) it.next();
				Specimen specimen = dItem.getSpecimen();

				Double quantity = dItem.getQuantity();
				//dItem.setPreviousQty(quantity);

				values.put(key1, Utility.toString(dItem.getId()));
				values.put(key2, Utility.toString(specimen.getId()));
				values.put(key3, quantity);
				values.put(key9, getAvailableQty(specimen));
				values.put(key10, quantity);
				values.put(key12, specimen.getBarcode());
				values.put(key13, specimen.getLabel());
				i++;
			}
			counter = distributedItemCollection.size();
		}
	}

	/**
	 * Overrides the validate method of ActionForm.
	 * @return error ActionErrors instance
	 * @param mapping Actionmapping instance
	 * @param request HttpServletRequest instance
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request)
	{
		//resolved bug# 4352
		//ActionErrors errors = super.validate(mapping, request);
		ActionErrors errors = new ActionErrors();
		Validator validator = new Validator();
		Logger.out.debug("Inside validate function");
		try
		{
			MultipleSpecimenValidationUtil.validateDate(errors, validator,
     				this.userId, this.dateOfEvent, this.timeInHours,this.timeInMinutes);
						if (specimenId == -1L)
			{
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required", "Specimen Id"));
			}
			// Mandar 10-apr-06 : bugid :353 
			// Error messages should be in the same sequence as the sequence of fields on the page.
			if (!validator.isValidOption(distributionProtocolId))
			{
				Logger.out.debug("dist prot");
				errors.add(ActionErrors.GLOBAL_ERROR,
						new ActionError("errors.item.required", ApplicationProperties.getValue("distribution.protocol")));
			}
			if (!validator.isValidOption("" + userId))
			{
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required", ApplicationProperties
						.getValue("distribution.distributedBy")));
			}

			//  date validation according to bug id  722 and 730
			String errorKey = validator.validateDate(dateOfEvent, true);
			if (errorKey.trim().length() > 0)
			{
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(errorKey, ApplicationProperties.getValue("eventparameters.dateofevent")));
			}
			if (validator.isEmpty(toSite) || toSite.equalsIgnoreCase("undefined"))
			{
				Logger.out.debug("to site");
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.invalid", ApplicationProperties.getValue("distribution.toSite")));
			}

			//Validations for Add-More Block
			if (this.values.keySet().isEmpty())
			{
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.one.item.required", ApplicationProperties
						.getValue("distribution.distributedItem")));
			}

			Iterator it = this.values.keySet().iterator();
			while (it.hasNext())
			{
				String key = (String) it.next();
				String value = (String) values.get(key);

				if (key.indexOf("Specimen_id") != -1 && !validator.isValidOption(value))
				{
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required", ApplicationProperties
							.getValue("itemrecord.specimenId")));
				}

				if (key.indexOf("_quantity") != -1)
				{
					if ((validator.isEmpty(value)))
					{
						Logger.out.debug("Quantity empty**************");
						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required", ApplicationProperties
								.getValue("itemrecord.quantity")));
					}
					else if (!validator.isDouble(value))
					{
						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.format", ApplicationProperties
								.getValue("itemrecord.quantity")));
					}
				}

				//}  if  quantity
			}
		}
		catch (Exception excp)
		{
			Logger.out.error(excp.getMessage());
		}

		return errors;
	}

	/**
	 * @return Returns the specimenId.
	 */
	public long getSpecimenId()
	{
		return specimenId;
	}

	/**
	 * @param specimenId The specimenId to set.
	 */
	public void setSpecimenId(long specimenId)
	{
		this.specimenId = specimenId;
	}

	/**
	 * @return Returns the comments.
	 */
	public String getComments()
	{
		return comments;
	}

	/**
	 * @param comments The comments to set.
	 */
	public void setComments(String comments)
	{
		this.comments = comments;
	}

	/**
	 * @return Returns the dateOfEvent.
	 */
	public String getDateOfEvent()
	{
		return dateOfEvent;
	}

	/**
	 * @param dateOfEvent The dateOfEvent to set.
	 */
	public void setDateOfEvent(String dateOfEvent)
	{
		this.dateOfEvent = dateOfEvent;
	}

	/**
	 * @return Returns the timeInHours.
	 */
	public String getTimeInHours()
	{
		return timeInHours;
	}

	/**
	 * @param timeInHours The timeInHours to set.
	 */
	public void setTimeInHours(String timeInHours)
	{
		this.timeInHours = timeInHours;
	}

	/**
	 * @return Returns the timeInMinutes.
	 */
	public String getTimeInMinutes()
	{
		return timeInMinutes;
	}

	/**
	 * @param timeInMinutes The timeInMinutes to set.
	 */
	public void setTimeInMinutes(String timeInMinutes)
	{
		this.timeInMinutes = timeInMinutes;
	}

	/**
	 * @return Returns the userId.
	 */
	public long getUserId()
	{
		return userId;
	}

	/**
	 * @param userId The userId to set.
	 */
	public void setUserId(long userId)
	{
		this.userId = userId;
	}

	/**
	 * @return Returns the distributionProtocolId.
	 */
	public String getDistributionProtocolId()
	{
		return distributionProtocolId;
	}

	/**
	 * @param distributionProtocolId The distributionProtocolId to set.
	 */
	public void setDistributionProtocolId(String distributionProtocolId)
	{
		this.distributionProtocolId = distributionProtocolId;
	}

	//	/**
	//	 * @return fromSite
	//	 */ 
	//	public String getFromSite() {
	//		return fromSite;
	//	}
	//	
	//	/**
	//	 * @param fromSite
	//	 */
	//	public void setFromSite(String fromSite) {
	//		this.fromSite = fromSite;
	//	}

	/**
	 * @return counter
	 */
	public int getCounter()
	{
		return counter;
	}

	/**
	 * @param counter The counter to set.
	 */
	public void setCounter(int counter)
	{
		this.counter = counter;
	}

	/**
	 * @return toSite get To site
	 */
	public String getToSite()
	{
		return toSite;
	}

	/**
	 * @param toSite Set site
	 */
	public void setToSite(String toSite)
	{
		this.toSite = toSite;
	}

	/**
	 * Associates the specified object with the specified key in the map.
	 * @param key the key to which the object is mapped.
	 * @param value the object which is mapped.
	 */
	public void setValue(String key, Object value)
	{
		if (isMutable())
			values.put(key, value);
	}

	/**
	 * Returns the object to which this map maps the specified key.
	 * 
	 * @param key the required key.
	 * @return the object to which this map maps the specified key.
	 */
	public Object getValue(String key)
	{
		return values.get(key);
	}

	/**
	 * @param values The values to set.
	 */
	public void setValues(Map values)
	{
		this.values = values;
	}

	/**
	 * @return values
	 */
	public Map getValues()
	{
		return values;
	}

	/**
	 * Resets the values of all the fields.
	 * This method defined in ActionForm is overridden in this class.
	 */
	protected void reset()
	{
		//        super.reset();
		//        this.distributionProtocolId = null;
		//        this.fromSite = null;
		//        this.toSite = null;
		//        this.counter =1;

	}

	/**
	 * @return Returns the idChange.
	 */
	public boolean isIdChange()
	{
		return idChange;
	}

	/**
	 * @param idChange The idChange to set.
	 */
	public void setIdChange(boolean idChange)
	{
		this.idChange = idChange;
	}

	/**
	 * @return rowNo Returns the rowNo.
	 */
	public int getRowNo()
	{
		return rowNo;
	}

	/**
	 * @param rowNo The rowNo to set.
	 */
	public void setRowNo(int rowNo)
	{
		this.rowNo = rowNo;
	}

	/*
	 * Unused code so commented ---- Ashwin Gupta
	 * 
	 private static String getUnitSpan(Specimen specimen)
	 {
	 
	 if(specimen instanceof TissueSpecimen)
	 {
	 return Constants.UNIT_GM;
	 
	 }
	 else if(specimen instanceof CellSpecimen)
	 {
	 return Constants.UNIT_CC;
	 
	 }
	 else if(specimen instanceof MolecularSpecimen)
	 {
	 return Constants.UNIT_MG;
	 
	 }
	 else if(specimen instanceof FluidSpecimen)
	 {
	 return Constants.UNIT_ML;
	 }
	 return null;
	 }
	 */

	/**
	 * This method returns UnitSpan for Specimen
	 */
	/*private static String getDomainObjectUnitSpan(Specimen specimen)
	 {
	 
	 if(specimen instanceof TissueSpecimen)
	 {
	 return Constants.UNIT_GM;
	 
	 }
	 else if(specimen instanceof CellSpecimen)
	 {
	 return Constants.UNIT_CC;
	 
	 }
	 else if(specimen instanceof MolecularSpecimen)
	 {
	 return Constants.UNIT_MG;
	 
	 }
	 else if(specimen instanceof FluidSpecimen)
	 {
	 return Constants.UNIT_ML;
	 }
	 return null;
	 }
	 */
	/**
	 *@param specimen Specimen class instance
	 *@return availableQuantity Quantity remaining
	 */
	public Object getAvailableQty(Specimen specimen)
	{
		//Retrieve the Available quantity for the particular specimen
		/* Aniruddha : 16/06/2006 -- TO BE DELETED --
		 * if(specimen instanceof TissueSpecimen)
		 {
		 TissueSpecimen tissueSpecimen = (TissueSpecimen) specimen;
		 Logger.out.debug("tissueSpecimenAvailableQuantityInGram "+tissueSpecimen.getAvailableQuantityInGram());
		 return tissueSpecimen.getAvailableQuantityInGram();
		 
		 }
		 else if(specimen instanceof CellSpecimen)
		 {
		 CellSpecimen cellSpecimen = (CellSpecimen) specimen;
		 return cellSpecimen.getAvailableQuantityInCellCount();
		 
		 }
		 else if(specimen instanceof MolecularSpecimen)
		 {
		 MolecularSpecimen molecularSpecimen = (MolecularSpecimen) specimen;
		 return molecularSpecimen.getAvailableQuantityInMicrogram();
		 
		 }
		 else if(specimen instanceof FluidSpecimen)
		 {
		 FluidSpecimen fluidSpecimen = (FluidSpecimen) specimen;
		 return fluidSpecimen.getAvailableQuantityInMilliliter();
		 }*/

		//Aniruddha : NEEDS TO TAKE CARE OFF CALLING METHOD
		Double availableQuantity = specimen.getAvailableQuantity();
		return availableQuantity;

		//return null;
	}

	/**
	 * This method returns AvailableQunatity for Specimen
	 * @param specimen Base class specimen
	 * @return specimen.getAvailableQuantity().getValue()
	 */
	public Object getDomainObjectAvailableQty(Specimen specimen)
	{
		return specimen.getAvailableQuantity();
	}

	/**
	 * This method returns ClassName for Specimen
	 * @param specimen Base class specimen
	 */
	public final String getDomainObjectClassName(Specimen specimen)
	{
		String className = null;

		if (specimen instanceof CellSpecimen)
		{
			className = "Cell";
		}
		else if (specimen instanceof MolecularSpecimen)
		{
			className = "Molecular";
		}
		else if (specimen instanceof FluidSpecimen)
		{
			className = "Fluid";
		}
		else if (specimen instanceof TissueSpecimen)
		{
			className = "Tissue";
		}

		return className;
	}

	/**
	 * This method sets Identifier of Objects inserted by AddNew activity in Form-Bean which initialized AddNew action
	 * @param addNewFor - FormBean ID of the object inserted
	 *  @param addObjectIdentifier - Identifier of the Object inserted 
	 */
	public void setAddNewObjectIdentifier(String addNewFor, Long addObjectIdentifier)
	{
		if (addNewFor.equals("distributionProtocolId"))
		{
			setDistributionProtocolId(addObjectIdentifier.toString());
		}
		else if (addNewFor.equals("userId"))
		{
			setUserId(addObjectIdentifier.longValue());
		}
		else if (addNewFor.equals("toSite"))
		{
			setToSite(addObjectIdentifier.toString());
		}
	}

	/**
	 * @return distributionBasedOn Distribution Based On label or barcode
	 */
	public Integer getDistributionBasedOn()
	{
		return distributionBasedOn;
	}

	/**
	 * @param distributionBasedOn Distribution Based On label or barcode
	 */
	public void setDistributionBasedOn(Integer distributionBasedOn)
	{
		this.distributionBasedOn = distributionBasedOn;
	}

	/**
	 * @return distributionType Type of Distribution
	 */
	public Integer getDistributionType()
	{
		return distributionType;
	}

	/**
	 * 
	 * @param distributionType Type of Distribution
	 */
	public void setDistributionType(Integer distributionType)
	{
		this.distributionType = distributionType;
	}

	//Consent Tracking Module---(Virender Mehta)	
	/**
	 * For Sequence no
	 * @return outerCounter
	 */
	public int getOuterCounter()
	{
		return outerCounter;
	}

	/**
	 * For Sequence no
	 * @param outerCounter For Sequence no
	 */
	public void setOuterCounter(int outerCounter)
	{
		this.outerCounter = outerCounter;
	}

	/**
	 * @return consentDate The Date on Which Consent is Signed
	 */
	public String getConsentDate()
	{
		return consentDate;
	}

	/**
	 * @param consentDate The Date on Which Consent is Signed
	 */
	public void setConsentDate(String consentDate)
	{
		this.consentDate = consentDate;
	}

	/**
	 *@return consentTierCounter  This will keep track of count of Consent Tier
	 */
	public int getConsentTierCounter()
	{
		return consentTierCounter;
	}

	/**
	 *@param consentTierCounter  This will keep track of count of Consent Tier
	 */
	public void setConsentTierCounter(int consentTierCounter)
	{
		this.consentTierCounter = consentTierCounter;
	}

	/**
	 * @return signedConsentUrl The reference to the electric signed document(eg PDF file)
	 */
	public String getSignedConsentUrl()
	{
		return signedConsentUrl;
	}

	/**
	 * @param signedConsentUrl The reference to the electric signed document(eg PDF file)
	 */
	public void setSignedConsentUrl(String signedConsentUrl)
	{
		this.signedConsentUrl = signedConsentUrl;
	}

	/**
	 * @return witnessName The name of the witness to the consent Signature(PI or coordinator of the Collection Protocol)
	 */
	public String getWitnessName()
	{
		return witnessName;
	}

	/**
	 * @param witnessName The name of the witness to the consent Signature(PI or coordinator of the Collection Protocol)
	 */
	public void setWitnessName(String witnessName)
	{
		this.witnessName = witnessName;
	}

	/**
	 * @return consentResponseForDistributionValues  The comments associated with Response at Distribution level
	 */
	public Map getConsentResponseForDistributionValues()
	{
		return consentResponseForDistributionValues;
	}

	/**
	 * @param consentResponseForDistributionValues  The comments associated with Response at Distribution level
	 */
	public void setConsentResponseForDistributionValues(Map consentResponseForDistributionValues)
	{
		this.consentResponseForDistributionValues = consentResponseForDistributionValues;
	}

	/**
	 * @param key Key prepared for saving data.
	 * @return consentResponseForDistributionValues.get(key)
	 */
	public void setConsentResponseForDistributionValue(String key, Object value)
	{
		if (isMutable())
			consentResponseForDistributionValues.put(key, value);
	}

	/**
	 * @param key Key prepared for saving data.
	 * @return consentResponseForSpecimenValues.get(key)
	 */
	public Object getConsentResponseForDistributionValue(String key)
	{
		return consentResponseForDistributionValues.get(key);
	}

	/**
	 * @return values in map consentResponseForDistributionValues
	 */
	public Collection getAllConsentResponseForDistributionValue()
	{
		return consentResponseForDistributionValues.values();
	}

	/**
	 * This function creates Array of String of keys and add them into the consentTiersList.
	 * @return consentTiersList
	 */
	public Collection getConsentTiers()
	{
		Collection consentTiersList = new ArrayList();
		String[] strArray = null;
		int noOfConsents = this.getConsentTierCounter();
		for (int counter = 0; counter < noOfConsents; counter++)
		{
			strArray = new String[6];
			strArray[0] = "consentResponseForDistributionValues(ConsentBean:" + counter + "_consentTierID)";
			strArray[1] = "consentResponseForDistributionValues(ConsentBean:" + counter + "_statement)";
			strArray[2] = "consentResponseForDistributionValues(ConsentBean:" + counter + "_participantResponse)";
			strArray[3] = "consentResponseForDistributionValues(ConsentBean:" + counter + "_participantResponseID)";
			strArray[4] = "consentResponseForDistributionValues(ConsentBean:" + counter + "_specimenLevelResponse)";
			strArray[5] = "consentResponseForDistributionValues(ConsentBean:" + counter + "_specimenLevelResponseID)";
			consentTiersList.add(strArray);
		}
		return consentTiersList;
	}

	/**
	 * This function returns empty string,we don't need this method but its definition is compulsory because this 
	 * function is declared in the implemented interface 
	 * @return "" 
	 */
	public String getConsentTierMap()
	{
		return "";
	}
	//Consent Tracking Module---(Virender Mehta)	

}