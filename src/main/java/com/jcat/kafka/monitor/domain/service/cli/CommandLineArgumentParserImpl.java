package com.jcat.kafka.monitor.domain.service.cli;

import com.jcat.kafka.monitor.domain.model.Operation;
import com.jcat.kafka.monitor.domain.model.Out;
import com.jcat.kafka.monitor.domain.model.cli.CommandLineRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class CommandLineArgumentParserImpl implements CommandLineArgumentParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineArgumentParser.class);

	private static final String PARAM_MARKER = "-";
	private static final String BOOTSTRAP_SERVER_PARAM = "bootstrap-server";
	private static final String GROUPS_PARAM = "groups";
	private static final String INTERVAL_PARAM = "interval";
	private static final String OUT_PARAM = "out";
	private static final String PROMETHEUS_URL_PARAM = "prometheus-url";
	private static final String PROMETHEUS_JOB_PARAM = "prometheus-job";

	@Override
	public CommandLineRequest parse(final String[] args) {
		CommandLineRequest commandLineRequest = new CommandLineRequest();
		if (args == null) {
			throw new IllegalArgumentException("No args passes");
		}
		for (int i = 0; i < args.length; i += 2) {
			if (i == args.length - 1) {
				commandLineRequest.setOperation(Operation.valueOf(args[i]));
			} else {
				if (i + 1 < args.length) {
					handleArgument(commandLineRequest, args[i], args[i + 1]);
				} else {
					throw new RuntimeException("No value passed for arg=\"" + args[i] + "\"");
				}
			}
		}
		return commandLineRequest;
	}

	private void handleArgument(CommandLineRequest commandLineRequest, String arg, String value) {
		String argName = getArgName(arg);
		switch (argName) {
			case BOOTSTRAP_SERVER_PARAM:
				commandLineRequest.setBootstrapServer(value);
				break;
			case GROUPS_PARAM:
				commandLineRequest.setGroups(value);
				break;
			case INTERVAL_PARAM:
				Integer interval = null;
				try {
					interval = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					//log or forget
				}
				commandLineRequest.setInterval(interval);
				break;
			case OUT_PARAM:
				Out out = null;
				try {
					out = Out.valueOf(value);
				} catch (IllegalArgumentException e) {
					String enumString = Arrays.stream(Out.values()).collect(StringBuilder::new, (b, s) -> {
						b.append(s).append(",");
					}, StringBuilder::append).toString();
					LOGGER.error("Illegal \"out\" argument value...can be one of \"" + enumString + "\"");
				}
				commandLineRequest.setOut(out);
				break;
			case PROMETHEUS_URL_PARAM:
				commandLineRequest.getPrometheusConfiguration().setUrl(value);
				break;
			case PROMETHEUS_JOB_PARAM:
				commandLineRequest.getPrometheusConfiguration().setJob(value);
				break;
			default:
				LOGGER.error("Argument " + argName + " is not resolved and will be ignored");
		}
	}

	private String getArgName(String arg) {
		checkIsArgName(arg);
		return arg.substring(1);
	}

	private void checkIsArgName(String arg) {
		if (arg == null) {
			throw new IllegalArgumentException("Argument is null");
		}
		if (!arg.startsWith(PARAM_MARKER)) {
			throw new IllegalArgumentException("Argument must begin with \"" + PARAM_MARKER + "\"");
		}
	}
}
