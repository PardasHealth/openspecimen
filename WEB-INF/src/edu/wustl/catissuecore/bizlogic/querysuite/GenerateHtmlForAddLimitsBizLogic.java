
package edu.wustl.catissuecore.bizlogic.querysuite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
import edu.common.dynamicextensions.domain.PermissibleValue;
import edu.common.dynamicextensions.domain.UserDefinedDE;
import edu.common.dynamicextensions.domaininterface.AttributeInterface;
import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
import edu.common.dynamicextensions.domaininterface.BooleanValueInterface;
import edu.common.dynamicextensions.domaininterface.DateValueInterface;
import edu.common.dynamicextensions.domaininterface.DoubleValueInterface;
import edu.common.dynamicextensions.domaininterface.EntityInterface;
import edu.common.dynamicextensions.domaininterface.FloatValueInterface;
import edu.common.dynamicextensions.domaininterface.IntegerValueInterface;
import edu.common.dynamicextensions.domaininterface.LongValueInterface;
import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
import edu.common.dynamicextensions.domaininterface.ShortValueInterface;
import edu.common.dynamicextensions.domaininterface.StringValueInterface;
import edu.wustl.cab2b.common.exception.CheckedException;
import edu.wustl.cab2b.common.util.AttributeInterfaceComparator;
import edu.wustl.cab2b.common.util.PermissibleValueComparator;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.querysuite.queryobject.ICondition;
import edu.wustl.common.querysuite.queryobject.IConstraints;
import edu.wustl.common.querysuite.queryobject.IExpression;
import edu.wustl.common.querysuite.queryobject.IExpressionId;
import edu.wustl.common.querysuite.queryobject.IExpressionOperand;
import edu.wustl.common.querysuite.queryobject.IQuery;
import edu.wustl.common.querysuite.queryobject.IRule;
import edu.wustl.common.querysuite.queryobject.RelationalOperator;
import edu.wustl.common.querysuite.queryobject.impl.ParameterizedCondition;
import edu.wustl.common.util.ParseXMLFile;
import edu.wustl.common.util.Utility;
import edu.wustl.common.util.global.ApplicationProperties;

/**
 * This class generates UI for 'Add Limits' and 'Edit Limits' section.
 * 
 * @author deepti_shelar
 */
public class GenerateHtmlForAddLimitsBizLogic
{

	/**
	 * Object which holds data operators fro attributes.
	 */
	static ParseXMLFile parseFile = null;

	private int expressionId = -1;

	private String attributesList = "";

	public int getExpressionId()
	{
		return expressionId;
	}

	private String formName = "categorySearchForm";

	public void setExpressionId(int expressionId)
	{
		this.expressionId = expressionId;
	}

