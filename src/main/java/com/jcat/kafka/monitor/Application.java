package com.jcat.kafka.monitor;

import com.jcat.kafka.monitor.domain.model.cli.CommandLineRequest;
import com.jcat.kafka.monitor.domain.model.task.DescribeTask;
import com.jcat.kafka.monitor.domain.service.operation.OperationServiceImpl;
import com.jcat.kafka.monitor.domain.service.cli.CommandLineArgumentParser;
import com.jcat.kafka.monitor.domain.service.cli.CommandLineArgumentParserImpl;
import com.jcat.kafka.monitor.domain.service.operation.writer.ConsoleOperationResponseWriter;
import com.jcat.kafka.monitor.domain.service.operation.writer.OperationResponseWriter;
import com.jcat.kafka.monitor.domain.service.validate.RequestValidator;
import com.jcat.kafka.monitor.domain.service.validate.RequestValidatorImpl;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private CommandLineRequest commandLineRequest;

	private CommandLineArgumentParser commandLineArgumentParser;

	public static void main(String[] args) {

		CommandLineArgumentParser commandLineArgumentParser = new CommandLineArgumentParserImpl();
		CommandLineRequest commandLineRequest = commandLineArgumentParser.parse(args);

		//here validate the request
		RequestValidator requestValidator = new RequestValidatorImpl();
		try {
			requestValidator.validate(commandLineRequest);
		} catch (Exception e) {
			//exit
			LOGGER.error("Error due CLI request validation", e);
			System.exit(-1);
		}

		//request is valid try to run
		ThreadFactory threadFactory = new ThreadFactory() {

			private final AtomicInteger counter = new AtomicInteger(0);

			@Override
			public Thread newThread(final Runnable r) {
				Thread thread = new Thread(r);
//				thread.setDaemon(true);
				thread.setName("kafka-monitor-worker-" + counter.getAndIncrement());
				return thread;
			}
		};
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(threadFactory);

		OperationServiceImpl operationService = new OperationServiceImpl();

		//admin client properties
		Properties adminClientProperties = new Properties();
		adminClientProperties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, commandLineRequest.getBootstrapServer());
		adminClientProperties.setProperty(AdminClientConfig.CLIENT_ID_CONFIG, "kafka-monitor-admin-client");

		AdminClient adminClient = AdminClient.create(adminClientProperties);
		operationService.setAdminClient(adminClient);

		//kafka consumer properties
		final Properties kafkaConsumerProperties = new Properties();
		kafkaConsumerProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, commandLineRequest.getBootstrapServer());
		kafkaConsumerProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass().getName());
		kafkaConsumerProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass().getName());
		kafkaConsumerProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "kafka-monitor-consumer-client");

		KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProperties);
		operationService.setKafkaConsumer(kafkaConsumer);

		DescribeTask describeTask = new DescribeTask(commandLineRequest);
		describeTask.setOperationService(operationService);

		OperationResponseWriter responseWriter;
		switch (commandLineRequest.getOut()) {
			case prometheus:
				throw new RuntimeException("Not implemented");
			default:
				responseWriter = new ConsoleOperationResponseWriter();
		}
		describeTask.setOperationResponseWriter(responseWriter);

		scheduledExecutorService.scheduleAtFixedRate(describeTask, 0, 2, TimeUnit.SECONDS);
	}

}
