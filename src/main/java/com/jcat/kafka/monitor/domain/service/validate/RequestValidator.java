package com.jcat.kafka.monitor.domain.service.validate;

import com.jcat.kafka.monitor.domain.model.cli.CommandLineRequest;

public interface RequestValidator {

	void validate(CommandLineRequest commandLineRequest) throws Exception;
}
