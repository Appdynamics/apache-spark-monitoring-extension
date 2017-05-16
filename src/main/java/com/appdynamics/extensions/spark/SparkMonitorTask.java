package com.appdynamics.extensions.spark;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.spark.metrics.MetricProperties;
import com.appdynamics.extensions.spark.metrics.MetricPropertiesBuilder;
import com.appdynamics.extensions.util.MetricWriteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

import static com.appdynamics.extensions.spark.helpers.Constants.*;

public class SparkMonitorTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SparkMonitorTask.class);
    private MonitorConfiguration configuration;
    private Map server;

    SparkMonitorTask(MonitorConfiguration configuration, Map server) {
        this.configuration = configuration;
        this.server = server;
    }

    public void run() {
        try {
            populateAndPrintStats();
            logger.info("Spark Metric Upload Complete");
        } catch (Exception ex) {
            configuration.getMetricWriter().registerError(ex.getMessage(), ex);
            logger.error("Error while running the task", ex);
        }
    }

    private void populateAndPrintStats() {
        try {
            SparkStats sparkStats = new SparkStats(configuration, server);
            Map<String, BigDecimal> sparkMetrics = sparkStats.populateMetrics();
            Map<String, MetricProperties> metricOverrides = MetricPropertiesBuilder.getMetricPropsMap();
            for(Map.Entry<String, BigDecimal> metric : sparkMetrics.entrySet()){
                String metricName = metric.getKey();
                if(metricOverrides.containsKey(metricName)) {
                    applyOverridesAndPrint(metricName, metricOverrides, metric);
                }
                else {
                    printMetricWithoutOverrides(metric);
                }
            }
            logger.info("Successfully completed the Spark Monitoring Task for " + server.get("name").toString());
        } catch (Exception ex) {
            logger.error("Spark Monitoring Task Failed", ex.getMessage());
        }

    }

    private void printMetricWithoutOverrides(Map.Entry<String, BigDecimal> metric) {
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        String metricPrefix = configuration.getMetricPrefix();
        String metricPath = metricPrefix + "|" + metric.getKey();
        BigDecimal metricValue = metric.getValue();
        metricWriter.printMetric(metricPath, String.valueOf(metricValue), DEFAULT_AGGREGATION_TYPE, DEFAULT_TIME_ROLLUP_TYPE, DEFAULT_CLUSTER_ROLLUP_TYPE);

    }

    // do the checks in metricpropsbuilder, not here.
    private void applyOverridesAndPrint(String metricName, Map<String, MetricProperties> metricOverrides, Map.Entry<String, BigDecimal> metric) {
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        String metricPrefix = configuration.getMetricPrefix();
        MetricProperties propertiesForCurrentMetric = metricOverrides.get(metricName);
        String aggregationType = propertiesForCurrentMetric.getAggregationType() == null || propertiesForCurrentMetric.getAggregationType().isEmpty() ?
                DEFAULT_AGGREGATION_TYPE : propertiesForCurrentMetric.getAggregationType();
        String clusterRollupType = propertiesForCurrentMetric.getClusterRollupType() == null || propertiesForCurrentMetric.getClusterRollupType().isEmpty() ?
                DEFAULT_CLUSTER_ROLLUP_TYPE : propertiesForCurrentMetric.getClusterRollupType();
        String timeRollupType = propertiesForCurrentMetric.getTimeRollupType() == null || propertiesForCurrentMetric.getTimeRollupType().isEmpty() ?
                DEFAULT_TIME_ROLLUP_TYPE : propertiesForCurrentMetric.getTimeRollupType();
        String multiplier = propertiesForCurrentMetric.getMultiplier() == null || propertiesForCurrentMetric.getMultiplier().isEmpty() ?
                DEFAULT_MULTIPLIER : propertiesForCurrentMetric.getMultiplier();
        String alias = propertiesForCurrentMetric.getAlias() == null || propertiesForCurrentMetric.getAlias().isEmpty() ?
                metricName : propertiesForCurrentMetric.getAlias();
        String metricPath = metricPrefix + "|" + propertiesForCurrentMetric.getMetricPath() + alias;
        BigDecimal metricValue = metric.getValue().multiply(new BigDecimal(multiplier));
        metricWriter.printMetric(metricPath, String.valueOf(metricValue), aggregationType, timeRollupType, clusterRollupType);
    }
}
