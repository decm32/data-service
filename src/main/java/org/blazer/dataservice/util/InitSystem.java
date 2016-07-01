package org.blazer.dataservice.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.blazer.dataservice.dao.CustomJdbcDao;
import org.blazer.dataservice.dao.DSConfigDao;
import org.blazer.dataservice.model.DSConfig;
import org.blazer.dataservice.model.DSConfigDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("initSystem")
public class InitSystem implements InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(InitSystem.class);

	@Autowired
	Properties dataSourceProperties;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Value("#{dataSourceProperties.url}")
	public String url;

	@Value("#{dataSourceProperties.username}")
	public String username;

	@Value("#{dataSourceProperties.password}")
	public String password;

	public void afterPropertiesSet() throws Exception {
		//////////////////////// 加载数据源 ////////////////////////
		List<Map<String, Object>> dataSourceList = jdbcTemplate.queryForList("select id,database_name,title,url,username,password,remark from ds_datasource");
		boolean defaultSource = false;
		for (Map<String, Object> map : dataSourceList) {
			Integer id = Integer.parseInt(map.get("id").toString());
			// id为1是系统默认的连接，如果有则覆盖
			String database_name = map.get("database_name").toString();
			String title = map.get("title").toString();
			String url = map.get("url").toString();
			String username = map.get("username").toString();
			String password = map.get("password").toString();
			String remark = map.get("remark").toString();
			if (id == 1) {
				if (StringUtils.isNotBlank(url) || StringUtils.isNotBlank(username) || StringUtils.isNotBlank(password)) {
					logger.info("检测到配置默认数据源中url、username、password不为空，系统将强行覆盖该数据源为系统配置的datasource.properties里的数据源。");
				}
				initDefaultDataSource();
				defaultSource = true;
			}
			CustomJdbcDao.addDataSource(id, database_name, title, url, username, password, remark);
		}
		if (!defaultSource) {
			initDefaultDataSource();
		}
		//////////////////////// 加载配置项 ////////////////////////
		List<Map<String, Object>> configList = jdbcTemplate.queryForList("select id,datasource_id,config_name,config_type from ds_config");
		for (Map<String, Object> map : configList) {
			DSConfig config = new DSConfig();
			config.setId(Integer.parseInt(map.get("id").toString()));
			if (StringUtils.isBlank((String) map.get("datasource_id"))) {
				config.setDataSource(CustomJdbcDao.getDao(1));
			} else {
				config.setDataSource(CustomJdbcDao.getDao(Integer.parseInt(map.get("datasource_id").toString())));
			}
			config.setConfigName(map.get("config_name").toString());
			config.setConfigType(map.get("config_type").toString());
			List<DSConfigDetail> detailList = new ArrayList<DSConfigDetail>();
			List<Map<String, Object>> rstList = jdbcTemplate.queryForList("select id,datasource_id,config_id,key,values from ds_config");
			for (Map<String, Object> map2 : rstList) {
				DSConfigDetail configDetail = new DSConfigDetail();
				configDetail.setId(Integer.parseInt(map2.get("id").toString()));
				if (StringUtils.isBlank((String) map2.get("datasource_id"))) {
					configDetail.setDataSource(config.getDataSource());
				} else {
					config.setDataSource(CustomJdbcDao.getDao(Integer.parseInt(map2.get("datasource_id").toString())));
				}
				configDetail.setKey(map.get("key").toString());
				configDetail.setValues(map.get("values").toString());
				detailList.add(configDetail);
			}
			config.setDetailList(detailList);
			DSConfigDao.addConfig(config);
		}
	}

	private void initDefaultDataSource() {
		CustomJdbcDao.addDataSource(1, "mysql", "default", url, username, password, "");
	}

	public void reload() {
		try {
			afterPropertiesSet();
		} catch (Exception e) {
			logger.error("重载数据失败", e);
		}
	}

}