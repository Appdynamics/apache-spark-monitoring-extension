package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.spark.helpers.Constants.*;

/**
 * Created by aditya.jagtiani on 5/16/17.
 */
public class MetricPropertiesBuilderTest {

    @Test
    public void buildMetricPropsMapTest_ValidQualifiers() {
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config_valid_qualifiers.yml"));
        Map allMetrics = (Map) config.get("metrics");
        List<Map> jobMetricsFromCfg = (List) allMetrics.get("jobs");
        MetricPropertiesBuilder.buildMetricPropsMap(jobMetricsFromCfg.get(0), "numTasks", "|metricPath|");
        Map<String, MetricProperties> overrides = MetricPropertiesBuilder.getMetricPropsMap();
        Assert.assertTrue(overrides.containsKey("|metricPath|numTasks"));
        MetricProperties properties = overrides.get("|metricPath|numTasks");
        Assert.assertTrue(properties.getMetricName().equals("numTasks"));
        Assert.assertTrue(properties.getAlias().equals("Number of tasks in the application"));
        Assert.assertTrue(properties.getAggregationType().equals("AVERAGE"));
        Assert.assertTrue(properties.getClusterRollupType().equals("INDIVIDUAL"));
        Assert.assertTrue(properties.getTimeRollupType().equals("SUM"));
        Assert.assertTrue(properties.getMultiplier().equals("2.5"));
        Assert.assertTrue(properties.getDelta());
    }

    @Test
    public void buildMetricPropsMapTest_InvalidQualifiers() {
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config_invalid_qualifiers.yml"));
        Map allMetrics = (Map) config.get("metrics");
        List<Map> jobMetricsFromCfg = (List) allMetrics.get("jobs");
        MetricPropertiesBuilder.buildMetricPropsMap(jobMetricsFromCfg.get(0), "numTasks", "|metricPath|");
        Map<String, MetricProperties> overrides = MetricPropertiesBuilder.getMetricPropsMap();
        Assert.assertTrue(overrides.containsKey("|metricPath|numTasks"));
        MetricProperties properties = overrides.get("|metricPath|numTasks");
        Assert.assertTrue(properties.getMetricName().equals("numTasks"));
        Assert.assertTrue(properties.getAlias().equals("numTasks"));
        Assert.assertTrue(properties.getAggregationType().equals(DEFAULT_AGGREGATION_TYPE));
        Assert.assertTrue(properties.getClusterRollupType().equals(DEFAULT_CLUSTER_ROLLUP_TYPE));
        Assert.assertTrue(properties.getTimeRollupType().equals(DEFAULT_TIME_ROLLUP_TYPE));
        Assert.assertTrue(properties.getMultiplier().equals(DEFAULT_MULTIPLIER));
        Assert.assertFalse(properties.getDelta());
    }
}
