/**
 * <p>Title: FluidRequirementSpecimen Class>
 * <p>Description:  A single unit of body fluid specimen that is 
 * collected or created from a Participant.</p>
 * Copyright:    Copyright (c) year
 * Company: Washington University, School of Medicine, St. Louis.
 * @author virender_mehta
 * @version caTissueSuite V1.1
 */

package edu.wustl.catissuecore.domain;

import java.io.Serializable;

import edu.wustl.common.actionForm.AbstractActionForm;
import edu.wustl.common.actionForm.IValueObject;
import edu.wustl.common.util.logger.Logger;

/**
 * A single unit of body fluid specimen that is collected or created from a Participant.
 * @hibernate.subclass name="FluidRequirementSpecimen" discriminator-value="Fluid" 
 */
public class FluidRequirementSpecimen extends RequirementSpecimen implements Serializable
{
    private static final long serialVersionUID = 12345678923230L;
    public FluidRequirementSpecimen()
    {
    	
    }
    public FluidRequirementSpecimen(AbstractActionForm form)
    {
    	setAllValues(form);
    }
    /**
     * This function Copies the data from an NewSpecimenForm object to a TissueSpecimen object.
     * @param siteForm An SiteForm object containing the information about the site.  
     * */
    public void setAllValues(IValueObject abstractForm)
    {
        try
        {
        	super.setAllValues(abstractForm);
        }
        catch (Exception excp)
        {
            Logger.out.error(excp.getMessage());
        }
    }
    public FluidRequirementSpecimen(FluidRequirementSpecimen fluidRequirementSpecimen)
    {
    	//super(fluidRequirementSpecimen);
    }
    
    public FluidRequirementSpecimen createClone()
    {
    	FluidRequirementSpecimen cloneFluidRequirementSpecimen = new FluidRequirementSpecimen(this);
    	return cloneFluidRequirementSpecimen;
    }
}