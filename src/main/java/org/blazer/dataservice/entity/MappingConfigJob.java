package org.blazer.dataservice.entity;

import org.blazer.scheduler.entity.Job;

public class MappingConfigJob {

	private Integer id;
	private Integer configId;
	private Integer jobId;
	private Integer userId;
	private Integer resultMode;
	private String email;
	private String emailUserids;
	private Job job;
	private String configName;

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getConfigId() {
		return configId;
	}

	public void setConfigId(Integer configId) {
		this.configId = configId;
	}

	public Integer getJobId() {
		return jobId;
	}

	public void setJobId(Integer jobId) {
		this.jobId = jobId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getResultMode() {
		return resultMode;
	}

	public void setResultMode(Integer resultMode) {
		this.resultMode = resultMode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmailUserids() {
		return emailUserids;
	}

	public void setEmailUserids(String emailUserids) {
		this.emailUserids = emailUserids;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	@Override
	public String toString() {
		return "MappingConfigJob [id=" + id + ", configId=" + configId + ", jobId=" + jobId + ", userId=" + userId + ", resultMode=" + resultMode + ", email=" + email
				+ ", emailUserids=" + emailUserids + ", configName=" + configName + "]";
	}

}
