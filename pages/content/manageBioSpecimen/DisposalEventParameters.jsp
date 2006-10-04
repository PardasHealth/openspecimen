<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/nlevelcombo.tld" prefix="ncombo" %>
<%@ page import="edu.wustl.catissuecore.util.global.Utility"%>
<%@ page import="edu.wustl.catissuecore.actionForm.DisposalEventParametersForm"%>

<%@ page import="edu.wustl.catissuecore.util.global.Constants"%>

<%
        String operation = (String) request.getAttribute(Constants.OPERATION);
        String formName,specimenId=null;

        boolean readOnlyValue;
        if (operation.equals(Constants.EDIT))
        {
            formName = Constants.DISPOSAL_EVENT_PARAMETERS_EDIT_ACTION;
            readOnlyValue = true;
        }
        else
        {
            formName = Constants.DISPOSAL_EVENT_PARAMETERS_ADD_ACTION;
			specimenId = (String) request.getAttribute(Constants.SPECIMEN_ID);
            readOnlyValue = false;
        }
		
			Object obj = request.getAttribute("disposalEventParametersForm");
			String currentEventParametersDate = ""; 
			if(obj != null && obj instanceof DisposalEventParametersForm)
			{
				DisposalEventParametersForm form = (DisposalEventParametersForm)obj;
			currentEventParametersDate = form.getDateOfEvent();
			if(currentEventParametersDate == null)
				currentEventParametersDate = "";
			}


%>	

<head>
<!-- Mandar : 434 : for tooltip -->
<script language="JavaScript" type="text/javascript" src="jss/javaScript.js"></script>
<!-- Mandar 21-Aug-06 : For calendar changes -->
<script src="jss/calendarComponent.js"></script>
<SCRIPT>var imgsrc="images/";</SCRIPT>
<LINK href="css/calanderComponent.css" type=text/css rel=stylesheet>
<!-- Mandar 21-Aug-06 : calendar changes end -->
<script language="javascript" >

	function confirmAction(form)
	{
		if(form.activityStatus.value == "<%=Constants.ACTIVITY_STATUS_DISABLED%>")
		{
			if(confirm("Are you sure you want to disable the specimen ?"))
			{
				form.action="<%=formName%>";
				form.submit();
			}
			else
			{
				return false;
			}
		}
		else
		{
			if(confirm("Are you sure you want to close the specimen ?"))
			{
				form.action="<%=formName%>";
				form.submit();
			}
			else
			{
				return false;
			}
		
		}
		
	}
</script>	
</head>
	
			
<html:errors/>
    
<table summary="" cellpadding="0" cellspacing="0" border="0" class="contentPage" width="600">

<html:form action="<%=Constants.DISPOSAL_EVENT_PARAMETERS_ADD_ACTION%>">


	<!-- NEW disposalEventParameter REGISTRATION BEGINS-->
	<tr>
	<td>
	
	<table summary="" cellpadding="3" cellspacing="0" border="0">
		<tr>
			<td><html:hidden property="operation" value="<%=operation%>"/></td>
		</tr>
		<tr>
			<td><html:hidden property="id" /></td>
		</tr>

		<tr>
			<td>
				<html:hidden property="specimenId" value="<%=specimenId%>"/>
			</td>
		</tr>
		
		<tr>
			 <td class="formMessage" colspan="3">* indicates a required field</td>
		</tr>

		<tr>
			<td class="formTitle" height="20" colspan="3">
				<logic:equal name="operation" value="<%=Constants.ADD%>">
					<bean:message key="disposaleventparameters.title"/>
				</logic:equal>
				<logic:equal name="operation" value="<%=Constants.EDIT%>">
					<bean:message key="disposaleventparameters.EDITtitle"/>
				</logic:equal>
			</td>
		</tr>

		<!-- Name of the disposalEventParameters -->
<!-- User -->		
		<tr>
			<td class="formRequiredNotice" width="5">*</td>
			<td class="formRequiredLabel">
				<label for="type">
					<bean:message key="eventparameters.user"/> 
				</label>
			</td>
			<td class="formField">
<!-- Mandar : 434 : for tooltip -->
				<html:select property="userId" styleClass="formFieldSized" styleId="userId" size="1"
				 onmouseover="showTip(this.id)" onmouseout="hideTip(this.id)">
					<html:options collection="<%=Constants.USERLIST%>" labelProperty="name" property="value"/>
				</html:select>
			</td>
		</tr>

