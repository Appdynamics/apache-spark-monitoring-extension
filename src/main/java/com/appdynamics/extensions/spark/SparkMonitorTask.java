package com.appdynamics.extensions.spark;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.spark.metrics.MetricProperties;
import com.appdynamics.extensions.spark.metrics.MetricPropertiesBuilder;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
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
            printMetrics(sparkMetrics);
            logger.info("Successfully completed the Spark Monitoring Task for " + server.get("name").toString());
        } catch (Exception ex) {
            logger.error("Spark Monitoring Task Failed", ex.getMessage());
        }

    }

    private void printMetrics(Map<String, BigDecimal> sparkMetrics) {
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        String metricPrefix = configuration.getMetricPrefix();
        String aggregationType = DEFAULT_AGGREGATION_TYPE;
        String clusterRollupType = DEFAULT_CLUSTER_ROLLUP_TYPE;
        String timeRollupType = DEFAULT_TIME_ROLLUP_TYPE;
        Map<String, MetricProperties> metricOverrides = MetricPropertiesBuilder.getMetricPropsMap();

        for(Map.Entry<String, BigDecimal> metric : sparkMetrics.entrySet()) {
            String metricPath = metricPrefix + METRIC_SEPARATOR + metric.getKey();
            String metricName = metric.getKey();
            BigDecimal metricValue = metric.getValue();
            if(metricOverrides.containsKey(metricName)) {
                MetricProperties propertiesForCurrentMetric = metricOverrides.get(metricName);
                metricPath = metricPrefix + METRIC_SEPARATOR + propertiesForCurrentMetric.getMetricPath() + propertiesForCurrentMetric.getAlias();
                metricValue = metric.getValue().multiply(new BigDecimal(propertiesForCurrentMetric.getMultiplier()));
                aggregationType = propertiesForCurrentMetric.getAggregationType();
                clusterRollupType = propertiesForCurrentMetric.getClusterRollupType();
                timeRollupType = propertiesForCurrentMetric.getTimeRollupType();
            }
            metricWriter.printMetric(metricPath, String.valueOf(metricValue), aggregationType, timeRollupType, clusterRollupType);
        }
    }
}