	/**
	 * Constructor for GenerateHtmlForAddLimitsBizLogic
	 * 
	 * @throws CheckedException
	 */
	public GenerateHtmlForAddLimitsBizLogic()
	{
		if (parseFile == null)
		{
			try
			{
				parseFile = ParseXMLFile.getInstance(Constants.DYNAMIC_UI_XML);
			}
			catch (CheckedException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method retrive expressionid , entity and conditions from the query object
	 * @param queryObject
	 * @param isShowAll
	 * @param forPage
	 * @return
	 */
	public String getHTMLForSavedQuery(IQuery queryObject, boolean isShowAll, String forPage)
	{
		String htmlString = "";
		Map<IExpressionId, Map<EntityInterface, List<ICondition>>> expressionMap = new HashMap<IExpressionId, Map<EntityInterface, List<ICondition>>>();
		IConstraints constraints = queryObject.getConstraints();
		Enumeration<IExpressionId> expressionIds = constraints.getExpressionIds();
		while (expressionIds.hasMoreElements())
		{
			IExpression expression = constraints.getExpression(expressionIds.nextElement());
			for (int i = 0; i < expression.numberOfOperands(); i++)
			{
				IExpressionOperand operand = expression.getOperand(i);
				if (!operand.isSubExpressionOperand())
				{
					IRule ruleObject = (IRule) operand;
					List<ICondition> conditions = ruleObject.getConditions();
					EntityInterface entity = expression.getQueryEntity()
							.getDynamicExtensionsEntity();
					Map<EntityInterface, List<ICondition>> entityConditionMap = new HashMap<EntityInterface, List<ICondition>>();
					entityConditionMap.put(entity, conditions);
					expressionMap.put(expression.getExpressionId(), entityConditionMap);
				}
			}

		}
		htmlString = generateHTMLForSavedQuery(expressionMap, isShowAll, forPage);

		return htmlString;
	}

	/**
	 * 
	 * @return
	 */
	public StringBuffer generateSaveQueryPreHTML()
	{
		StringBuffer generatedPreHTML = new StringBuffer();
		generatedPreHTML
				.append("<table border=\"0\" width=\"100%\"  cellspacing=\"0\" cellpadding=\"0\">");
		return generatedPreHTML;
	}

	/**
	 * 
	 * @param expressionID
	 * @param entity
	 * @param conditions
	 * @param isShowAll
	 * @param forPage
	 * @param isTopButton
	 * @return
	 */
	public StringBuffer generateSaveQueryForEntity(int expressionID, EntityInterface entity,
			List<ICondition> conditions, boolean isShowAll, String forPage, boolean isTopButton,
			Map<EntityInterface, List<IExpressionId>> entityList)
	{
		setExpressionId(expressionID);
		StringBuffer generatedHTML = new StringBuffer();
		StringBuffer generatedPreHTML = new StringBuffer();
		String nameOfTheEntity = entity.getName();
		Collection<AttributeInterface> attributeCollection = entity.getEntityAttributesForQuery();
		Collection<AttributeInterface> collection = new ArrayList<AttributeInterface>();
		// String attributesList = "";
		boolean isEditLimits = false;
		boolean isBGColor = false;
		boolean isParameterizedCondition = false;
		Map<String, ICondition> attributeNameConditionMap = null;
		if (conditions != null)
		{
			attributeNameConditionMap = getMapOfConditions(conditions);
			isEditLimits = true;
		}
		if (!attributeCollection.isEmpty())
		{
			// get the list of dag ids for the corresponding entity
			List<IExpressionId> entityDagId = (List<IExpressionId>)entityList.get(entity);
			String DAGNodeId = "";		// Converting the dagId to string
			if (entityDagId.size() > 1)
			{
				// DAGNodeId / expressionID to be shown only in case if there are more than one node of the same class
				DAGNodeId = String.valueOf(expressionID) + "."; 
			}
			List<AttributeInterface> attributes = new ArrayList<AttributeInterface>(attributeCollection);
			String styleSheetClass = "rowBGWhiteColor";
			Collections.sort(attributes, new AttributeInterfaceComparator());
			if (forPage.equalsIgnoreCase(Constants.ADD_EDIT_PAGE))
			{
				generatedHTML
						.append("<table border=\"0\" width=\"100%\" height=\"100%\" cellspacing=\"0\" cellpadding=\"0\">");
				generatedHTML.append("\n<tr>");
				generatedHTML.append("\n<td valign=\"top\">");
				generatedHTML.append("\n</td>");
				generatedHTML.append("\n</tr>");
			}
			for(AttributeInterface attribute : attributes)
			{
				String attrName = attribute.getName();
				if (forPage.equalsIgnoreCase(Constants.EXECUTE_QUERY_PAGE))
					isParameterizedCondition = attributeNameConditionMap.containsKey(attrName)
							&& (attributeNameConditionMap.get(attrName) instanceof ParameterizedCondition);
				if (attributeNameConditionMap != null
						&& !attributeNameConditionMap.containsKey(attrName) && !isShowAll)
				{
					continue;
				}
				collection.add(attribute);
				String attrLabel = Utility.getDisplayLabel(attrName);
				String componentId = generateComponentName(attribute);
				if (forPage.equalsIgnoreCase(Constants.EXECUTE_QUERY_PAGE)
						&& isParameterizedCondition)
					attributesList = attributesList + ";" + componentId;
				else if (!forPage.equalsIgnoreCase(Constants.EXECUTE_QUERY_PAGE))
					attributesList = attributesList + ";" + componentId;
				if (isBGColor)
				{
					styleSheetClass = "rowBGGreyColor1";
				}
				else
				{
					styleSheetClass = "rowBGWhiteColor";
				}
				isBGColor = !isBGColor;
				String name =Utility.parseClassName(entity.getName());				

				generatedHTML.append("\n<tr  class='"+styleSheetClass +"'" +

				"  id=\"componentId\" " +
				//"\" height=\"6%\" " +
				" >\n");

				if (forPage.equalsIgnoreCase(Constants.SAVE_QUERY_PAGE))
				{
					formName = "saveQueryForm";
					generatedHTML.append(" " + generateCheckBox(attribute, false)
							+ "<td valign='top' align='left' class='standardTextQuery'>"
							+"<label for='" + componentId
							+ "_displayName' title='" + DAGNodeId + name + "." + attrLabel + "'>"
							+ "<input type=\"textbox\"  class=\"formFieldSized20\"  name='"
							+ componentId + "_displayName'     id='" + componentId
							+ "_displayName' value='" + DAGNodeId + name + "." + attrLabel
							+ "' disabled='true'> " + "</label></td>");
				}
				if (!forPage.equalsIgnoreCase(Constants.EXECUTE_QUERY_PAGE))
					generatedHTML
							.append("<td valign='top' align='left' class='standardTextQuery' nowrap='nowrap' width=\"15%\">"
									+ attrLabel + " ");
				if (forPage.equalsIgnoreCase(Constants.EXECUTE_QUERY_PAGE)
						&& isParameterizedCondition)
				{
					formName = "saveQueryForm";
					ParameterizedCondition con = (ParameterizedCondition) attributeNameConditionMap
							.get(attrName);
					generatedHTML
							.append("<td valign='top' align='left' class='standardTextQuery' nowrap='nowrap' width=\"15%\">"
									+ con.getName() + " ");
				}
				if (attribute.getDataType().equalsIgnoreCase(Constants.DATE))
				{
					String dateFormat = Constants.DATE_FORMAT;// ApplicationProperties.getValue("query.date.format");
					generatedHTML.append("\n(" + dateFormat + ")");
				}
				if (forPage.equalsIgnoreCase(Constants.EXECUTE_QUERY_PAGE)
						&& attributeNameConditionMap.get(attrName) instanceof ParameterizedCondition)
				{
					generatedHTML.append("&nbsp;&nbsp;&nbsp;&nbsp;</b></td>\n");
				}
				else if (!forPage.equalsIgnoreCase(Constants.EXECUTE_QUERY_PAGE))
				{
					generatedHTML.append("&nbsp;&nbsp;&nbsp;&nbsp;</b></td>\n");
				}

				List<String> operatorsList = getConditionsList(attribute);
				boolean isBetween = false;
				if (!operatorsList.isEmpty()
						&& operatorsList.get(0).equalsIgnoreCase(
								RelationalOperator.Between.toString()))
				{
					isBetween = true;
				}
				List<PermissibleValueInterface> permissibleValues = getPermissibleValuesList(attribute);
				if (conditions != null)
				{
					if (attributeNameConditionMap.containsKey(attrName))
					{
						ICondition condition = attributeNameConditionMap.get(attrName);
						if (forPage.equalsIgnoreCase(Constants.EXECUTE_QUERY_PAGE)
								&& !(condition instanceof ParameterizedCondition))
							continue;

						List<String> values = condition.getValues();
						String operator = condition.getRelationalOperator().getStringRepresentation();
						generatedHTML.append("\n"
								+ generateHTMLForOperators(attribute, operatorsList, operator,
										"PermissibleValuesQuery"));
						if (operator.equalsIgnoreCase(RelationalOperator.Between.toString()))
						{
							isBetween = true;
						}
						else
						{
							isBetween = false;
						}
						if (!permissibleValues.isEmpty())
						{
							generatedHTML.append("\n"
									+ generateHTMLForEnumeratedValues(attribute, permissibleValues,
											values, "PermissibleValuesQuery"));
						}
						else
						{
							if (attribute.getDataType().equalsIgnoreCase("boolean"))
							{
								generatedHTML.append("\n"
										+ generateHTMLForRadioButton(attribute, values,
												"standardTextQuery"));
							}
							else
							{
								generatedHTML.append("\n"
										+ generateHTMLForTextBox(attribute, isBetween, values,
												operator, "standardTextQuery"));
							}
						}

					}
					else
					{
						generatedHTML.append("\n"
								+ generateHTMLForOperators(attribute, operatorsList, null,
										"PermissibleValuesQuery"));
						if (!permissibleValues.isEmpty())
						{
							generatedHTML.append("\n"
									+ generateHTMLForEnumeratedValues(attribute, permissibleValues,
											null, "PermissibleValuesQuery"));
						}
						else
						{
							if (attribute.getDataType().equalsIgnoreCase("boolean"))
							{
								generatedHTML.append("\n"
										+ generateHTMLForRadioButton(attribute, null, "standardTextQuery"));
							}
							else
							{
								generatedHTML.append("\n"
										+ generateHTMLForTextBox(attribute, isBetween, null, null,
												"standardTextQuery"));
							}
						}
					}

				}
				if (conditions == null)
				{
					generatedHTML.append("\n"
							+ generateHTMLForOperators(attribute, operatorsList, null,
									"PermissibleValuesQuery"));
					if (!permissibleValues.isEmpty())
					{
						generatedHTML.append("\n"
								+ generateHTMLForEnumeratedValues(attribute, permissibleValues,
										null, "PermissibleValuesQuery"));
					}
					else
					{
						if (attribute.getDataType().equalsIgnoreCase("boolean"))
						{
							generatedHTML
									.append("\n"
											+ generateHTMLForRadioButton(attribute, null,
													"standardTextQuery"));
						}
						else
						{
							generatedHTML.append("\n"
									+ generateHTMLForTextBox(attribute, isBetween, null, null,
											"standardTextQuery"));
						}
					}
				}
				generatedHTML.append("\n</tr>");

			}

			if (forPage.equalsIgnoreCase(Constants.SAVE_QUERY_PAGE)
					|| forPage.equalsIgnoreCase(Constants.EXECUTE_QUERY_PAGE))
			{
				generatedHTML.append(" <input type='hidden'  id='" + expressionID + ":"
						+ Utility.parseClassName(entity.getName()) + "_attributeList'" + "value="
						+ getAttributesString(collection) + " />  ");
			}
			else if (forPage.equalsIgnoreCase(Constants.ADD_EDIT_PAGE))
			{
				// isTopButton = true;
				generatedHTML.append("\n<tr>");
				generatedHTML.append("\n<td valign=\"top\">");
				generatedHTML.append("\n</td>");
				generatedHTML.append("\n</tr>");
				// generatedHTML.append(generateHTMLForButton(nameOfTheEntity,
				// attributesList, isEditLimits,isTopButton));
				generatedHTML.append("</table>");
				generatedPreHTML = generatePreHtml(attributeCollection, nameOfTheEntity,
						isEditLimits, isTopButton);
			}
		}
		if (forPage.equalsIgnoreCase(Constants.ADD_EDIT_PAGE))
		{
			generatedPreHTML.append("####");
			return generatedPreHTML.append(generatedHTML);
		}
		return generatedHTML;
	}

	private String generateCheckBox(AttributeInterface attribute, boolean isSelected)
	{
		String select = (isSelected ? "select" : "");
		String componentId = generateComponentName(attribute);
		String tag = "<td class=\"standardTextQuery\"  width=\"5\" valign=\"top\"><input type=\"checkbox\"   id='"
				+ componentId
				+ "_checkbox'"
				+ select
				+ "  onClick=\"enableDisplayField(this.form,'" + componentId + "')\"></td>";
		return tag;
	}

	private String generateComponentName(AttributeInterface attribute)
	{
		String componentId = "";
		String attributeName = "";
		if (getExpressionId() > -1)
		{
			componentId = getExpressionId() + "_";
		}
		else
		{
			attributeName = attribute.getName();
		}
		componentId = componentId + attributeName + attribute.getId().toString();
		return componentId;

	}

	/*
	 * This method generates the html for Save Query section. This internally
	 * calls methods to generate other UI components like text, Calendar,
	 * Combobox etc. This method is same as the generateHTML except that this
	 * will generate html for selected conditions and will display only those
	 * conditions with their values set by user. @param entity entity to be
	 * presented on UI. @param conditions List of conditions , will contains
	 * atleast one element always. @return String html generated for Save Query
	 * section.
	 */

	public String generateHTMLForSavedQuery(
			Map<IExpressionId, Map<EntityInterface, List<ICondition>>> expressionMap, boolean isShowAll,
			String forPage)
	{
		StringBuffer generatedHTML = new StringBuffer(
				"<table  cellpadding=\"3\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
		if (forPage.equalsIgnoreCase(Constants.SAVE_QUERY_PAGE))
			generatedHTML
					.append("<tr style='height:10%'><td  valign='top' class='formSubTitleWithoutBorder'>"
							+ ApplicationProperties
									.getValue("savequery.column.userDefined")
							+ "</td><td valign='top' class='formSubTitleWithoutBorder'>"
							+ ApplicationProperties
									.getValue("savequery.column.displayLabel")
							+ "</td><td valign='top' class='formSubTitleWithoutBorder'>"
							+ ApplicationProperties
									.getValue("savequery.column.attributeName")
							+ "</td><td valign='top' class='formSubTitleWithoutBorder'>"
							+ ApplicationProperties
									.getValue("savequery.column.condition")
							+ "</td><td colspan=\"4\"  valign='top' class='formSubTitleWithoutBorder'>"
							+ ApplicationProperties
									.getValue("savequery.column.value")
							+ "</td></tr>");
		else if(forPage.equalsIgnoreCase(Constants.EXECUTE_QUERY_PAGE))
			generatedHTML
					.append("<tr valign='top'><td class=\"formSubTitleWithoutBorder\" valign='top'>" 
							+ ApplicationProperties
									.getValue("savequery.column.displayLabel")
							+ "</td><td class=\"formSubTitleWithoutBorder\" valign='top' >" 
							+ ApplicationProperties
									.getValue("savequery.column.condition")
							+ "</td><td class=\"formSubTitleWithoutBorder\" colspan=\"4\" valign='top' >" 
							+ ApplicationProperties
							.getValue("savequery.column.value")
							+ "</td></tr>");
		attributesList = "";
		Map<EntityInterface, List<ICondition>> entityConditionMap = null;
		String expressionEntityString = "";
		if (expressionMap.isEmpty())
		{
			generatedHTML.append("No record found.");
			return generatedHTML.toString();
		}
		else
		{
			//get the map which holds the list of all dag ids / expression ids for a particular entity
			Map<EntityInterface, List<IExpressionId>> entityExpressionIdListMap = 
																	getEntityExpressionIdListMap(expressionMap);
			Iterator<IExpressionId> it = expressionMap.keySet().iterator();
			while (it.hasNext())
			{
				IExpressionId expressionId = (IExpressionId) it.next();
				entityConditionMap = expressionMap.get(expressionId);
				if (entityConditionMap.isEmpty())
				{
					continue;
				}
				Iterator<EntityInterface> it2 = entityConditionMap.keySet().iterator();
				while (it2.hasNext())
				{
					EntityInterface entity = (EntityInterface) it2.next();
					List<ICondition> conditions = entityConditionMap.get(entity);
					generatedHTML.append(generateSaveQueryForEntity(expressionId.getInt(), entity,
							conditions, isShowAll, forPage, false, entityExpressionIdListMap));
					expressionEntityString = expressionEntityString + expressionId.getInt() + ":"
							+ Utility.parseClassName(entity.getName()) + ";";

				}

			}
		}
		if (forPage.equalsIgnoreCase(Constants.SAVE_QUERY_PAGE)
				|| forPage.equalsIgnoreCase(Constants.EXECUTE_QUERY_PAGE))
		{
			generatedHTML.append("<input type='hidden' id='totalentities' value='"
					+ expressionEntityString + "' />");
			generatedHTML.append("<input type='hidden' id='attributesList' value='"
					+ attributesList + "' />");
			generatedHTML
					.append("<input type='hidden' id='conditionList' name='conditionList' value='' />");
		}
		generatedHTML.append("</table>");
		return generatedHTML.toString();
	}

	/** Create a map which holds the list of all Expression(DAGNode) ids for a particular entity
	 * @param expressionMap
	 * @return map consisting of the entity and their corresponding expression ids
	 */
	private Map<EntityInterface, List<IExpressionId>> getEntityExpressionIdListMap(
			Map<IExpressionId, Map<EntityInterface, List<ICondition>>> expressionMap) 
	{
			Map<EntityInterface, List<IExpressionId>> entityExpressionIdMap = new HashMap<EntityInterface, 
																					List<IExpressionId>>();
			Iterator<IExpressionId> outerMapIterator = expressionMap.keySet().iterator();
			while (outerMapIterator.hasNext())
			{
				IExpressionId expressionId = (IExpressionId) outerMapIterator.next();
				Map<EntityInterface, List<ICondition>> entityMap = expressionMap.get(expressionId);
				if (!entityMap.isEmpty())
				{
					Iterator<EntityInterface> innerMapIterator = entityMap.keySet().iterator();
					while (innerMapIterator.hasNext())
					{
						List<IExpressionId> dagIdList = null;
						EntityInterface entity = (EntityInterface)innerMapIterator.next();
						if (!entityExpressionIdMap.containsKey(entity))
						{
							//if the entity is not present in the map create new list and add it to map
							dagIdList = new ArrayList<IExpressionId>();
							dagIdList.add(expressionId);
							entityExpressionIdMap.put(entity, dagIdList);
							continue;
						}		
						//if the entity is present in the map add the dag id to the existing list 
						dagIdList = (List<IExpressionId>)entityExpressionIdMap.get(entity);
						dagIdList.add(expressionId);
						entityExpressionIdMap.put(entity, dagIdList);
					}
				}
			}
		return entityExpressionIdMap;
	}

	private StringBuffer generatePreHtml(Collection<AttributeInterface> attributeCollection, 
			String nameOfTheEntity, boolean isEditLimits, boolean isTopButton)
	{
		String header = Constants.DEFINE_SEARCH_RULES;
		String entityName = Utility.parseClassName(nameOfTheEntity);
		StringBuffer generatedPreHTML = new StringBuffer();
		generatedPreHTML
				.append("<table border=\"0\" width=\"100%\" height=\"30%\" cellspacing=\"0\" cellpadding=\"0\">");
		generatedPreHTML.append("\n<tr height=\"2%\"> ");
		generatedPreHTML
				.append("<td valign='top' height=\"2%\" colspan=\"8\" bgcolor=\"#EAEAEA\" ><font face=\"Arial\" size=\"2\" color=\"#000000\"><b>");
		generatedPreHTML.append(header + " '" + entityName + "'</b></font>");
		generatedPreHTML.append("\n</td>");
		generatedPreHTML.append(generateHTMLForButton(nameOfTheEntity,
				getAttributesString(attributeCollection), isEditLimits, isTopButton));
		generatedPreHTML.append("\n</tr></table>");
		return generatedPreHTML;
	}

	/**
	 * This method generates the html for Add Limits and Edit Limits section.
	 * This internally calls methods to generate other UI components like text, Calendar, Combobox etc.
	 * @param entity entity to be presented on UI.
	 * @param conditions List of conditions , These are required in case of edit limits, For adding linits this parameter is null
	 * @return String html generated for Add Limits section.
	 */
	public String generateHTML(EntityInterface entity, List<ICondition> conditions)
	{
		StringBuffer generatedPreHTML = new StringBuffer();
		StringBuffer generatedHTML = new StringBuffer();
		String nameOfTheEntity = entity.getName();
		String entityId = entity.getId().toString();
		String entityName = Utility.parseClassName(nameOfTheEntity);//nameOfTheEntity.substring(nameOfTheEntity.lastIndexOf(".")+1,nameOfTheEntity.length());
		entityName = Utility.getDisplayLabel(entityName);
		Collection<AttributeInterface> attributeCollection = entity.getEntityAttributesForQuery();
		boolean isEditLimits = false;
		String header = Constants.DEFINE_SEARCH_RULES;
		//ApplicationProperties.getValue("query.defineSearchRulesFor");//"\nDefine Search Rules For";//
		String attributesList = "";
		generatedPreHTML
				.append("<table border=\"0\" width=\"100%\" height=\"30%\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#EAEAEA\" >");
		generatedPreHTML.append("\n<tr height=\"2%\" bgcolor=\"#EAEAEA\" > ");
		generatedPreHTML
				.append("<td class='standardLabelQuery' valign='top' height=\"2%\" colspan=\"8\" bgcolor=\"#EAEAEA\" ><font face=\"Arial\" size=\"2\" color=\"#000000\"><b>");
		generatedPreHTML.append(header + " '" + entityName + "'</b></font>");
		generatedPreHTML.append("\n</td>");

		boolean isTopButton = true;
		if (conditions != null)
		{
			isEditLimits = true;
		}
		generatedPreHTML.append(generateHTMLForButton(entityId,
				getAttributesString(attributeCollection), isEditLimits, isTopButton));
		generatedPreHTML.append("\n</tr></table>");
		generatedHTML
				.append("<table valign='top' border=\"0\" width=\"100%\" height=\"100%\" cellspacing=\"0\" cellpadding=\"0\" class='rowBGWhiteColor'>");
		boolean isBGColor = false;
		generatedHTML.append("\n<tr>");
		generatedHTML.append("\n<td valign=\"top\">");
		generatedHTML.append("\n</td>");
		generatedHTML.append("\n</tr>");
		if (!attributeCollection.isEmpty())
		{
			List<AttributeInterface> attributes = new ArrayList<AttributeInterface>(attributeCollection);
			String styleSheetClass = "rowBGWhiteColor";
			Collections.sort(attributes, new AttributeInterfaceComparator());
			for (int i = 0; i < attributes.size(); i++)
			{
				AttributeInterface attribute = (AttributeInterface) attributes.get(i);
				String attrName = attribute.getName();
				String attrLabel = Utility.getDisplayLabel(attrName);
				String componentId = generateComponentName(attribute);
				attributesList = attributesList + ";" + componentId;
				if (isBGColor)
				{
					styleSheetClass = "rowBGGreyColor1";
				}
				else
				{
					styleSheetClass = "rowBGWhiteColor";
				}
				isBGColor = !isBGColor;
				generatedHTML
						.append("\n<tr class='"
								+ styleSheetClass
								+ "' id=\""
								+ componentId
								+ "\" height=\"6%\" >\n"
								+ "<td valign='top' align='right' class='standardLabelQuery' nowrap='nowrap' width=\"15%\">");
				generatedHTML.append(attrLabel + " ");
				if (attribute.getDataType().equalsIgnoreCase(Constants.DATE))
				{
					String dateFormat = Constants.DATE_FORMAT;//ApplicationProperties.getValue("query.date.format");
					generatedHTML.append("\n(" + dateFormat + ")");
				}
				generatedHTML.append(":&nbsp;&nbsp;&nbsp;&nbsp;</td>\n");
				List<String> operatorsList = getConditionsList(attribute);
				boolean isBetween = false;
				if (!operatorsList.isEmpty()
						&& operatorsList.get(0).equalsIgnoreCase(
								RelationalOperator.Between.toString()))
				{
					isBetween = true;
				}
				List<PermissibleValueInterface> permissibleValues = getPermissibleValuesList(attribute);
				if (conditions != null)
				{
					Map<String, ICondition> attributeNameConditionMap = getMapOfConditions(conditions);
					isEditLimits = true;
					if (attributeNameConditionMap.containsKey(attrName))
					{
						ICondition condition = attributeNameConditionMap.get(attrName);
						List<String> values = (List<String>) condition.getValues();
						String operator = condition.getRelationalOperator().getStringRepresentation();
						generatedHTML.append("\n"
								+ generateHTMLForOperators(attribute, operatorsList, operator,
										"PermissibleValuesQuery"));
						if (operator.equalsIgnoreCase(RelationalOperator.Between.toString()))
						{
							isBetween = true;
						}
						else
						{
							isBetween = false;
						}
						if (!permissibleValues.isEmpty())
						{
							generatedHTML.append("\n"
									+ generateHTMLForEnumeratedValues(attribute, permissibleValues,
											values, "PermissibleValuesQuery"));
						}
						else
						{
							if (attribute.getDataType().equalsIgnoreCase("boolean"))
							{
								generatedHTML.append("\n"
										+ generateHTMLForRadioButton(attribute, values,
												"standardTextQuery"));
							}
							else
							{
								generatedHTML.append("\n"
										+ generateHTMLForTextBox(attribute, isBetween, values,
												operator, "standardTextQuery"));
							}
						}
					}
					else
					{
						generatedHTML.append("\n"
								+ generateHTMLForOperators(attribute, operatorsList, null,
										"PermissibleValuesQuery"));
						if (!permissibleValues.isEmpty())
						{
							generatedHTML.append("\n"
									+ generateHTMLForEnumeratedValues(attribute, permissibleValues,
											null, "PermissibleValuesQuery"));
						}
						else
						{
							if (attribute.getDataType().equalsIgnoreCase("boolean"))
							{
								generatedHTML.append("\n"
										+ generateHTMLForRadioButton(attribute, null,
												"standardTextQuery"));
							}
							else
							{
								generatedHTML.append("\n"
										+ generateHTMLForTextBox(attribute, isBetween, null, null,
												"standardTextQuery"));
							}
						}
					}
				}
				if (conditions == null)
				{
					generatedHTML.append("\n"
							+ generateHTMLForOperators(attribute, operatorsList, null,
									"PermissibleValuesQuery"));
					if (!permissibleValues.isEmpty())
					{
						generatedHTML.append("\n"
								+ generateHTMLForEnumeratedValues(attribute, permissibleValues,
										null, "PermissibleValuesQuery"));
					}
					else
					{
						if (attribute.getDataType().equalsIgnoreCase("boolean"))
						{
							generatedHTML.append("\n"
									+ generateHTMLForRadioButton(attribute, null,
											"standardTextQuery"));
						}
						else
						{
							generatedHTML.append("\n"
									+ generateHTMLForTextBox(attribute, isBetween, null, null,
											"standardTextQuery"));
						}
					}
				}
				generatedHTML.append("\n</tr>");
			}
		}
		isTopButton = false;
		generatedHTML.append("\n<tr>");
		generatedHTML.append("\n<td valign=\"top\">");
		generatedHTML.append("\n</td>");
		generatedHTML.append("\n</tr>");
		//generatedHTML.append(generateHTMLForButton(nameOfTheEntity, attributesList, isEditLimits,isTopButton));
		generatedHTML.append("</table>");
		return generatedPreHTML.toString() + "####" + generatedHTML.toString();
	}

	/**
	 * returns PermissibleValuesList' list for attribute
	 * 
	 * @param attribute
	 *            AttributeInterface
	 * @return List of permissible values for the passed attribute
	 */
	private List<PermissibleValueInterface> getPermissibleValuesList(AttributeInterface attribute)
	{
		UserDefinedDE userDefineDE = (UserDefinedDE) attribute.getAttributeTypeInformation()
				.getDataElement();
		List<PermissibleValueInterface> permissibleValues = new ArrayList<PermissibleValueInterface>();
		if (userDefineDE != null && userDefineDE.getPermissibleValueCollection() != null)
		{
			Iterator<PermissibleValueInterface> permissibleValueInterface = userDefineDE
					.getPermissibleValueCollection().iterator();
			while (permissibleValueInterface.hasNext())
			{
				PermissibleValue permValue = (PermissibleValue) permissibleValueInterface.next();
				if (permValue instanceof StringValueInterface)
					permissibleValues.add(((StringValueInterface) permValue));

				else if (permValue instanceof ShortValueInterface)
					permissibleValues.add(((ShortValueInterface) permValue));

				else if (permValue instanceof LongValueInterface)
					permissibleValues.add(((LongValueInterface) permValue));

				else if (permValue instanceof DateValueInterface)
					permissibleValues.add(((DateValueInterface) permValue));

				else if (permValue instanceof BooleanValueInterface)
					permissibleValues.add(((BooleanValueInterface) permValue));

				else if (permValue instanceof IntegerValueInterface)
					permissibleValues.add(((IntegerValueInterface) permValue));

				else if (permValue instanceof DoubleValueInterface)
					permissibleValues.add((DoubleValueInterface) permValue);

				else if (permValue instanceof FloatValueInterface)
					permissibleValues.add(((FloatValueInterface) permValue));
			}
		}
		return permissibleValues;
	}

	/**
	 * Returns the map of name of the attribute and condition obj as its value.
	 * 
	 * @param conditions
	 *            list of conditions user had applied in case of edit limits
	 * @return Map name of the attribute and condition obj
	 */
	private Map<String, ICondition> getMapOfConditions(List<ICondition> conditions)
	{
		if (conditions == null)
			return null;
		Map<String, ICondition> attributeNameConditionMap = new HashMap<String, ICondition>();
		for (int i = 0; i < conditions.size(); i++)
		{
			attributeNameConditionMap.put(conditions.get(i).getAttribute().getName(), conditions
					.get(i));
		}
		return attributeNameConditionMap;
	}

	/**
	 * Returns list of possible numerated/enumerated operators for attribute.
	 * 
	 * @param attributeInterface
	 *            attributeInterface
	 * @return List listOf operators.
	 */
	private List<String> getConditionsList(AttributeInterface attributeInterface)
	{
		List<String> operatorsList = new ArrayList<String>();
		List<String> strObj = null;
		if (attributeInterface != null)
		{
			String dataType = attributeInterface.getDataType();
			UserDefinedDE userDefineDE = (UserDefinedDE) attributeInterface
					.getAttributeTypeInformation().getDataElement();
			if (userDefineDE != null)
			{
				if (dataType.equalsIgnoreCase("long") || dataType.equalsIgnoreCase("double")
						|| dataType.equalsIgnoreCase("short")
						|| dataType.equalsIgnoreCase("integer"))
				{
					operatorsList = parseFile.getEnumConditionList("number");
				}
				else if (dataType.equalsIgnoreCase("string"))
				{
					operatorsList = parseFile.getEnumConditionList("string");
				}
				else if (dataType.equalsIgnoreCase("boolean"))
				{
					operatorsList = parseFile.getEnumConditionList("boolean");
				}
				else if (dataType.equalsIgnoreCase("date"))
				{
					operatorsList = parseFile.getEnumConditionList("date");
				}
			}
			else
			{
				if (dataType.equalsIgnoreCase("long") || dataType.equalsIgnoreCase("double")
						|| dataType.equalsIgnoreCase("short")
						|| dataType.equalsIgnoreCase("integer"))
				{
					operatorsList = parseFile.getNonEnumConditionList("number");
				}
				else if (dataType.equalsIgnoreCase("string"))
				{
					operatorsList = parseFile.getNonEnumConditionList("string");
				}
				else if (dataType.equalsIgnoreCase("boolean"))
				{
					operatorsList = parseFile.getNonEnumConditionList("boolean");
				}
				else if (dataType.equalsIgnoreCase("date"))
				{
					operatorsList = parseFile.getNonEnumConditionList("date");
				}
				else if (dataType.equalsIgnoreCase(Constants.FILE_TYPE))
				{
					operatorsList = parseFile.getNonEnumConditionList(Constants.FILE_TYPE);
				}
			}
			strObj = new ArrayList<String>(operatorsList);;
			operatorsList = new ArrayList<String>();
			Collections.sort(strObj);
			for (int i = 0; i < strObj.size(); i++)
			{
				if (strObj.get(i) != null)
					operatorsList.add((String) strObj.get(i));
			}
		}
		return operatorsList;
	}

	/**
	 * This method generates the combobox's html to show the operators valid for
	 * the attribute passed to it.
	 * 
	 * @param attribute
	 *            AttributeInterface
	 * @param operatorsList
	 *            list of operators for each attribute
	 * @return String HTMLForOperators
	 */
	private String generateHTMLForOperators(AttributeInterface attribute, List<String> operatorsList,
			String op, String cssClass)
	{
		StringBuffer html = new StringBuffer();
		//String attributeName = attribute.getName();
		String componentId = generateComponentName(attribute);
		if (operatorsList != null && operatorsList.size() != 0)
		{
			html.append("\n<td width='15%' class=" + cssClass + " valign='top' >");
			AttributeTypeInformationInterface attrTypeInfo = attribute
					.getAttributeTypeInformation();
			if (attrTypeInfo instanceof DateAttributeTypeInformation)
			{
				html
						.append("\n<select   class=" + cssClass
								+ " style=\"width:150px; display:block;\" name=\"" + componentId
								+ "_combobox\" onChange=\"operatorChanged('" + componentId
								+ "','true')\">");
			}
			else
			{
				html.append("\n<select  class=" + cssClass
						+ " style=\"width:150px; display:block;\" name=\"" + componentId
						+ "_combobox\" onChange=\"operatorChanged('" + componentId
						+ "','false')\">");
			}
			Iterator<String> iter = operatorsList.iterator();

			while (iter.hasNext())
			{
				String operator = iter.next().toString();
				//String op1 = operator.replace(" ", "");
				if (operator.equalsIgnoreCase(op))
				{
					html.append("\n<option  class=" + cssClass + " value=\"" + operator
							+ "\" SELECTED>" + operator + "</option>");
				}
				else
				{
					html.append("\n<option  class=" + cssClass + " value=\"" + operator + "\">"
							+ operator + "</option>");
				}
			}
			html.append("\n</select>");
			html.append("\n</td>");
		}
		return html.toString();
	}

	/**
	 * Generates html for textBox to hold the input for operator selected.
	 * 
	 * @param attributeInterface
	 *            attribute
	 * @param isBetween
	 *            boolean
	 * @return String HTMLForTextBox
	 */
	private String generateHTMLForTextBox(AttributeInterface attributeInterface, boolean isBetween,
			List<String> values, String op, String cssClass)
	{
		String componentId = generateComponentName(attributeInterface);
		String textBoxId = componentId + "_textBox";
		String textBoxId1 = componentId + "_textBox1";
		String dataType = attributeInterface.getDataType();
		StringBuffer html = new StringBuffer();
		html.append("<td width='15%' valign='top' class=\"standardTextQuery\">\n");
		if (values == null || values.isEmpty())
		{
			if(op != null)
			{
				if(op.equalsIgnoreCase(Constants.IS_NOT_NULL) || op.equalsIgnoreCase(Constants.IS_NULL))
				{
					html.append("<input style=\"width:150px; display:block;\" type=\"text\" disabled='true' name=\""
							+ textBoxId + "\" id=\"" + textBoxId + "\">");
				}
			}else
			{
				html.append("<input style=\"width:150px; display:block;\" type=\"text\" name=\""
					+ textBoxId + "\" id=\"" + textBoxId + "\">");
			}
		}
		else
		{
			String valueStr = "";
			if (op.equalsIgnoreCase(Constants.In) || op.equalsIgnoreCase(Constants.Not_In))
			{
				valueStr = values.toString();
				valueStr = valueStr.replace("[", "");
				valueStr = valueStr.replace("]", "");
				html.append("<input style=\"width:150px; display:block;\" type=\"text\" name=\""
						+ textBoxId + "\" id=\"" + textBoxId + "\" value=\"" + valueStr + "\">");
			}
			else
			{
				html.append("<input style=\"width:150px; display:block;\" type=\"text\" name=\""
						+ textBoxId + "\" id=\"" + textBoxId + "\" value=\"" + values.get(0)
						+ "\">");
			}
		}
		html.append("\n</td>");
		if (dataType.equalsIgnoreCase(Constants.DATE))
		{
			html.append("\n" + generateHTMLForCalendar(attributeInterface, true, false,cssClass));
			// html.append("\n<td valign='top' nowrap='nowrap' id=\""
			// +dateFormatLabelId1+ "\" class=\"standardTextQuery\"
			// width=\"8%\">"+dateFormat+"</td>");
		}
		else
		{
			html.append("\n<td valign='top' width='1%'>&nbsp;</td>");
			// html.append("\n<td valign='top' />");
		}
		html.append("<td width='15%'  valign='top' class=\"standardTextQuery\">\n");
		if (isBetween)
		{
			if (values == null || values.isEmpty())
			{
				html.append("<input type=\"text\" name=\"" + textBoxId1 + "\" id=\"" + textBoxId1
						+ "\" style=\"display:block\">");
			}
			else
			{
				html.append("<input type=\"text\" name=\"" + textBoxId1 + "\" id=\"" + textBoxId1
						+ "\" value=\"" + values.get(1) + "\" style=\"display:block\">");
			}
		}
		else
		{
			html.append("<input type=\"text\" name=\"" + textBoxId1 + "\" id=\"" + textBoxId1
					+ "\" style=\"display:none\">");
		}
		html.append("\n</td>");
		if (dataType.equalsIgnoreCase(Constants.DATE))
		{
			html.append("\n" + generateHTMLForCalendar(attributeInterface, false, isBetween,cssClass));
			/*
			 * if(!isBetween) { html.append("\n<td valign='top' nowrap='nowrap' id=\"" +dateFormatLabelId2+ "\" class=\"standardTextQuery\" style=\"display:none\" width=\"8%\">"+dateFormat+"</td>"); }
			 * else { html.append("\n<td valign='top' nowrap='nowrap' id=\"" +dateFormatLabelId2+ "\" class=\"standardTextQuery\" style=\"display:block\" width=\"8%\">"+dateFormat+"</td>"); }
			 */
		}
		else
		{
			html.append("\n<td valign='top' />");
			// html.append("\n<td valign='top' />");
		}
		return html.toString();
	}

	/**
	 * Generates html for button.
	 * 
	 * @param entityName
	 *            entityName
	 * @param attributesStr
	 *            attributesStr
	 * @return String HTMLForButton
	 */
	private String generateHTMLForButton(String entityName, String attributesStr,
			boolean isEditLimits, boolean isTopButton)
	{
		String buttonName = "addLimit";
		String buttonId = "";
		StringBuffer html = new StringBuffer();
		// html.append("\n<tr>");
		if (isTopButton)
		{
			buttonId = "TopAddLimitButton";
			html.append("\n<td bgcolor=\"#EAEAEA\" colspan=\"2\" height=\"2%\"valign=\"top\">");
		}
		else
		{
			buttonId = "BottomAddLimitButton";
			html.append("\n<td valign=\"bottom\">");
		}
		String buttonCaption = "Add Limit";
		if (isEditLimits)
		{
			buttonCaption = "Edit Limit";
		}
		html.append("\n<input id=\"" + buttonId + "\" type=\"button\" name=\"" + buttonName
				+ "\" onClick=\"produceQuery('" + buttonId
				+ "', 'addToLimitSet.do', 'categorySearchForm', '" + entityName + "','"
				+ attributesStr + "')\" value=\"" + buttonCaption + "\"></input>");
		html.append("\n</td>");
		// html.append("\n</tr>");
		return html.toString();
	}

	/**
	 * Generators html for Calendar.Depending upon the value of operator the
	 * calendar is displayed(hidden/visible).
	 * 
	 * @param attributeName
	 *            attributeName
	 * @param isFirst
	 *            boolean
	 * @param isBetween
	 *            boolean
	 * @return String HTMLForCalendar
	 */
	private String generateHTMLForCalendar(AttributeInterface attribute, boolean isFirst,
			boolean isBetween, String cssClass)
	{
		String componentId = generateComponentName(attribute);
		String innerStr = "";
		// String divId = "overDiv" + (i + 1);
		
		if (isFirst)
		{
			String textBoxId = componentId + "_textBox";
			String calendarId = componentId + "_calendar";
			String imgStr = "\n<img id=\"calendarImg\" src=\"images/calendar.gif\" width=\"24\" height=\"22\"" +
					" border=\"0\" onclick='scwShow("+ textBoxId + ",event);'>";
			
			innerStr = "\n<td width='3%' class='"+ cssClass +"' valign='top' id=\"" + calendarId + "\">" 
						+ "\n" + imgStr ;
		}
		else
		{
			String textBoxId1 = componentId + "_textBox1";
			String calendarId1 = componentId + "_calendar1";
			String imgStr = "\n<img id=\"calendarImg\" src=\"images/calendar.gif\" width=\"24\" height=\"22\" border='0'" +
					" onclick='scwShow(" + textBoxId1 + ",event);'>";
			String style = "";
			if (isBetween)
			{
				style = "display:block";
			}
			else
			{
				style = "display:none";
			}
			
			innerStr = "\n<td width='3%' class='"+ cssClass +"' valign='top' id=\"" + calendarId1 + "\" style=\"" + style
						+ "\">" 
						+ "\n" + imgStr ;
		}
		innerStr = innerStr + "\n</td>";
		return innerStr.toString();
	}

	/**
	 * This function generates the HTML for enumerated values.
	 * 
	 * @param attribute
	 *            AttributeInterface
	 * @param enumeratedValuesList
	 *            enumeratedValuesList
	 * @param list
	 *            values values' list in case of edit limits
	 * @return String html for enumerated value dropdown
	 */
	private String generateHTMLForEnumeratedValues(AttributeInterface attribute,
			List<PermissibleValueInterface> permissibleValues, List<String> editLimitPermissibleValues, String cssClass)
	{
		StringBuffer html = new StringBuffer();
		//String attributeName = attribute.getName();
		String componentId = generateComponentName(attribute);
		if (permissibleValues != null && permissibleValues.size() != 0)
		{

			html.append("\n<td width='15%' valign='centre' >");
			html
					.append("\n<select style=\"width:150px; display:block;\" MULTIPLE styleId='country' size ='3' name=\""
							+ componentId + "_enumeratedvaluescombobox\"\">");
			List<PermissibleValueInterface> values = new ArrayList<PermissibleValueInterface>(permissibleValues);
			Collections.sort(values, new PermissibleValueComparator());
			for (int i = 0; i < values.size(); i++)
			{
				PermissibleValueInterface perValue = (PermissibleValueInterface) values.get(i);
				String value = perValue.getValueAsObject().toString();
				if (editLimitPermissibleValues != null
						&& editLimitPermissibleValues.contains(value))
				{
					html.append("\n<option class=\"PermissibleValuesQuery\" title=\"" + value
							+ "\" value=\"" + value + "\" SELECTED>" + value + "</option>");

				}
				else
				{
					html.append("\n<option class=\"PermissibleValuesQuery\" title=\"" + value
							+ "\" value=\"" + value + "\">" + value + "</option>");
				}
			}
			html.append("\n</select>");
			html.append("\n</td>");
			html.append("\n<td>");
			html.append("\n</td>");
			html.append("\n<td>");
			html.append("\n</td>");
			html.append("\n<td>");
			html.append("\n</td>");
		}
		return html.toString();

	}

	/**
	 * 
	 * @param attributeCollection
	 * @return
	 */
	String getAttributesString(Collection<AttributeInterface> attributeCollection)
	{
		String attributesList = "";
		if (!attributeCollection.isEmpty())
		{
			List<AttributeInterface> attributes = new ArrayList<AttributeInterface>(attributeCollection);
			Collections.sort(attributes, new AttributeInterfaceComparator());
			for (int i = 0; i < attributes.size(); i++)
			{
				AttributeInterface attribute = (AttributeInterface) attributes.get(i);
				//String attrName = attribute.getName();
				String componentId = generateComponentName(attribute);
				attributesList = attributesList + ";" + componentId;
			}
		}
		return attributesList;
	}

	//	TODO please remove this quick fix for bug :#5462
	private String generateHTMLForRadioButton(AttributeInterface attribute, List<String> values,
			String cssClass)
	{
		StringBuffer html = new StringBuffer();
		String componentId = generateComponentName(attribute) + "_radioButton";
		String componentName = componentId + "_booleanAttribute";
		String radioButtonTrueId = componentId + "_true";
		String radioButtonFalseId = componentId + "_false";

		html.append("\n<td class='" + cssClass + "' >");
		if (values == null)
		{
			html.append("\n<input type='radio' id = '" + componentId
					+ "_true' value='true' onclick=\"resetOptionButton('" + radioButtonTrueId
					+ "',this)\" name='" + componentName + "'/><font class='" + cssClass
					+ "'>True</font>");
			html.append("\n<input type='radio' id = '" + componentId
					+ "_false' value='false' onclick=\"resetOptionButton('" + radioButtonFalseId
					+ "',this)\" name='" + componentName + "'/><font class='" + cssClass
					+ "'>False</font>");
		}
		else
		{
			if (values.get(0).equalsIgnoreCase("true"))
			{
				html.append("\n<input type='radio' id = '" + componentId
						+ "_true' value='true' onclick=\"resetOptionButton('" + radioButtonTrueId
					+ "',this)\" name='" + componentName + "' checked><font  class='"
						+ cssClass + "'>True</font>");
				html.append("\n<input type='radio' id = '" + componentId
						+ "_false' value='false' onclick=\"resetOptionButton('" + radioButtonFalseId
						+ "',this)\" name='" + componentName + "'><font class='"
						+ cssClass + "'>False</font>");
			}
			else
			{
				html.append("\n<input type='radio' id = '" + componentId
						+ "_true' value='true' onclick=\"resetOptionButton('" + radioButtonTrueId
					+ "',this)\" name='" + componentName + "' ><font class='"
						+ cssClass + "'>True</font>");
				html.append("\n<input type='radio' id = '" + componentId
						+ "_false' value='false' onclick=\"resetOptionButton('" + radioButtonFalseId
					+ "',this)\" name='" + componentName
						+ "'  checked><font class='" + cssClass + "'>False</font>");
			}
		}
		html.append("\n</td>");

		html.append("\n<td class='" + cssClass + "'>&nbsp;");
		html.append("\n</td>");
		html.append("\n<td class='" + cssClass + "'>&nbsp;");
		html.append("\n</td>");
		html.append("\n<td class='" + cssClass + "'>&nbsp;");
		html.append("\n</td>");

		return html.toString();
	}

}