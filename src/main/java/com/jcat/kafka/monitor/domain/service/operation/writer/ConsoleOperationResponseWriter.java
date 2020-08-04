package com.jcat.kafka.monitor.domain.service.operation.writer;

import com.jcat.kafka.monitor.domain.model.response.DescribeOperationResponse;
import com.jcat.kafka.monitor.domain.model.response.OperationResponse;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsoleOperationResponseWriter implements OperationResponseWriter {

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
	public void write(final OperationResponse operationResponse) {
		if (operationResponse instanceof DescribeOperationResponse) {
			DescribeOperationResponse describeOperationResponse = (DescribeOperationResponse) operationResponse;
			CONSOLE_WRITER_EXECUTOR.submit(() -> {
				writeInternal(describeOperationResponse);
			});
		}
	}

	@Override
	public Future<?> writeAsync(final Future<? extends OperationResponse> futureOperationResponse) {
		return CONSOLE_WRITER_EXECUTOR.submit(() -> {
			try {
				OperationResponse operationResponse = futureOperationResponse.get();
				if (operationResponse instanceof DescribeOperationResponse) {
					DescribeOperationResponse describeOperationResponse = (DescribeOperationResponse) operationResponse;
					//sout
					writeInternal(describeOperationResponse);
				}
			} catch (Exception e) {
				//ignore or log it, whar we can do here
				System.out.println("Some error occurred when try to handle response");
			}
		});
	}

	private void writeInternal(DescribeOperationResponse describeOperationResponse) {
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
