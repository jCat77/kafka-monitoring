package com.jcat.kafka.monitor.domain.model.request;

import java.time.Duration;
import java.util.Collection;

public class DescribeOperationRequest {

	private Collection<String> groups;
	private Duration timeoutMs;

	public Collection<String> getGroups() {
		return groups;
	}

	public void setGroups(final Collection<String> groups) {
		this.groups = groups;
	}

	public Duration getTimeoutMs() {
		return timeoutMs;
	}

	public void setTimeoutMs(final Duration timeoutMs) {
		this.timeoutMs = timeoutMs;
	}
}
