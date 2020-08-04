package com.jcat.kafka.monitor.configuration;

import com.jcat.kafka.monitor.domain.model.Out;

public class ApplicationConfiguration {

	public static final DefaultValues DEFAULT_VALUES;

	static {

		DefaultValues defaultValues = new DefaultValues();
		defaultValues.setInterval(2_000);
		defaultValues.setOut(Out.console);
		DEFAULT_VALUES = defaultValues;
	}


	public static class DefaultValues {

		private Out out;
		private Integer interval; //in ms
		private final IntervalConfiguration intervalConfiguration = new IntervalConfiguration(1000, 10_000);

		public Out getOut() {
			return out;
		}

		public void setOut(final Out out) {
			this.out = out;
		}

		public Integer getInterval() {
			return interval;
		}

		public void setInterval(final Integer interval) {
			this.interval = interval;
		}

		public IntervalConfiguration getIntervalConfiguration() {
			return intervalConfiguration;
		}

		public static class IntervalConfiguration {

			private final int min;
			private final int max;

			public IntervalConfiguration(final int min, final int max) {
				this.min = min;
				this.max = max;
			}

			public int getMin() {
				return min;
			}

			public int getMax() {
				return max;
			}
		}
	}


}
