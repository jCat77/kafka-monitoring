package com.jcat.kafka.monitor.domain.service.operation.consumer;

import com.jcat.kafka.monitor.domain.model.response.DescribeOperationResponse;
import com.jcat.kafka.monitor.domain.model.response.OperationResponse;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsoleWriterOperationResponseConsumer implements OperationResponseConsumer {

	private static final ExecutorService CONSOLE_WRITER_EXECUTOR;
	private static final String EMPTY_VALUE = "-";

	static {

		ThreadFactory threadFactory = new ThreadFactory() {

			private final AtomicInteger counter = new AtomicInteger(0);

			@Override
			public Thread newThread(final Runnable r) {
				Thread thread = new Thread(r);
				thread.setDaemon(true);
				thread.setName("console-writer-worker-" + counter.getAndIncrement());
				return thread;
			}
		};
		CONSOLE_WRITER_EXECUTOR = Executors.newSingleThreadExecutor(threadFactory);
	}

	@Override
	public Future<?> consume(final OperationResponse operationResponse) {
		if (operationResponse instanceof DescribeOperationResponse) {
			DescribeOperationResponse describeOperationResponse = (DescribeOperationResponse) operationResponse;
			return CONSOLE_WRITER_EXECUTOR.submit(() -> {
				writeDescrineOperationResponseInternal(describeOperationResponse);
			});
		} else {
			throw new RuntimeException("unknown response type");
		}
	}

	private void writeDescrineOperationResponseInternal(DescribeOperationResponse describeOperationResponse) {
		describeOperationResponse.getTopicPartitions().forEach(tp -> {
			System.out.format("%-30s", tp.getGroup());
			System.out.format("%-15s", tp.getTopic());
			System.out.format("%-4d", tp.getPartition());
			Optional<DescribeOperationResponse.TopicPartitionInfo.Offset> optionalOffset = Optional.ofNullable(tp.getOffset());
			System.out.format("%-15s", optionalOffset.map(DescribeOperationResponse.TopicPartitionInfo.Offset::getGroup).map(Object::toString).orElse(EMPTY_VALUE));
			System.out.format("%-15s", optionalOffset.map(DescribeOperationResponse.TopicPartitionInfo.Offset::getTopic).map(Object::toString).orElse(EMPTY_VALUE));
			//lag here
			System.out.format("%-10d", tp.getLag());
			Optional<DescribeOperationResponse.TopicPartitionInfo.MemberInfo> m = Optional.ofNullable(tp.getMemberInfo());
			System.out.format("%-50s", m.map(DescribeOperationResponse.TopicPartitionInfo.MemberInfo::getId).orElse(EMPTY_VALUE));
			System.out.format("%-15s", m.map(DescribeOperationResponse.TopicPartitionInfo.MemberInfo::getClientId).orElse(EMPTY_VALUE));
			System.out.format("%-20s", m.map(DescribeOperationResponse.TopicPartitionInfo.MemberInfo::getHost).orElse(EMPTY_VALUE));
			System.out.println();
		});
	}
}
