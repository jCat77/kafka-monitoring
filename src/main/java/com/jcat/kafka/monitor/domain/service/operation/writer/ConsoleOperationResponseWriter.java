package com.jcat.kafka.monitor.domain.service.operation.writer;

import com.jcat.kafka.monitor.domain.model.response.DescribeOperationResponse;
import com.jcat.kafka.monitor.domain.model.response.OperationResponse;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsoleOperationResponseWriter implements OperationResponseWriter {

	private static final ExecutorService CONSOLE_WRITER_EXECUTOR;

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
				System.out.println(map(describeOperationResponse));
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
					System.out.println(map(describeOperationResponse));
				}
			} catch (Exception e) {
				//ignore or log it, whar we can do here
				System.out.println("Some error occurred when try to handle response");
			}
		});
	}

	private String map(DescribeOperationResponse describeOperationResponse) {
		StringBuilder stringBuilder = new StringBuilder();
		describeOperationResponse.getGroupDescriptions().forEach(gd -> {
			gd.getMembers().forEach(m -> {
				m.getAssignment().getTopicPartitions().stream().forEach(tp -> {
					System.out.format("%-30s", gd.getGroupId());
					System.out.format("%-15s", tp.getTopic());
					System.out.format("%-4d", tp.getPartition());
					DescribeOperationResponse.ConsumerGroupDescriptionFull.MemberDescription.MemberAssignment.TopicPartition.OffsetMetadata offsetMetadata = tp.getOffsetMetadata();
					System.out.format("%-15d", offsetMetadata.getOffset());
					System.out.format("%-15d",tp.getTopicLogEndOffset());
					//lag here
					System.out.format("%-10d",calculateLag(tp.getTopicLogEndOffset(), offsetMetadata.getOffset()));
					System.out.format("%-50s",m.getMemberId());
					System.out.format("%-15s",m.getClientId());
					System.out.format("%-20s",m.getHost());
					System.out.println();
				});
			});
		});
		return stringBuilder.toString();
	}

	public long calculateLag(long topicOffset, long cgOffset) {
		return topicOffset - cgOffset;
	}

}
