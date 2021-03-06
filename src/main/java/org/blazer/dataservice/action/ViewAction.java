package org.blazer.dataservice.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blazer.dataservice.body.Body;
import org.blazer.dataservice.body.DataSourceBody;
import org.blazer.dataservice.body.GroupBody;
import org.blazer.dataservice.body.PageBody;
import org.blazer.dataservice.body.TreeBody;
import org.blazer.dataservice.body.view.ViewConfigBody;
import org.blazer.dataservice.body.view.ViewMappingConfigJobBody;
import org.blazer.dataservice.cache.ConfigCache;
import org.blazer.dataservice.cache.DataSourceCache;
import org.blazer.dataservice.entity.MappingConfigJob;
import org.blazer.dataservice.model.ConfigModel;
import org.blazer.dataservice.model.DataSourceModel;
import org.blazer.dataservice.service.ViewService;
import org.blazer.dataservice.util.IntegerUtil;
import org.blazer.scheduler.entity.Task;
import org.blazer.scheduler.model.ResultModel;
import org.blazer.scheduler.model.TaskLog;
import org.blazer.userservice.core.filter.PermissionsFilter;
import org.blazer.userservice.core.model.CheckUrlStatus;
import org.blazer.userservice.core.model.SessionModel;
import org.blazer.userservice.core.model.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller(value = "viewAction")
@RequestMapping("/view")
public class ViewAction extends BaseAction {

	private static Logger logger = LoggerFactory.getLogger(ViewAction.class);

	@Autowired
	ViewService viewService;

	@Autowired
	DataSourceCache dataSourceCache;

	@Autowired
	ConfigCache configCache;

	@ResponseBody
	@RequestMapping("/cancelTaskByName")
	public Body cancelTaskByName(HttpServletRequest request, HttpServletResponse response) {
		try {
			viewService.cancelTaskByName(getParamMap(request));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new Body().error().setMessage(e.getMessage());
		}
		return new Body().success();
	}

