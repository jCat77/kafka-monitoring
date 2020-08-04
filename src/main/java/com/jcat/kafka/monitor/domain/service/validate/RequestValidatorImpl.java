package com.jcat.kafka.monitor.domain.service.validate;

import com.jcat.kafka.monitor.domain.model.Operation;
import com.jcat.kafka.monitor.domain.model.cli.CommandLineRequest;

public class RequestValidatorImpl implements RequestValidator {

	private RequestValidator describeValidator = new DescribeOperationValidator();

	@Override
	public void validate(final CommandLineRequest commandLineRequest) throws Exception {
		Operation operation = commandLineRequest.getOperation();
		if (operation == null) {
			throw new IllegalArgumentException("No operation passed. Please specify the operation");
		}
		switch (operation) {
			case describe:
				describeValidator.validate(commandLineRequest);
				break;
			default:
		}
	}
}
