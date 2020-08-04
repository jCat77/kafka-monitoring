package com.jcat.kafka.monitor.domain.model.task;

import com.jcat.kafka.monitor.domain.model.cli.CommandLineRequest;

public abstract class AbstractTask implements Runnable {

	private CommandLineRequest commandLineRequest;

	public AbstractTask(final CommandLineRequest commandLineRequest) {
		this.commandLineRequest = commandLineRequest;
	}

	public CommandLineRequest getCommandLineRequest() {
		return commandLineRequest;
	}
}
