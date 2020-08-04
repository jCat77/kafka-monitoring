package com.jcat.kafka.monitor.domain.model.response;

import org.apache.kafka.common.ConsumerGroupState;

import java.util.Collection;

public class DescribeOperationResponse implements OperationResponse {

	private Collection<ConsumerGroupDescriptionFull> groupDescriptions;

	public Collection<ConsumerGroupDescriptionFull> getGroupDescriptions() {
		return groupDescriptions;
	}

	public void setGroupDescriptions(final Collection<ConsumerGroupDescriptionFull> groupDescriptions) {
		this.groupDescriptions = groupDescriptions;
	}

	public static class ConsumerGroupDescriptionFull {

		private String groupId;
		private boolean isSimpleConsumerGroup;
		private Collection<MemberDescription> members;
		private String partitionAssignor;
		private ConsumerGroupState state;

		public String getGroupId() {
			return groupId;
		}

		public void setGroupId(final String groupId) {
			this.groupId = groupId;
		}

		public boolean isSimpleConsumerGroup() {
			return isSimpleConsumerGroup;
		}

		public void setSimpleConsumerGroup(final boolean simpleConsumerGroup) {
			isSimpleConsumerGroup = simpleConsumerGroup;
		}

		public Collection<MemberDescription> getMembers() {
			return members;
		}

		public void setMembers(final Collection<MemberDescription> members) {
			this.members = members;
		}

		public String getPartitionAssignor() {
			return partitionAssignor;
		}

		public void setPartitionAssignor(final String partitionAssignor) {
			this.partitionAssignor = partitionAssignor;
		}

		public ConsumerGroupState getState() {
			return state;
		}

		public void setState(final ConsumerGroupState state) {
			this.state = state;
		}

		public static class MemberDescription {

			private String memberId;
			private String clientId;
			private String host;
			private MemberAssignment assignment;

			public String getMemberId() {
				return memberId;
			}

			public void setMemberId(final String memberId) {
				this.memberId = memberId;
			}

			public String getClientId() {
				return clientId;
			}

			public void setClientId(final String clientId) {
				this.clientId = clientId;
			}

			public String getHost() {
				return host;
			}

			public void setHost(final String host) {
				this.host = host;
			}

			public MemberAssignment getAssignment() {
				return assignment;
			}

			public void setAssignment(final MemberAssignment assignment) {
				this.assignment = assignment;
			}

			public static class MemberAssignment {

				private Collection<TopicPartition> topicPartitions;

				public Collection<TopicPartition> getTopicPartitions() {
					return topicPartitions;
				}

				public void setTopicPartitions(final Collection<TopicPartition> topicPartitions) {
					this.topicPartitions = topicPartitions;
				}

				public static class TopicPartition {

					private int partition;
					private String topic;
					private long topicLogEndOffset;
					private OffsetMetadata offsetMetadata;

					public int getPartition() {
						return partition;
					}

					public void setPartition(final int partition) {
						this.partition = partition;
					}

					public String getTopic() {
						return topic;
					}

					public void setTopic(final String topic) {
						this.topic = topic;
					}

					public OffsetMetadata getOffsetMetadata() {
						return offsetMetadata;
					}

					public void setOffsetMetadata(final OffsetMetadata offsetMetadata) {
						this.offsetMetadata = offsetMetadata;
					}

					public long getTopicLogEndOffset() {
						return topicLogEndOffset;
					}

					public void setTopicLogEndOffset(final long topicLogEndOffset) {
						this.topicLogEndOffset = topicLogEndOffset;
					}

					public static class OffsetMetadata {

						private long offset;
						private String metadata;

						public OffsetMetadata(final long offset, final String metadata) {
							this.offset = offset;
							this.metadata = metadata;
						}

						public long getOffset() {
							return offset;
						}

						public String getMetadata() {
							return metadata;
						}

					}

				}

			}
		}

	}
}
