package com.jcat.kafka.monitor.domain.model.response;

import org.apache.kafka.common.ConsumerGroupState;

import java.util.Collection;

public class DescribeOperationResponse implements OperationResponse {

	private Collection<TopicPartitionInfo> topicPartitions;

	public Collection<TopicPartitionInfo> getTopicPartitions() {
		return topicPartitions;
	}

	public void setTopicPartitions(final Collection<TopicPartitionInfo> topicPartitions) {
		this.topicPartitions = topicPartitions;
	}

	public static class TopicPartitionInfo {

		private String group;
		private String topic;
		private int partition;
		private Offset offset;
		private long lag;
		private MemberInfo memberInfo;

		public String getGroup() {
			return group;
		}

		public void setGroup(final String group) {
			this.group = group;
		}

		public String getTopic() {
			return topic;
		}

		public void setTopic(final String topic) {
			this.topic = topic;
		}

		public int getPartition() {
			return partition;
		}

		public void setPartition(final int partition) {
			this.partition = partition;
		}

		public Offset getOffset() {
			return offset;
		}

		public void setOffset(final Offset offset) {
			this.offset = offset;
		}

		public long getLag() {
			return lag;
		}

		public void setLag(final long lag) {
			this.lag = lag;
		}

		public MemberInfo getMemberInfo() {
			return memberInfo;
		}

		public void setMemberInfo(final MemberInfo memberInfo) {
			this.memberInfo = memberInfo;
		}

		public static class Offset {

			private long group;
			private long topic;

			public long getGroup() {
				return group;
			}

			public void setGroup(final long group) {
				this.group = group;
			}

			public long getTopic() {
				return topic;
			}

			public void setTopic(final long topic) {
				this.topic = topic;
			}
		}
		public static class MemberInfo {

			private String id;
			private String clientId;
			private String host;

			public String getId() {
				return id;
			}

			public void setId(final String id) {
				this.id = id;
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
		}
	}
}
