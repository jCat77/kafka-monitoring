package com.jcat.kafka.monitor.domain.service.operation;

import com.jcat.kafka.monitor.domain.model.request.DescribeOperationRequest;
import com.jcat.kafka.monitor.domain.model.response.DescribeOperationResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface OperationService {

	Future<DescribeOperationResponse> describe(DescribeOperationRequest describeOperationRequest) throws ExecutionException, InterruptedException;
}
