package com.jcat.kafka.monitor.domain.service.validate;

import com.jcat.kafka.monitor.configuration.ApplicationConfiguration;
import com.jcat.kafka.monitor.domain.model.Out;
import com.jcat.kafka.monitor.domain.model.cli.CommandLineRequest;

public class DescribeOperationValidator implements RequestValidator {

	@Override
	public void validate(final CommandLineRequest commandLineRequest) throws Exception {

		if (commandLineRequest.getBootstrapServer() == null) {
			throw new IllegalArgumentException("bootstrap-server option is required for describe operation");
		}

		if (commandLineRequest.getGroups() == null) {
			throw new IllegalArgumentException("groups option is required for describe operation");
		}

		Integer interval = commandLineRequest.getInterval();
		ApplicationConfiguration.DefaultValues.IntervalConfiguration intervalConfiguration = ApplicationConfiguration.DEFAULT_VALUES.getIntervalConfiguration();
		if (interval == null || (interval < intervalConfiguration.getMin() || interval > intervalConfiguration.getMax())) {
			commandLineRequest.setInterval(ApplicationConfiguration.DEFAULT_VALUES.getInterval());
		}

		Out out = commandLineRequest.getOut();
		if (out == null) {
			commandLineRequest.setOut(ApplicationConfiguration.DEFAULT_VALUES.getOut());
		} else {
			if (Out.prometheus.equals(out)) {
				//check that all required props is exists
				if (commandLineRequest.getPrometheusConfiguration().getJob() == null) {
					throw new IllegalArgumentException("prometheus-job option is required for prometheus output");
				}
			}
		}
	}
}
