package com.bhatman.poc.astra.metrics;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;

import lombok.AllArgsConstructor;
import lombok.Data;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

	@Autowired
	MetricRegistry registry;

	@GetMapping("/speculative-executions")
	public ResponseEntity<List<SMetric>> get() {
		Map<String, Metric> metricsMap = registry.getMetrics();
		List<SMetric> vals = metricsMap.entrySet().stream()
				.filter(entry -> entry.getKey().contains("speculative-executions"))
				.map(val -> new SMetric(val.getKey(), ((Counter) val.getValue()).getCount())).toList();

		return new ResponseEntity<>(vals, HttpStatus.OK);
	}

	@AllArgsConstructor
	@Data
	class SMetric {
		String metricName;
		Long metricValue;
	}

}
