/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.spark.helpers.SparkUtils;
import com.appdynamics.extensions.util.MetricPathUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.spark.helpers.Constants.*;

/**
 * Created by aditya.jagtiani on 5/9/17, abhishek.saxena on 7/8/20.
 */

class JobMetrics {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(JobMetrics.class);
    private String applicationName;
    private List<JsonNode> jobsFromApplication;
    private List<Map> jobMetricsFromConfig;
    private String metricPrefix;
    private List<MetricProperties> sparkJobMetric;
    private ObjectMapper objectMapper = new ObjectMapper();

    JobMetrics(String applicationName, List<JsonNode> jobsFromApplication, List<Map> jobMetricsFromConfig, String metricPrefix) {
        this.applicationName = applicationName;
        this.jobsFromApplication = jobsFromApplication;
        this.jobMetricsFromConfig = jobMetricsFromConfig;
        this.metricPrefix = metricPrefix;
    }

    List<Metric> populateMetrics() {
        List<Metric> jobMetrics = Lists.newArrayList();
        if (SparkUtils.validateEntityInApplication(applicationName, jobsFromApplication, JOBS)) {
            try {
                String metricName;
                sparkJobMetric = Arrays.asList(objectMapper.convertValue(jobMetricsFromConfig, MetricProperties[].class));
                for (JsonNode job : jobsFromApplication) {
                    String jobName = job.findValue("name").asText();
                    String jobId = job.findValue("jobId").asText();
                    String currentJobMetricPath = MetricPathUtils.buildMetricPath(metricPrefix, APPLICATIONS, applicationName, JOBS, jobId, jobName);
                    logger.info("Fetching metrics for job " + jobId + ": " + jobName + " in application: " + applicationName);
                    for (MetricProperties metrics : sparkJobMetric) {
                        metricName = metrics.getName();
                        if (job.findValue(metricName) != null) {
                            jobMetrics.add(new Metric(metricName, String.valueOf(job.findValue(metricName)), currentJobMetricPath + METRIC_SEPARATOR + metrics.getAlias(), objectMapper.convertValue(metrics, Map.class)));
                        } else {
                            logger.debug("Metric :" + metricName + " not found for job : " + jobName + ". Please verify whether correct metric names have been entered in the config.yml");
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Error occurred while fetching metrics for job", ex);
            }
        }
        return jobMetrics;
    }
}
