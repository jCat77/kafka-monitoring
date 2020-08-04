package com.jcat.kafka.monitor;

import com.jcat.kafka.monitor.domain.model.cli.CommandLineRequest;
import com.jcat.kafka.monitor.domain.model.task.DescribeTask;
import com.jcat.kafka.monitor.domain.service.ApplicationFactoryImpl;
import com.jcat.kafka.monitor.domain.service.operation.OperationServiceImpl;
import com.jcat.kafka.monitor.domain.service.cli.CommandLineArgumentParser;
import com.jcat.kafka.monitor.domain.service.cli.CommandLineArgumentParserImpl;
import com.jcat.kafka.monitor.domain.service.validate.RequestValidator;
import com.jcat.kafka.monitor.domain.service.validate.RequestValidatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) throws InterruptedException {

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
				thread.setDaemon(true);
				thread.setName("kafka-monitor-worker-" + counter.getAndIncrement());
				return thread;
			}
		};
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(threadFactory);

		ApplicationFactoryImpl factory = new ApplicationFactoryImpl();
		OperationServiceImpl operationService = new OperationServiceImpl();
		operationService.setAdminClient(factory.createAdminClient(commandLineRequest));
		operationService.setKafkaConsumer(factory.createKafkaConsumer(commandLineRequest));

		DescribeTask describeTask = new DescribeTask(commandLineRequest);
		describeTask.setOperationService(operationService);

		describeTask.setOperationResponseWriter(factory.createOperationResponseWriter(commandLineRequest));

		scheduledExecutorService.scheduleWithFixedDelay(describeTask, 0, 2, TimeUnit.SECONDS);

		System.out.println("Started...");
		Thread.currentThread().join();
		System.out.println("Stopped");
	}

}
