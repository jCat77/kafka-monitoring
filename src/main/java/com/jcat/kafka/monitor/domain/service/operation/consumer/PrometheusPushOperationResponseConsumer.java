package com.jcat.kafka.monitor.domain.service.operation.consumer;

import com.jcat.kafka.monitor.domain.model.PrometheusConfiguration;
import com.jcat.kafka.monitor.domain.model.cli.CommandLineRequest;
import com.jcat.kafka.monitor.domain.model.response.DescribeOperationResponse;
import com.jcat.kafka.monitor.domain.model.response.OperationResponse;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class PrometheusPushOperationResponseConsumer implements OperationResponseConsumer {

	private final PushGateway pushGateway;
	private final PrometheusConfiguration pc;

	private static final ExecutorService PROMETHEUS_PUSHER_EXECUTOR_SERVICE;
	private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusPushOperationResponseConsumer.class);

	static final Gauge kafkaConsumerGroupLagGauge = Gauge.build()
			         .name("kafka_consumer_group_lag").help("Kafka consumer group lag for partition")
                    .labelNames("group", "topic", "partition")
					.register();

	static final Gauge kafkaConsumerGroupConsumerNumber = Gauge.build()
			         .name("kafka_consumer_group_consumer_number").help("Kafka consumer group consumer number")
                    .labelNames("group", "topic", "partition")
					.register();

	static {

		ThreadFactory threadFactory = new ThreadFactory() {

			private final AtomicInteger counter = new AtomicInteger(0);

			@Override
			public Thread newThread(final Runnable r) {
				Thread thread = new Thread(r);
				thread.setDaemon(true);
				thread.setName("prometheus-pushgateway-worker-" + counter.getAndIncrement());
				return thread;
			}
		};
		PROMETHEUS_PUSHER_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(threadFactory);
	}

	public PrometheusPushOperationResponseConsumer(final CommandLineRequest commandLineRequest) {
		//create gateway
		this.pc = commandLineRequest.getPrometheusConfiguration();
		this.pushGateway = new PushGateway(pc.getUrl());
	}

	@Override
	public Future<?> consume(final OperationResponse operationResponse) {
		if (operationResponse instanceof DescribeOperationResponse) {
			DescribeOperationResponse describeOperationResponse = (DescribeOperationResponse) operationResponse;
			return PROMETHEUS_PUSHER_EXECUTOR_SERVICE.submit(() -> {
				CollectorRegistry registry = CollectorRegistry.defaultRegistry;
				try {
					collectMetricsFromDescribeOperationResponseInternal(describeOperationResponse, registry);
					pushGateway.pushAdd(registry, pc.getJob());
				} catch (Exception e) {
					LOGGER.error("Error when try to handle response={}", describeOperationResponse, e);
				}
			});
		} else {
			throw new RuntimeException("unknown response type");
		}
	}

	private void collectMetricsFromDescribeOperationResponseInternal(DescribeOperationResponse describeOperationResponse, CollectorRegistry collectorRegistry) {
		describeOperationResponse.getTopicPartitions().forEach(tp -> {
			kafkaConsumerGroupLagGauge.labels(
					tp.getGroup(),
					tp.getTopic(),
					String.valueOf(tp.getPartition()))
					.set(tp.getLag());

			kafkaConsumerGroupConsumerNumber.labels(
					tp.getGroup(),
					tp.getTopic(),
					String.valueOf(tp.getPartition()))
					.inc();
		});
	}

}
