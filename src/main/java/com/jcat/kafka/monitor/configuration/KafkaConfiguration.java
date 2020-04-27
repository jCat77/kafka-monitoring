package com.jcat.kafka.monitor.configuration;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfiguration {

	@Bean("admin-client-properties")
	public Properties adminClientProperties() {
		Properties properties = new Properties();
		return properties;
	}

	@Bean
	public AdminClient adminClient(@Qualifier("admin-client-properties") Properties properties) {
		return AdminClient.create(properties);
	}
}