<!-- date -->		
		<tr>
			<td class="formRequiredNotice" width="5">*</td>
			<td class="formRequiredLabel">
				<label for="type">
					<bean:message key="eventparameters.dateofevent"/> 
				</label>
			</td>
			<td class="formField">
				<%
				if(currentEventParametersDate.trim().length() > 0)
				{
					Integer eventParametersYear = new Integer(Utility.getYear(currentEventParametersDate ));
					Integer eventParametersMonth = new Integer(Utility.getMonth(currentEventParametersDate ));
					Integer eventParametersDay = new Integer(Utility.getDay(currentEventParametersDate ));
				%>
				<ncombo:DateTimeComponent name="dateOfEvent"
							  id="dateOfEvent"
							  formName="disposalEventParametersForm"
							  month= "<%= eventParametersMonth %>"
							  year= "<%= eventParametersYear %>"
							  day= "<%= eventParametersDay %>"
							  value="<%=currentEventParametersDate %>"
							  styleClass="formDateSized10"
									/>
				<% 
					}
					else
					{  
				 %>
				<ncombo:DateTimeComponent name="dateOfEvent"
							  id="dateOfEvent"
							  formName="disposalEventParametersForm"
							  styleClass="formDateSized10"
									/>
				<% 
					} 
				%> 
				<bean:message key="page.dateFormat" />&nbsp;
				

			</td>
		</tr>
		
		
		<!-- hours & minutes -->		
		<tr>
			<td class="formRequiredNotice" width="5">*</td>
			<td class="formRequiredLabel">
				<label for="eventparameters.time">
					<bean:message key="eventparameters.time"/>
				</label>
			</td>
			<td class="formField">
<!-- Mandar : 434 : for tooltip -->
				<html:select property="timeInHours" styleClass="formFieldSized5" styleId="timeInHours" size="1"
				 onmouseover="showTip(this.id)" onmouseout="hideTip(this.id)">
					<html:options name="<%=Constants.HOUR_LIST%>" labelName="<%=Constants.HOUR_LIST%>" />
				</html:select>&nbsp;
				<label for="eventparameters.timeinhours">
					<bean:message key="eventparameters.timeinhours"/>&nbsp; 
				</label>
<!-- Mandar : 434 : for tooltip -->
				<html:select property="timeInMinutes" styleClass="formFieldSized5" styleId="timeInMinutes" size="1"
				 onmouseover="showTip(this.id)" onmouseout="hideTip(this.id)">
					<html:options name="<%=Constants.MINUTES_LIST%>" labelName="<%=Constants.MINUTES_LIST%>" />
				</html:select>
				<label for="eventparameters.timeinhours">
					&nbsp;<bean:message key="eventparameters.timeinminutes"/> 
				</label>
			</td>
		</tr>
		
		
		<tr>
			<td class="formRequiredNotice" width="5">*</td>
			<td class="formRequiredLabel">
				<label for="activityStatus">
				<bean:message key="participant.activityStatus" />
				</label>
			</td>
			<td class="formField">
<!-- Mandar : 434 : for tooltip -->
						<html:select property="activityStatus" styleClass="formFieldSized10" styleId="activityStatus" size="1"
						 onmouseover="showTip(this.id)" onmouseout="hideTip(this.id)">
							<html:options name="<%=Constants.ACTIVITYSTATUSLIST%>" labelName="<%=Constants.ACTIVITYSTATUSLIST%>" />
						</html:select>
			</td>
		</tr>				

		
<!-- Reason -->		
		<tr>
		
			<td class="formRequiredNotice" width="5">*</td>
			<td class="formRequiredLabel">
				<label for="type">
					<bean:message key="disposaleventparameters.reason"/> 
				</label>
			</td>
			<td class="formField">
				<html:textarea styleClass="formFieldSized"  styleId="reason" property="reason" />
			</td>
		</tr>
	

<!-- comments -->		
		<tr>
			<td class="formRequiredNotice" width="5">&nbsp;</td>
			<td class="formLabel">
				<label for="type">
					<bean:message key="eventparameters.comments"/> 
				</label>
			</td>
			<td class="formField">
				<html:textarea styleClass="formFieldSized"  styleId="comments" property="comments" />
			</td>
		</tr>

<!-- buttons -->
		<tr>
		  <td align="right" colspan="3">
			<!-- action buttons begins -->
			<%
        		String changeAction = "setFormAction('" + formName + "');";
			%> 
			<table cellpadding="4" cellspacing="0" border="0">
				<tr>
					<td>
						<html:button styleClass="actionButton" value="Submit" property="Submit" onclick="return confirmAction(this.form)"/>
						<!--<html:submit styleClass="actionButton" value="Submit" onclick="<%=changeAction%>"/>-->
						
					</td>
					<%-- td><html:reset styleClass="actionButton"/></td --%> 
				</tr>
			</table>
			<!-- action buttons end -->
			</td>
		</tr>

		</table>
		
	  </td>
	 </tr>

	 <!-- NEW disposalEventParameters ends-->
	 
	 </html:form>
 </table>