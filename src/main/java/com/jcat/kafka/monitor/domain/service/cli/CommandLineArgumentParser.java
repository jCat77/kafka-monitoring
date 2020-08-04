package com.jcat.kafka.monitor.domain.service.cli;

import com.jcat.kafka.monitor.domain.model.cli.CommandLineRequest;

public interface CommandLineArgumentParser {

	CommandLineRequest parse(String[] args);

}
