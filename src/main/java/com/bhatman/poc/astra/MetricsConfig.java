package com.bhatman.poc.astra;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "cassandra.metrics")
public class MetricsConfig {
	private List<String> sessionMetrics;
	private List<String> nodeMetrics;

}
