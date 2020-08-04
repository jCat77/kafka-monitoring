package com.jcat.kafka.monitor.domain.model.task;

import com.jcat.kafka.monitor.domain.model.cli.CommandLineRequest;
import com.jcat.kafka.monitor.domain.model.request.DescribeOperationRequest;
import com.jcat.kafka.monitor.domain.model.response.DescribeOperationResponse;
import com.jcat.kafka.monitor.domain.service.operation.OperationService;
import com.jcat.kafka.monitor.domain.service.operation.writer.OperationResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

public class DescribeTask extends AbstractTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(DescribeTask.class);

	private OperationService operationService;
	private OperationResponseWriter operationResponseWriter;

	public DescribeTask(final CommandLineRequest commandLineRequest) {
		super(commandLineRequest);
	}

	@Override
	public void run() {

		CommandLineRequest commandLineRequest = getCommandLineRequest();
		List<String> groups = Arrays.asList(commandLineRequest.getGroups().split(","));
		DescribeOperationRequest describeOperationRequest = new DescribeOperationRequest();
		describeOperationRequest.setGroups(groups);

		Future<DescribeOperationResponse> future;
		try {
			future = operationService.describe(describeOperationRequest);

			//ok, if we have a result we must consume it
			operationResponseWriter.writeAsync(future);

		} catch (Exception e) {
			//log it and ignore
			LOGGER.error("Error when get data from kafka: {}", e.getMessage(), e);
		}
	}

	public OperationService getOperationService() {
		return operationService;
	}

	public void setOperationService(final OperationService operationService) {
		this.operationService = operationService;
	}

	public OperationResponseWriter getOperationResponseWriter() {
		return operationResponseWriter;
	}

	public void setOperationResponseWriter(final OperationResponseWriter operationResponseWriter) {
		this.operationResponseWriter = operationResponseWriter;
	}
}