	@ResponseBody
	@RequestMapping("/findReportByTaskName")
	public ResultModel findReportByTaskName(HttpServletRequest request, HttpServletResponse response) {
		try {
			return viewService.findReportByTaskName(getParamMap(request));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ResultModel();
	}

	@ResponseBody
	@RequestMapping("/findTaskLogByName")
	public TaskLog findTaskLogByName(HttpServletRequest request, HttpServletResponse response) {
		try {
			return viewService.findTaskLogByName(getParamMap(request));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new TaskLog();
	}

	@ResponseBody
	@RequestMapping("/findTaskByName")
	public Task findTaskByName(HttpServletRequest request, HttpServletResponse response) {
		try {
			return viewService.findTaskByName(getParamMap(request));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new Task();
	}

	@ResponseBody
	@RequestMapping("/findTaskByUser")
	public PageBody<Task> findTaskByUser(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, String> params = getParamMap(request);
		SessionModel sm = PermissionsFilter.getSessionModel(request);
		try {
			return viewService.findTaskByUser(params, sm);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new PageBody<Task>();
	}

	@ResponseBody
	@RequestMapping("/findTaskByAdmin")
	public PageBody<Map<String, Object>> findTaskByAdmin(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, String> params = getParamMap(request);
		SessionModel sm = PermissionsFilter.getSessionModel(request);
		try {
			return viewService.findTaskByAdmin(params, sm);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new PageBody<Map<String, Object>>();
	}

	@ResponseBody
	@RequestMapping("/addTask")
	public Body addTask(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, String> params = getParamMap(request);
		SessionModel sm = PermissionsFilter.getSessionModel(request);
		Task t = null;
		try {
			t = viewService.addTask(params, sm);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return fail().setMessage(e.getMessage());
		}
		return success().setMessage(t.getTaskName());
	}

	@ResponseBody
	@RequestMapping("/saveScheduler")
	public Body saveScheduler(HttpServletRequest request, HttpServletResponse response, @RequestBody MappingConfigJob s) {
		logger.debug(s.toString());
		return success().setMessage("保存成功！");
	}

	@ResponseBody
	@RequestMapping("/saveSchedulers")
	public Body saveSchedulers(HttpServletRequest request, HttpServletResponse response, @RequestBody ViewMappingConfigJobBody vBody) {
		SessionModel sm = PermissionsFilter.getSessionModel(request);
		try {
			viewService.saveMappingConfigJob(vBody, sm);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return fail().setMessage(e.getMessage());
		}
		return success().setMessage("保存成功！");
	}

	@ResponseBody
	@RequestMapping("/findSchedulersAll")
	public List<MappingConfigJob> findSchedulersAll(HttpServletRequest request, HttpServletResponse response) {
		// HashMap<String, String> params = getParamMap(request);
		List<MappingConfigJob> list = null;
		try {
			list = viewService.findSchedulersAll();
		} catch (Exception e) {
			list = new ArrayList<MappingConfigJob>();
		}
		return list;
	}

	@ResponseBody
	@RequestMapping("/findSchedulers")
	public List<MappingConfigJob> findSchedulers(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, String> params = getParamMap(request);
		List<MappingConfigJob> list = null;
		try {
			list = viewService.findSchedulersByConfigId(IntegerUtil.getInt0(params.get("config_id")));
		} catch (Exception e) {
			list = new ArrayList<MappingConfigJob>();
		}
		return list;
	}

	@ResponseBody
	@RequestMapping("/getAllUser")
	public List<UserModel> getAllUser(HttpServletRequest request, HttpServletResponse response) {
		List<UserModel> list = null;
		try {
			list = PermissionsFilter.findAllUserBySystemNameAndUrl("isuser");
		} catch (Exception e) {
			list = new ArrayList<UserModel>();
			logger.error(e.getMessage(), e);
		}
		return list;
	}

	@ResponseBody
	@RequestMapping("/getUserGroupIds")
	public List<Integer> getUserGroupIds(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		List<Integer> list = viewService.findUserGroupIds(getParamMap(request));
		return list;
	}

	@ResponseBody
	@RequestMapping("/saveUserGroup")
	public Body saveUserGroup(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, String> params = getParamMap(request);
		try {
			viewService.saveUserGroup(params);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return fail().setMessage(e.getMessage());
		}
		return success().setMessage("保存成功！");
	}

	@ResponseBody
	@RequestMapping("/getTreeAll")
	public List<TreeBody> getTreeAll(HttpServletRequest request, HttpServletResponse response) {
		List<TreeBody> list = null;
		try {
			list = viewService.findTreeAllByParentId(-1);
		} catch (Exception e) {
			list = new ArrayList<TreeBody>();
			logger.error(e.getMessage(), e);
		}
		return list;
	}

	@ResponseBody
	@RequestMapping("/getTree")
	public List<GroupBody> getGroup(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		SessionModel sm = PermissionsFilter.getSessionModel(request);
		logger.debug("session model : " + sm);
		try {
			CheckUrlStatus cus = PermissionsFilter.checkUrl(sm, "isadmin");
			if (cus == CheckUrlStatus.Success) {
				return viewService.findTreeById(getParamMap(request), sm);
			}
			cus = PermissionsFilter.checkUrl(sm, "isuser");
			if (cus == CheckUrlStatus.Success) {
				return viewService.findTreeByIdAndUserId(getParamMap(request), sm);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		logger.info("返回了空目录。");
		return new ArrayList<GroupBody>();
	}

	@ResponseBody
	@RequestMapping("/getConfigsByGroupId")
	public List<ViewConfigBody> getConfigsByGroupId(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		try {
			return viewService.getConfigsByGroupId(getParamMap(request));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<ViewConfigBody>();
	}

	@ResponseBody
	@RequestMapping("/getConfigsByConfigName")
	public List<ViewConfigBody> getConfigsByConfigName(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		SessionModel sm = PermissionsFilter.getSessionModel(request);
		logger.debug("session model : " + sm);
		try {
			CheckUrlStatus cus = PermissionsFilter.checkUrl(sm, "isadmin");
			if (cus == CheckUrlStatus.Success) {
				return viewService.getConfigsByConfigNameAndAdmin(getParamMap(request));
			}
			cus = PermissionsFilter.checkUrl(sm, "isuser");
			if (cus == CheckUrlStatus.Success) {
				return viewService.getConfigsByConfigNameAndUser(sm, getParamMap(request));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<ViewConfigBody>();
	}

	@ResponseBody
	@RequestMapping("/getConfigById")
	public ViewConfigBody getConfigById(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		return viewService.getConfigById(getParamMap(request));
	}

	@ResponseBody
	@RequestMapping("/saveConfigRemark")
	public Body saveConfigRemark(HttpServletRequest request, HttpServletResponse response, @RequestBody ViewConfigBody viewConfigBody) {
		try {
			viewService.saveConfigRemark(viewConfigBody);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return fail().setMessage(e.getMessage());
		}
		return success().setMessage("保存成功！");
	}

	@ResponseBody
	@RequestMapping("/saveConfig")
	public Body saveConfig(HttpServletRequest request, HttpServletResponse response, @RequestBody ViewConfigBody viewConfigBody) {
		try {
			SessionModel sm = PermissionsFilter.getSessionModel(request);
			viewService.saveConfig(sm, viewConfigBody);
			configCache.initConfigEntity(viewConfigBody.getId());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return fail().setMessage(e.getMessage());
		}
		return success().setMessage("保存成功！");
	}

	@ResponseBody
	@RequestMapping("/moveConfig")
	public Body moveConfig(HttpServletRequest request, HttpServletResponse response) {
		try {
			HashMap<String, String> params = getParamMap(request);
			viewService.moveConfig(params);
			ConfigModel cm = configCache.get(IntegerUtil.getInt(params.get("id")));
			cm.setGroupId(IntegerUtil.getInt(params.get("config_id")));
			configCache.add(cm);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return fail().setMessage(e.getMessage());
		}
		return success().setMessage("保存成功！");
	}

	@ResponseBody
	@RequestMapping("/saveConfigOrderAsc")
	public Body saveConfigOrderAsc(HttpServletRequest request, HttpServletResponse response, @RequestBody List<ViewConfigBody> list) {
		try {
			viewService.saveConfigOrderAsc(list);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return fail().setMessage(e.getMessage());
		}
		return success().setMessage("保存成功！");
	}

	@ResponseBody
	@RequestMapping("/deleteConfig")
	public Body deleteConfig(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, String> params = getParamMap(request);
		try {
			viewService.deleteConfig(params);
			configCache.initConfigEntity(IntegerUtil.getInt0(params.get("id")));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return fail().setMessage(e.getMessage());
		}
		return success().setMessage("删除成功！");
	}

	@ResponseBody
	@RequestMapping("/getDataSourceAll")
	public List<DataSourceBody> getDataSourceAll(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		List<DataSourceBody> list = new ArrayList<DataSourceBody>();
		for (Integer i : dataSourceCache.getKeySet()) {
			DataSourceBody dsb = new DataSourceBody();
			dsb.setId(i);
			DataSourceModel dsm = dataSourceCache.getDataSourceBean(i);
			if (dsm == null) {
				dsb.setDatabaseName("NotFoundDataSource");
				dsb.setTitle("NotFoundDataSource");
				dsb.setRemark("NotFoundDataSource");
			} else {
				dsb.setDatabaseName(dsm.getDatabase_name());
				dsb.setTitle(dsm.getTitle());
				dsb.setRemark(dsm.getRemark());
			}
			list.add(dsb);
		}
		return list;
	}

}
