package com.jcat.kafka.monitor.domain.service;

import com.jcat.kafka.monitor.domain.model.cli.CommandLineRequest;
import com.jcat.kafka.monitor.domain.service.operation.consumer.OperationResponseConsumer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public interface ApplicationFactory {

	KafkaConsumer<?, ?> createKafkaConsumer(CommandLineRequest commandLineRequest);
	AdminClient createAdminClient(CommandLineRequest commandLineRequest);
	OperationResponseConsumer createOperationResponseWriter(CommandLineRequest commandLineRequest);
}
