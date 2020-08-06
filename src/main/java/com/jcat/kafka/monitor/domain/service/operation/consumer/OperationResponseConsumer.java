package com.jcat.kafka.monitor.domain.service.operation.consumer;

import com.jcat.kafka.monitor.domain.model.response.OperationResponse;

import java.util.concurrent.Future;

public interface OperationResponseConsumer {

	Future<?> consume(OperationResponse operationResponse);

}
