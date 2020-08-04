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
		CompletableFuture<?>[] allFuturesArray = allFutures.toArray(new CompletableFuture<?>[allFutures.size()]);
		return CompletableFuture.allOf(allFuturesArray).thenApply((voitt) -> {
			try {
				Map<String, ConsumerGroupDescription> groupMap = describeFuture.get();
				Map<TopicPartition, Long> topicPartitionOffsetMap = topicOffsetFuture.get();
				List<DescribeOperationResponse.ConsumerGroupDescriptionFull> consumerGroupDescriptionFulls = groupMap.values().stream()
						.map(cg -> {
							try {
								DescribeOperationResponse.ConsumerGroupDescriptionFull consumerGroupDescriptionFull = new DescribeOperationResponse.ConsumerGroupDescriptionFull();
								consumerGroupDescriptionFull.setGroupId(cg.groupId());
								consumerGroupDescriptionFull.setPartitionAssignor(cg.partitionAssignor());
								consumerGroupDescriptionFull.setSimpleConsumerGroup(cg.isSimpleConsumerGroup());
								consumerGroupDescriptionFull.setState(cg.state());

								CompletableFuture<Map<TopicPartition, OffsetAndMetadata>> offsetFuture = consumerGroupOffsetFuturesMap.get(cg.groupId());
								if (offsetFuture == null) {
									throw new RuntimeException("No group id=\"" + cg + "\" contains in offsets");
								}
								Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = offsetFuture.get();
								Set<DescribeOperationResponse.ConsumerGroupDescriptionFull.MemberDescription> memberDescriptions = cg.members().stream()
										.map(memberDescription -> {
											DescribeOperationResponse.ConsumerGroupDescriptionFull.MemberDescription md = new DescribeOperationResponse.ConsumerGroupDescriptionFull.MemberDescription();
											md.setClientId(memberDescription.clientId());
											md.setHost(memberDescription.host());
											md.setMemberId(memberDescription.consumerId());

											DescribeOperationResponse.ConsumerGroupDescriptionFull.MemberDescription.MemberAssignment memberAssignment = new DescribeOperationResponse.ConsumerGroupDescriptionFull.MemberDescription.MemberAssignment();
											md.setAssignment(memberAssignment);

											Set<DescribeOperationResponse.ConsumerGroupDescriptionFull.MemberDescription.MemberAssignment.TopicPartition> collectionTPs = memberDescription.assignment().topicPartitions().stream()
													.map(tp -> {
														OffsetAndMetadata offsetAndMetadata = topicPartitionOffsetAndMetadataMap.get(tp);
														DescribeOperationResponse.ConsumerGroupDescriptionFull.MemberDescription.MemberAssignment.TopicPartition topicPartition = new DescribeOperationResponse.ConsumerGroupDescriptionFull.MemberDescription.MemberAssignment.TopicPartition();
														topicPartition.setPartition(tp.partition());
														topicPartition.setTopic(tp.topic());
														Long topicLogEndOffset = topicPartitionOffsetMap.get(tp);
														topicPartition.setTopicLogEndOffset(topicLogEndOffset == null ? 0L : topicLogEndOffset);

														if (offsetAndMetadata != null) {
															DescribeOperationResponse.ConsumerGroupDescriptionFull.MemberDescription.MemberAssignment.TopicPartition.OffsetMetadata offsetMetadata = new DescribeOperationResponse.ConsumerGroupDescriptionFull.MemberDescription.MemberAssignment.TopicPartition.OffsetMetadata(offsetAndMetadata.offset(), offsetAndMetadata.metadata());
															topicPartition.setOffsetMetadata(offsetMetadata);
														}
														return topicPartition;

													}).collect(Collectors.toSet());
											memberAssignment.setTopicPartitions(collectionTPs);
											return md;

										}).collect(Collectors.toSet());
								consumerGroupDescriptionFull.setMembers(memberDescriptions);
								return consumerGroupDescriptionFull;

							} catch (Exception e) {
								//
								throw new RuntimeException();
							}
						}).collect(Collectors.toList());
				DescribeOperationResponse describeOperationResponse = new DescribeOperationResponse();
				describeOperationResponse.setGroupDescriptions(consumerGroupDescriptionFulls);
				return describeOperationResponse;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
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
}
