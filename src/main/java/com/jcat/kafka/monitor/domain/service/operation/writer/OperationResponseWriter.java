package com.jcat.kafka.monitor.domain.service.operation.writer;

import com.jcat.kafka.monitor.domain.model.Out;
import com.jcat.kafka.monitor.domain.model.response.OperationResponse;

import java.util.concurrent.Future;

public interface OperationResponseWriter {

	void write(OperationResponse operationResponse);

	Future<?> writeAsync(Future<? extends OperationResponse> futureOperationResponse);
}
