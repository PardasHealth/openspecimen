package com.krishagni.catissueplus.core.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.de.events.ExecuteQueryEventOp;
import com.krishagni.catissueplus.core.de.events.FacetDetail;
import com.krishagni.catissueplus.core.de.events.GetFacetValuesOp;
import com.krishagni.catissueplus.core.de.events.QueryExecResult;
import com.krishagni.catissueplus.core.de.services.QueryService;

import edu.common.dynamicextensions.domain.nui.Container;
import edu.common.dynamicextensions.domain.nui.Control;
import edu.common.dynamicextensions.domain.nui.DataType;
import edu.common.dynamicextensions.domain.nui.LookupControl;

public class DefaultListGenerator implements ListGenerator {

	private QueryService querySvc;

	public void setQuerySvc(QueryService querySvc) {
		this.querySvc = querySvc;
	}

	@Override
	@PlusTransactional
	public ListDetail getList(ListConfig cfg, List<Column> searchCriteria) {
		if (CollectionUtils.isEmpty(cfg.getColumns())) {
			// TODO: Error empty column list
		}

		if (StringUtils.isBlank(cfg.getCriteria())) {
			cfg.setCriteria(StringUtils.EMPTY);
		}

		if (StringUtils.isBlank(cfg.getRestriction())) {
			cfg.setRestriction(StringUtils.EMPTY);
		}

		if (StringUtils.isBlank(cfg.getCriteria()) && StringUtils.isBlank(cfg.getRestriction())) {
			// TODO: No list restricting criteria
		}

		return executeQuery(cfg, getAql(cfg, searchCriteria));
	}

	@Override
	public List<Column> getFilters(ListConfig cfg) {
		Map<String, Container> formsCache = new HashMap<>();
		return cfg.getFilters().stream().map(filter -> getFilterDetail(filter, formsCache)).collect(Collectors.toList());
	}

	@Override
	@PlusTransactional
	public Collection<Object> getExpressionValues(Long cpId, String expr, String searchTerm) {
		GetFacetValuesOp op = new GetFacetValuesOp();
		op.setCpId(cpId);
		op.setFacets(Collections.singletonList(expr));
		op.setSearchTerm(searchTerm);

		ResponseEvent<List<FacetDetail>> resp = querySvc.getFacetValues(new RequestEvent<>(op));
		resp.throwErrorIfUnsuccessful();
		return resp.getPayload().get(0).getValues();
	}

	private String getAql(ListConfig cfg, List<Column> searchCriteria) {
		StringBuilder aql = new StringBuilder()
			.append("select ").append(getSelectExpr(cfg))
			.append(" where ").append(getCriteria(cfg, searchCriteria));

		String orderBy = getOrderExpr(cfg);
		if (StringUtils.isNotBlank(orderBy)) {
			aql.append(" order by ").append(orderBy);
		}

		aql.append(" limit ").append(cfg.getStartAt()).append(", ").append(cfg.getMaxResults());
		return aql.toString();
	}

	private String getSelectExpr(ListConfig cfg) {
		StringBuilder select = new StringBuilder();

		String hidden = getSelectExpr(cfg.getHiddenColumns());
		if (StringUtils.isNotBlank(hidden)) {
			select.append(hidden).append(", ");
		}

		return select.append(getSelectExpr(cfg.getColumns())).toString();
	}

	private String getSelectExpr(List<Column> columns) {
		return columns.stream().map(this::getSelectExpr).collect(Collectors.joining(", "));
	}

	private String getSelectExpr(Column column) {
		String expr = column.getExpr();
		if (StringUtils.isNotBlank(column.getCaption())) {
			expr += " as \"" + column.getCaption() + "\"";
		}

		return expr;
	}

