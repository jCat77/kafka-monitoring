package com.jcat.kafka.monitor.domain.service;

import com.jcat.kafka.monitor.domain.model.cli.CommandLineRequest;
import com.jcat.kafka.monitor.domain.service.operation.consumer.ConsoleWriterOperationResponseConsumer;
import com.jcat.kafka.monitor.domain.service.operation.consumer.OperationResponseConsumer;
import com.jcat.kafka.monitor.domain.service.operation.consumer.PrometheusPushOperationResponseConsumer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Properties;

public class ApplicationFactoryImpl implements ApplicationFactory {

	@Override
	public KafkaConsumer<?, ?> createKafkaConsumer(final CommandLineRequest commandLineRequest) {
		//kafka consumer properties
		final Properties kafkaConsumerProperties = new Properties();
		kafkaConsumerProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, commandLineRequest.getBootstrapServer());
		kafkaConsumerProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass().getName());
		kafkaConsumerProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass().getName());
		kafkaConsumerProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "kafka-monitor-consumer-client");

		return new KafkaConsumer<>(kafkaConsumerProperties);
	}

	@Override
	public AdminClient createAdminClient(CommandLineRequest commandLineRequest) {
		//admin client properties
		Properties adminClientProperties = new Properties();
		adminClientProperties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, commandLineRequest.getBootstrapServer());
		adminClientProperties.setProperty(AdminClientConfig.CLIENT_ID_CONFIG, "kafka-monitor-admin-client");

		return AdminClient.create(adminClientProperties);
	}

	@Override
	public OperationResponseConsumer createOperationResponseWriter(final CommandLineRequest commandLineRequest) {
		OperationResponseConsumer responseWriter;
		switch (commandLineRequest.getOut()) {
			case prometheus:
				responseWriter = new PrometheusPushOperationResponseConsumer(commandLineRequest);
				break;
			default:
				responseWriter = new ConsoleWriterOperationResponseConsumer();
		}
		return responseWriter;
	}
}
