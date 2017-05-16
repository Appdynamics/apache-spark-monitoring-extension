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

class StageMetrics {
    private static final Logger logger = LoggerFactory.getLogger(StageMetrics.class);
    private static final String ENTITY_TYPE = "STAGES";
    private String applicationName;
    private List<JsonNode> stagesFromApplication;
    private List<Map> stageMetricsFromConfig;

    StageMetrics(String applicationName, List<JsonNode> stagesFromApplication, List<Map> stageMetricsFromConfig) {
        this.applicationName = applicationName;
        this.stagesFromApplication = stagesFromApplication;
        this.stageMetricsFromConfig = stageMetricsFromConfig;
    }

    Map<String, BigDecimal> populateMetrics() throws IOException {
        if (!SparkUtils.isValidationSuccessful(stageMetricsFromConfig, stagesFromApplication, ENTITY_TYPE)) {
            return Maps.newHashMap();
        }
        Map<String, BigDecimal> stageMetrics = Maps.newHashMap();
        for (JsonNode stage : stagesFromApplication) {
            String stageId = stage.findValue("stageId").asText();
            String stageName = stage.findValue("name").asText();
            String currentStageMetricPath = "Applications" + METRIC_SEPARATOR + applicationName + METRIC_SEPARATOR + "Stages" + METRIC_SEPARATOR + stageId + METRIC_SEPARATOR + stageName + METRIC_SEPARATOR;
            logger.info("Fetching metrics for executor :" + stageId + " in application: " + applicationName);
            for (Map metric : stageMetricsFromConfig) {
                Map.Entry<String, String> entry = (Map.Entry) metric.entrySet().iterator().next();
                String metricName = entry.getKey();
                if (stage.findValue(metricName) != null) {
                    stageMetrics.put(currentStageMetricPath + metricName, SparkUtils.convertDoubleToBigDecimal(stage.findValue(metricName).asDouble()));
                    if (entry.getValue() != null) {
                        MetricPropertiesBuilder.buildMetricPropsMap(metric, metricName, currentStageMetricPath);
                    }
                } else {
                    logger.debug("Metric :" + metricName + " not found for stage : " + stageId + ". Please verify whether correct metric names have been entered in the config.yml");
                }
            }
        }
        return stageMetrics;
    }
}

