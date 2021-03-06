package org.blazer.scheduler.entity;

import java.util.List;

public class Job {

	private Integer id;
	private Integer type;
	private String jobName;
	private String cron;
	private String command;
	private List<JobParam> params;

	public String[] toArrayParams() {
		if (params == null) {
			return new String[0];
		}
		String[] arr = new String[params.size()];
		for (int i = 0; i < params.size(); i++) {
			arr[i] = params.get(i).getParamName() + "=" + params.get(i).getDefaultValue();
		}
		return arr;
	}

	public String toStrParams() {
		if (params == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < params.size(); i++) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			sb.append(params.get(i).getParamName() + "=" + params.get(i).getDefaultValue());
		}
		return sb.toString();
	}

	public List<JobParam> getParams() {
		return params;
	}

	public void setParams(List<JobParam> params) {
		this.params = params;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public String toString() {
		return "Job [id=" + id + ", type=" + type + ", jobName=" + jobName + ", cron=" + cron + ", command=" + command + "]";
	}

}
