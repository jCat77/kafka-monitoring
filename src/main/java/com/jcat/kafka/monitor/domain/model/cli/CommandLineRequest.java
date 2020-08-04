package com.jcat.kafka.monitor.domain.model.cli;

import com.jcat.kafka.monitor.domain.model.Operation;
import com.jcat.kafka.monitor.domain.model.Out;

import java.time.Duration;

public class CommandLineRequest {

	private Operation operation;
	private String bootstrapServer;
	private String groups;
	private Integer interval;
	private Out out;

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(final Operation operation) {
		this.operation = operation;
	}

	public String getBootstrapServer() {
		return bootstrapServer;
	}

	public void setBootstrapServer(final String bootstrapServer) {
		this.bootstrapServer = bootstrapServer;
	}

	public String getGroups() {
		return groups;
	}

	public void setGroups(final String groups) {
		this.groups = groups;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(final Integer interval) {
		this.interval = interval;
	}

	public Out getOut() {
		return out;
	}

	public void setOut(final Out out) {
		this.out = out;
	}
}
