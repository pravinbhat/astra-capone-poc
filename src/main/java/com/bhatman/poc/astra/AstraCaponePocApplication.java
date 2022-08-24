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
import com.datastax.oss.driver.api.core.CqlSession;

@SpringBootApplication
@EnableConfigurationProperties({ AstraConfig.class, AstraConfigLocal.class })
public class AstraCaponePocApplication {

	public static void main(String[] args) {
		SpringApplication.run(AstraCaponePocApplication.class, args);
	}

	/**
	 * Used to connect to Astra via secure-connect-bundle
	 */
	@Bean
	@Profile("!local")
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(AstraConfig astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}
	
	@Bean
	@Profile("local")
    DriverConfigLoaderBuilderCustomizer configLoaderBuilderCustomizer(AstraConfigLocal cassandraProperties) {
        return builder -> {
        	builder.withString(REQUEST_DEFAULT_IDEMPOTENCE, "true");
        	builder.withString(SPECULATIVE_EXECUTION_POLICY_CLASS, "ConstantSpeculativeExecutionPolicy");
        	builder.withString(SPECULATIVE_EXECUTION_MAX, "3");
        	builder.withString(SPECULATIVE_EXECUTION_DELAY, "2 milliseconds");
            
        	builder.withStringList(METRICS_SESSION_ENABLED, cassandraProperties.getSessionMetrics());
            builder.withStringList(METRICS_NODE_ENABLED, cassandraProperties.getNodeMetrics());
        };
    }

	
	@Bean
	@Profile("local")
	public MetricRegistry getMetricsbean(CqlSession cqlSession) {
		return cqlSession.getMetrics()
			    .orElseThrow(() -> new IllegalStateException("Metrics are disabled"))
			    .getRegistry();
	}


}
