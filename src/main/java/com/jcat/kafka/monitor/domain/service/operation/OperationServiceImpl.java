package com.jcat.kafka.monitor.domain.service.operation;

import com.jcat.kafka.monitor.domain.model.request.DescribeOperationRequest;
import com.jcat.kafka.monitor.domain.model.response.DescribeOperationResponse;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class OperationServiceImpl implements OperationService {

	private AdminClient adminClient;
	private KafkaConsumer<String, String> kafkaConsumer;

	@Override
	public Future<DescribeOperationResponse> describe(final DescribeOperationRequest describeOperationRequest) throws ExecutionException, InterruptedException {
		//here some work with admin client
		if (adminClient == null) {
			throw new RuntimeException("Admin client is required for this operation");

		}
		DescribeConsumerGroupsOptions describeConsumerGroupsOptions = new DescribeConsumerGroupsOptions();
		Duration timeoutMs = describeOperationRequest.getTimeoutMs();
		if (timeoutMs != null) {
			describeConsumerGroupsOptions.timeoutMs((int) timeoutMs.get(ChronoUnit.MILLIS));
		}

		CompletableFuture<Map<String, ConsumerGroupDescription>> describeFuture = CompletableFuture.supplyAsync(() -> {
			try {
				return adminClient.describeConsumerGroups(describeOperationRequest.getGroups(), describeConsumerGroupsOptions).all().get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		CompletableFuture<Map<TopicPartition, Long>> topicOffsetFuture = CompletableFuture.supplyAsync(() -> {
			try {
				//here we get topics metadata
				Map<String, List<PartitionInfo>> topicMap = kafkaConsumer.listTopics();
				List<TopicPartition> topicPartitions = topicMap.values().stream()
						.flatMap(Collection::stream)
						.filter(pi -> !pi.topic().startsWith("__")) //remove internal topic partitions (__consumer_offsets) from list
						.map(pi -> new TopicPartition(pi.topic(), pi.partition()))
						.collect(Collectors.toList());
				return kafkaConsumer.endOffsets(topicPartitions);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		Map<String, CompletableFuture<Map<TopicPartition, OffsetAndMetadata>>> consumerGroupOffsetFuturesMap = new HashMap<>();
		describeOperationRequest.getGroups().forEach(gId -> {
			CompletableFuture<Map<TopicPartition, OffsetAndMetadata>> offsetFuture = CompletableFuture.supplyAsync(() -> {
				try {
					return adminClient.listConsumerGroupOffsets(gId).partitionsToOffsetAndMetadata().get();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			consumerGroupOffsetFuturesMap.put(gId, offsetFuture);
		});

		List<Object> allFutures = new ArrayList<>(consumerGroupOffsetFuturesMap.values());
		allFutures.add(describeFuture);
		allFutures.add(topicOffsetFuture);
		CompletableFuture<?>[] allFuturesArray = allFutures.toArray(new CompletableFuture<?>[0]);
		return CompletableFuture.allOf(allFuturesArray).thenApply((voitt) -> {
			try {
				DescribeOperationResponse response = new DescribeOperationResponse();
				Collection<DescribeOperationResponse.TopicPartitionInfo> topicPartitionInfoCollection = new ArrayList<>(consumerGroupOffsetFuturesMap.size());
				response.setTopicPartitions(topicPartitionInfoCollection);

				Map<String, ConsumerGroupDescription> consumerGroupMap = describeFuture.get();
				Map<TopicPartition, Long> topicPartitionOffsetMap = topicOffsetFuture.get();
				consumerGroupOffsetFuturesMap.forEach((consumerGroup, value) -> {
					try {

						//try to get consumerGroupDescription
						ConsumerGroupDescription consumerGroupDescription = consumerGroupMap.get(consumerGroup);

						value.get().forEach((tp, consumerGroupOffset) -> {
							DescribeOperationResponse.TopicPartitionInfo topicPartitionInfo = new DescribeOperationResponse.TopicPartitionInfo();
							topicPartitionInfoCollection.add(topicPartitionInfo);

							topicPartitionInfo.setGroup(consumerGroup);
							topicPartitionInfo.setTopic(tp.topic());
							topicPartitionInfo.setPartition(tp.partition());

							DescribeOperationResponse.TopicPartitionInfo.Offset offset = new DescribeOperationResponse.TopicPartitionInfo.Offset();
							topicPartitionInfo.setOffset(offset);
							offset.setGroup(consumerGroupOffset.offset());
							offset.setTopic(topicPartitionOffsetMap.get(tp));

							topicPartitionInfo.setLag(calculateLag(topicPartitionInfo));

							Collection<MemberDescription> members = consumerGroupDescription.members();
							if (members.size() > 0) {
								//get member for partition here
								MemberDescription memberForPartition = getMemberForPartition(tp, members);
								if (memberForPartition != null) {
									DescribeOperationResponse.TopicPartitionInfo.MemberInfo memberInfo = new DescribeOperationResponse.TopicPartitionInfo.MemberInfo();
									memberInfo.setClientId(memberForPartition.clientId());
									memberInfo.setHost(memberForPartition.host());
									memberInfo.setId(memberForPartition.consumerId());
									topicPartitionInfo.setMemberInfo(memberInfo);
								}
							}
						});

					} catch (Exception ex) {
						//ignore
					}
				});
				return response;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	private MemberDescription getMemberForPartition(final TopicPartition tp, final Collection<MemberDescription> members) {
		return members.stream()
				.filter(md -> md.assignment().topicPartitions().contains(tp))
				.findFirst()
				.orElse(null);
	}

	public AdminClient getAdminClient() {
		return adminClient;
	}

	public void setAdminClient(final AdminClient adminClient) {
		this.adminClient = adminClient;
	}

	public KafkaConsumer<String, String> getKafkaConsumer() {
		return kafkaConsumer;
	}

	public void setKafkaConsumer(final KafkaConsumer<String, String> kafkaConsumer) {
		this.kafkaConsumer = kafkaConsumer;
	}

	private long calculateLag(DescribeOperationResponse.TopicPartitionInfo topicPartitionInfo) {
		return Optional.ofNullable(topicPartitionInfo.getOffset())
				.map(o -> o.getTopic() - o.getGroup())
				.orElse(-1L);
	}
}
