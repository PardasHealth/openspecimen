
package com.krishagni.catissueplus.rest.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.krishagni.catissueplus.core.administrative.events.CreateDepartmentEvent;
import com.krishagni.catissueplus.core.administrative.events.DepartmentCreatedEvent;
import com.krishagni.catissueplus.core.administrative.events.DepartmentDetails;
import com.krishagni.catissueplus.core.administrative.events.DepartmentDisabledEvent;
import com.krishagni.catissueplus.core.administrative.events.DepartmentUpdatedEvent;
import com.krishagni.catissueplus.core.administrative.events.DisableDepartmentEvent;
import com.krishagni.catissueplus.core.administrative.events.DisableUserEvent;
import com.krishagni.catissueplus.core.administrative.events.UpdateDepartmentEvent;
import com.krishagni.catissueplus.core.administrative.events.UserDisabledEvent;
import com.krishagni.catissueplus.core.administrative.services.DepartmentService;
import com.krishagni.catissueplus.core.common.events.EventStatus;

import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.beans.SessionDataBean;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

	@Autowired
	private DepartmentService departmentSvc;

	@Autowired
	private HttpServletRequest httpServletRequest;

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public DepartmentDetails createDepartment(@RequestBody DepartmentDetails departmentDetails) {
		CreateDepartmentEvent event = new CreateDepartmentEvent();
		event.setSessionDataBean(getSession());
		event.setDepartmentDetails(departmentDetails);
		DepartmentCreatedEvent resp = departmentSvc.createDepartment(event);
		if (resp.getStatus() == EventStatus.OK) {
			return resp.getDepartmentDetails();
		}
		return null;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{departmentId}")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public DepartmentDetails updateDepartment(@PathVariable Long departmentId, @RequestBody DepartmentDetails departmentDetails) {
		UpdateDepartmentEvent event = new UpdateDepartmentEvent();
		event.setDepartmentDetails(departmentDetails);
		departmentDetails.setId(departmentId);
		DepartmentUpdatedEvent resp = departmentSvc.updateDepartment(event);
		if (resp.getStatus() == EventStatus.OK) {
			return resp.getDepartmentDetails();
		}
		return null;
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	public String disableDepartment(@PathVariable Long id) {
		DisableDepartmentEvent event = new DisableDepartmentEvent();
		event.setId(id);
		event.setSessionDataBean(getSession());
		DepartmentDisabledEvent resp = departmentSvc.deleteDepartment(event);
		if (resp.getStatus() == EventStatus.OK) {
			return resp.getMessage();
		}
		return null;
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.DELETE, value = "/name={name}")
	@ResponseStatus(HttpStatus.OK)
	public String disableDepartment(@PathVariable String name) {
		DisableDepartmentEvent event = new DisableDepartmentEvent();
		event.setName(name);
		event.setSessionDataBean(getSession());
		DepartmentDisabledEvent resp = departmentSvc.deleteDepartment(event);
		if (resp.getStatus() == EventStatus.OK) {
			return resp.getMessage();
		}
		return null;
	}

	private SessionDataBean getSession() {
		return (SessionDataBean) httpServletRequest.getSession().getAttribute(Constants.SESSION_DATA);
	}
}
	