	private String getCriteria(ListConfig cfg, List<Column> searchCriteria) {
		StringBuilder criteria = new StringBuilder();

		if (StringUtils.isNotBlank(cfg.getCriteria())) {
			criteria.append("(").append(cfg.getCriteria()).append(")");
		}

		if (StringUtils.isNotBlank(cfg.getRestriction())) {
			if (criteria.length() != 0) {
				criteria.append(" and ");
			}

			criteria.append("(").append(cfg.getRestriction()).append(")");
		}

		String searchCriteriaAql = getSearchCriteria(cfg, searchCriteria);
		if (StringUtils.isNotBlank(searchCriteriaAql)) {
			if (criteria.length() != 0) {
				criteria.append(" and ");
			}

			criteria.append("(").append(searchCriteriaAql).append(")");
		}

		return criteria.toString();
	}

	private String getSearchCriteria(ListConfig cfg, List<Column> searchCriteria) {
		if (CollectionUtils.isEmpty(searchCriteria)) {
			return StringUtils.EMPTY;
		}

		if (CollectionUtils.isEmpty(cfg.getFilters())) {
			// TODO: error out when no filters are pre-configured
		}

		Map<String, Column> filtersMap = cfg.getFilters().stream()
			.collect(Collectors.toMap(column -> column.getExpr(), column -> column));

		List<String> invalidFilters = new ArrayList<>();
		Map<String, Container> formsCache = new HashMap<>();
		List<String> aqls = new ArrayList<>();
		for (Column criterion : searchCriteria) {
			Column criterionCfg = filtersMap.get(criterion.getExpr());
			if (criterionCfg == null) {
				invalidFilters.add(criterion.getExpr());
				continue;
			}

			String criterionAql = getSearchCriterion(criterionCfg, criterion, formsCache);
			if (StringUtils.isBlank(criterionAql)) {
				continue;
			}

			aqls.add(criterionAql);
		}

		if (CollectionUtils.isNotEmpty(invalidFilters)) {
			// TODO: error out specifying invalid filter expressions
		}

		return aqls.stream().collect(Collectors.joining(" and "));
	}

	private String getSearchCriterion(Column criterionCfg, Column criterion, Map<String, Container> formsCache) {
		if (CollectionUtils.isEmpty(criterion.getValues())) {
			return null;
		}

		if (criterionCfg.isTemporal()) {
			return getRangeAql(criterion.getExpr(), criterion.getValues());
		}

		Control field = getField(criterion.getExpr(), formsCache);
		DataType type = field.getDataType();
		if (field instanceof LookupControl) {
			type = ((LookupControl) field).getValueType();
		}

		switch (type) {
			case INTEGER:
			case FLOAT:
			case DATE:
			case DATE_INTERVAL:
				return getRangeAql(criterion.getExpr(), criterion.getValues());

			default:
				String searchType = criterionCfg.getSearchType();
				if (StringUtils.isNotBlank(searchType) && searchType.equalsIgnoreCase("contains")) {
					return getContainsAql(criterion.getExpr(), criterion.getValues());
				} else {
					return getInAql(criterion.getExpr(), criterion.getValues());
				}
		}
	}

	private String getRangeAql(String expr, List<Object> values) {
		String min = null, max = null;
		if (values.size() == 1) {
			Object minVal = values.get(0);
			if (minVal != null) {
				min = minVal.toString();
			}
		} else {
			Object minVal = values.get(0);
			if (minVal != null) {
				min = minVal.toString();
			}

			Object maxVal = values.get(1);
			if (maxVal != null) {
				max = maxVal.toString();
			}
		}

		if (StringUtils.isBlank(min) && StringUtils.isBlank(max)) {
			return null;
		} else if (StringUtils.isBlank(max)) {
			return expr + " >= " + min;
		} else if (StringUtils.isBlank(min)) {
			return expr + " <= " + max;
		} else {
			return expr + " between(" + min + ", " + max + ")";
		}
	}

	private String getContainsAql(String expr, List<Object> values) {
		Object value = values.get(0);
		if (value == null) {
			return null;
		}

		String strValue = value.toString();
		if (StringUtils.isBlank(strValue)) {
			return null;
		}

		return expr + " contains \"" + strValue + "\"";
	}

