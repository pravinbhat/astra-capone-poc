package com.bhatman.poc.astra;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "speculative-executions")
public class SpeculativeConfig {
	private int retryTimes;
	private int delayMillis;
}
