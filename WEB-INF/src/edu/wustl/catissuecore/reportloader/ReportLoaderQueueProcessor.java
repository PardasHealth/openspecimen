package edu.wustl.catissuecore.reportloader;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.wustl.catissuecore.caties.util.CSVLogger;
import edu.wustl.catissuecore.caties.util.CaCoreAPIService;
import edu.wustl.catissuecore.caties.util.CaTIESConstants;
import edu.wustl.catissuecore.caties.util.CaTIESProperties;
import edu.wustl.catissuecore.caties.util.SiteInfoHandler;
import edu.wustl.catissuecore.domain.Participant;
import edu.wustl.catissuecore.domain.pathology.ReportLoaderQueue;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.util.logger.Logger;
import gov.nih.nci.common.util.HQLCriteria;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.ApplicationService;

/**
 * @author sandeep_ranade
 * This class represents a thread which polls on report queue entries and
 * add report data to database. 
 * 
 */
public class ReportLoaderQueueProcessor extends Thread
{
	/**
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		CSVLogger.configure(CaTIESConstants.LOGGER_QUEUE_PROCESSOR);
		Set participantSet=null;
		ReportLoaderQueue reportLoaderQueue=null;
		HL7Parser parser=null;
		Long startTime=null;
		Long endTime=null;
		while(true)
		{
			try 
			{
				while((reportLoaderQueue=getQueueObject())!=null)
				{	
					Logger.out.info("Processing report loader queue id:"+reportLoaderQueue.getId());
					try
					{
						Logger.out.debug("Processing report from Queue with serial no="+reportLoaderQueue.getId());
						participantSet=(Set)reportLoaderQueue.getParticipantCollection();
						Iterator it = participantSet.iterator();
						
						// get instance  of parser
						parser= (HL7Parser)ParserManager.getInstance().getParser();
						
						Participant participant=null;
						// parse report text
						if(it.hasNext())
						{
							participant=(Participant)it.next();
						}
						String reportText=reportLoaderQueue.getReportText();
						startTime=new Date().getTime();
						parser.parseString(participant, reportText, reportLoaderQueue.getSpecimenCollectionGroup());
						endTime=new Date().getTime();
						Logger.out.info("Report loaded successfully, deleting report queue object");
						// delete record from queue
						CaCoreAPIService.getAppServiceInstance().removeObject(reportLoaderQueue);
						CSVLogger.info(CaTIESConstants.LOGGER_QUEUE_PROCESSOR,new Date().toString()+","+reportLoaderQueue.getId()+","+"SUCCESS"+",Report Loaded SuccessFully  ,"+(endTime-startTime));
						Logger.out.info("Processed report from Queue with id ="+reportLoaderQueue.getId());
					}
					catch(Exception ex)
					{
						endTime=new Date().getTime();
						reportLoaderQueue.setStatus(CaTIESConstants.FAILURE);
						if(ex.getMessage().equalsIgnoreCase(CaTIESConstants.CP_NOT_FOUND_ERROR_MSG))
						{
							reportLoaderQueue.setStatus(CaTIESConstants.CP_NOT_FOUND);
						}
						CSVLogger.info(CaTIESConstants.LOGGER_QUEUE_PROCESSOR,new Date().toString()+","+reportLoaderQueue.getId()+","+reportLoaderQueue.getStatus()+","+ex.getMessage()+","+(endTime-startTime));
						CaCoreAPIService.getAppServiceInstance().updateObject(reportLoaderQueue);
					}
				}
						
				Logger.out.info("Report loader Queue server is going to sleep for "+CaTIESProperties.getValue(CaTIESConstants.POLLER_SLEEPTIME)+ "ms");
				Thread.sleep(Long.parseLong(CaTIESProperties.getValue(CaTIESConstants.POLLER_SLEEPTIME)));
				SiteInfoHandler.init(CaTIESProperties.getValue(CaTIESConstants.SITE_INFO_FILENAME));
			}
			catch (NumberFormatException ex) 
			{
				Logger.out.error("Error stopping ReportLoaderQueueThread ",ex);
			}
			catch (InterruptedException ex) 
			{
				Logger.out.error("Error stopping ReportLoaderQueueThread ",ex);
			}
			catch(Exception ex)
			{
				Logger.out.error("Error in processing of ReportLoaderQueueThread ",ex);
			}
		}
	}
	
	/**
	 * Method to retrieve list of all objects from report queue
	 * @return list List of objects in report queue
	 * @throws Exception Generic exception
	 */
	private ReportLoaderQueue getQueueObject() throws Exception
	{
		List queue=null;
		String hqlQuery="from edu.wustl.catissuecore.domain.pathology.ReportLoaderQueue where "+Constants.COLUMN_NAME_STATUS+"='"+CaTIESConstants.NEW+"' OR "+Constants.COLUMN_NAME_STATUS+"='"+CaTIESConstants.SITE_NOT_FOUND+"' OR "+Constants.COLUMN_NAME_STATUS+"='"+CaTIESConstants.CP_NOT_FOUND+"'";
		Logger.out.info("HQL Query:"+hqlQuery);
		HQLCriteria hqlCriteria = new HQLCriteria(hqlQuery); 
		
		try 
		{
			ApplicationService appService=CaCoreAPIService.getAppServiceInstance();
			appService.setRecordsCount(1);
			queue =appService.query(hqlCriteria, ReportLoaderQueue.class.getName());
		}
		catch (ApplicationException ex) 
		{
			Logger.out.error("Error while fetching ReportLoaderQueue objects "+ex);
		}
		Logger.out.info("ReportLoaderQueue query result" +queue.size());
		if(queue!=null && queue.size()>0)
		{
			return (ReportLoaderQueue)queue.get(0);
		}
		return null;
	}
}
