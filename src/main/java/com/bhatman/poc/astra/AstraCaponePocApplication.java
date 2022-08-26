package com.bhatman.poc.astra;

import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.METRICS_NODE_ENABLED;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.METRICS_SESSION_ENABLED;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.REQUEST_DEFAULT_IDEMPOTENCE;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.SPECULATIVE_EXECUTION_DELAY;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.SPECULATIVE_EXECUTION_MAX;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.SPECULATIVE_EXECUTION_POLICY_CLASS;

import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.autoconfigure.cassandra.DriverConfigLoaderBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.internal.core.specex.ConstantSpeculativeExecutionPolicy;

@SpringBootApplication
@EnableConfigurationProperties({ AstraConfig.class, MetricsConfig.class, SpeculativeConfig.class })
public class AstraCaponePocApplication {

	public static void main(String[] args) {
		SpringApplication.run(AstraCaponePocApplication.class, args);
	}

	/**
	 * Used to connect to Astra via secure-connect-bundle
	 */
	@Bean
	@Profile("!local")
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(AstraConfig astraConfig) {
		Path bundle = astraConfig.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}

	@Bean
	DriverConfigLoaderBuilderCustomizer configLoaderBuilderCustomizer(MetricsConfig metricsConfig,
			SpeculativeConfig speculativeConfig) {
		return builder -> {
			builder.withBoolean(REQUEST_DEFAULT_IDEMPOTENCE, true);
			builder.withBoolean(SPECULATIVE_EXECUTION_POLICY_CLASS, true);
			builder.withClass(SPECULATIVE_EXECUTION_POLICY_CLASS, ConstantSpeculativeExecutionPolicy.class);
			builder.withInt(SPECULATIVE_EXECUTION_MAX, speculativeConfig.getRetryTimes());
			builder.withInt(SPECULATIVE_EXECUTION_DELAY, speculativeConfig.getDelayMillis());

			builder.withStringList(METRICS_SESSION_ENABLED, metricsConfig.getSessionMetrics());
			builder.withStringList(METRICS_NODE_ENABLED, metricsConfig.getNodeMetrics());
		};
	}

	@Bean
	public MetricRegistry getMetricsbean(CqlSession cqlSession) {
		return cqlSession.getMetrics().orElseThrow(() -> new IllegalStateException("Metrics are disabled"))
				.getRegistry();
	}

	@Bean
	public JmxReporter getJmxReporter(MetricRegistry registry) {
		JmxReporter reporter = JmxReporter.forRegistry(registry).inDomain("com.bhatman.poc.astra.metrics").build();
		reporter.start();
		return reporter;
	}

}