	private String getInAql(String expr, List<Object> values) {
		String inVals = values.stream()
			.map(value -> value != null ? "\"" + value.toString() + "\"" : null)
			.filter(value -> StringUtils.isNotBlank(value))
			.collect(Collectors.joining(", "));

		if (StringUtils.isBlank(inVals)) {
			return null;
		}

		return expr + " in (" + inVals + ")";
	}

	private String getOrderExpr(ListConfig cfg) {
		return cfg.getOrderBy().stream().map(this::getOrderExpr).collect(Collectors.joining(", "));
	}

	private String getOrderExpr(Column column) {
		String expr = column.getExpr();
		if ("desc".equals(column.getDirection())) {
			expr += " desc";
		}

		return expr;
	}

	private ListDetail executeQuery(ListConfig cfg, String aql) {
		ExecuteQueryEventOp op = new ExecuteQueryEventOp();
		op.setAql(aql);
		op.setCpId(cfg.getCpId());
		op.setRunType("Data");
		op.setDrivingForm(StringUtils.isBlank(cfg.getDrivingForm()) ? "Participant" : cfg.getDrivingForm());
		op.setWideRowMode("OFF");

		RequestEvent<ExecuteQueryEventOp> req = new RequestEvent<>(op);
		ResponseEvent<QueryExecResult> resp = querySvc.executeQuery(req);
		resp.throwErrorIfUnsuccessful();

		return getListDetail(cfg, resp.getPayload());
	}

	private ListDetail getListDetail(ListConfig cfg, QueryExecResult result) {
		List<Row> rows = new ArrayList<>();
		for (String[] rowData : result.getRows()) {
			Map<String, Object> hidden = new HashMap<>();
			int colIdx = 0;
			for (Column hiddenColumn : cfg.getHiddenColumns()) {
				hidden.put(hiddenColumn.getCaption(), rowData[colIdx++]);
			}

			Row row = new Row();
			row.setHidden(hidden);
			row.setData(ArrayUtils.subarray(rowData, colIdx, rowData.length));
			rows.add(row);
		}

		ListDetail listDetail = new ListDetail();
		listDetail.setColumns(cfg.getColumns());
		listDetail.setRows(rows);
		return listDetail;
	}

	private Column getFilterDetail(Column input, Map<String, Container> formsCache) {
		Column result = new Column();
		BeanUtils.copyProperties(input, result);

		if (input.isTemporal()) {
			result.setDataType(DataType.INTEGER.name());
			return result;
		}

		Control field = getField(input.getExpr(), formsCache);
		DataType type = field.getDataType();
		if (field instanceof LookupControl) {
			type = ((LookupControl) field).getValueType();
		}

		result.setDataType(type.name());
		return result;
	}

	private Control getField(String expr, Map<String, Container> formsCache) {
		String[] exprParts = expr.split("\\.");

		String formName = null, fieldName = null;
		if (exprParts[1].equals("extensions") || exprParts[1].equals("customFields")) {
			if (exprParts.length < 4) {
				throw new IllegalArgumentException("Invalid expression: " + expr);
			}

			formName = exprParts[2];
			fieldName = StringUtils.join(exprParts, ".", 3, exprParts.length);
		} else {
			formName = exprParts[0];
			fieldName = StringUtils.join(exprParts, ".", 1, exprParts.length);
		}

		Container form = formsCache.get(formName);
		if (form == null) {
			form = Container.getContainer(formName);
			if (form == null) {
				throw new IllegalArgumentException("Invalid expression: " + expr);
			}

			formsCache.put(formName, form);
		}

		Control field = form.getControlByUdn(fieldName, "\\.");
		if (field == null) {
			throw new IllegalArgumentException("Invalid filter: " + expr);
		}

		return field;
	}
}
