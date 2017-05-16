package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.spark.helpers.SparkUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.spark.helpers.Constants.METRIC_SEPARATOR;

/**
 * Created by aditya.jagtiani on 5/9/17.
 */

class JobMetrics {

    private static final Logger logger = LoggerFactory.getLogger(JobMetrics.class);
    private static final String ENTITY_TYPE = "JOBS";
    private String applicationName;
    private List<JsonNode> jobsFromApplication;
    private List<Map> jobMetricsFromConfig;

    JobMetrics(String applicationName, List<JsonNode> jobsFromApplication, List<Map> jobMetricsFromConfig) {
        this.applicationName = applicationName;
        this.jobsFromApplication = jobsFromApplication;
        this.jobMetricsFromConfig = jobMetricsFromConfig;
    }

    Map<String, BigDecimal> populateMetrics() throws IOException {
        if (!SparkUtils.isValidationSuccessful(jobMetricsFromConfig, jobsFromApplication, ENTITY_TYPE)) {
            return Maps.newHashMap();
        }
        Map<String, BigDecimal> jobMetrics = Maps.newHashMap();
        for (JsonNode job : jobsFromApplication) {
            String jobName = job.findValue("name").asText();
            String jobId = job.findValue("jobId").asText();
            String currentJobMetricPath = "Applications" + METRIC_SEPARATOR + applicationName + METRIC_SEPARATOR + "Jobs" + METRIC_SEPARATOR + jobId + METRIC_SEPARATOR + jobName + METRIC_SEPARATOR;
            logger.info("Fetching metrics for job " + jobId + ": " + jobName + " in application: " + applicationName);
            for (Map metric : jobMetricsFromConfig) {
                Map.Entry<String, String> entry = (Map.Entry) metric.entrySet().iterator().next();
                String metricName = entry.getKey();
                if (job.findValue(metricName) != null) {
                    jobMetrics.put(currentJobMetricPath + metricName, SparkUtils.convertDoubleToBigDecimal(job.findValue(metricName).asDouble()));
                    if (entry.getValue() != null) {
                        MetricPropertiesBuilder.buildMetricPropsMap(metric, metricName, currentJobMetricPath);
                    }
                } else {
                    logger.debug("Metric :" + metricName + " not found for job : " + jobName + ". Please verify whether correct metric names have been entered in the config.yml");
                }
            }
        }
        return jobMetrics;
    }
}
