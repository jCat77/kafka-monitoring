package com.jcat.kafka.monitor.domain.model;

public class PrometheusConfiguration {

	private String job;
	private String url;

	public String getJob() {
		return job;
	}

	public void setJob(final String job) {
		this.job = job;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}
}
