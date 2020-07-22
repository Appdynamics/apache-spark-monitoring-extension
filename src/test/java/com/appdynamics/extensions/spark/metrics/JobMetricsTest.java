/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.MetricPathUtils;
import com.appdynamics.extensions.yml.YmlReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Created by aditya.jagtiani on 5/16/17, abhishek.saxena on 7/8/20.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(MetricPathUtils.class)
public class JobMetricsTest {
    private String applicationName;
    private List<JsonNode> jobsFromApplication;
    private List<Map> jobMetricsFromCfg;
    private String metricPrefix;

    @Before
    public void setup() throws IOException {
        metricPrefix="Custom Metrics|Spark|";
        applicationName = "App1";
        ObjectMapper mapper = new ObjectMapper();
        jobsFromApplication = Lists.newArrayList();
        jobsFromApplication.add(mapper.readValue(new File("src/test/resources/jobs.json"), JsonNode.class));
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config_metrics.yml"));
        Map allMetrics = (Map) config.get("metrics");
        jobMetricsFromCfg = (List) allMetrics.get("jobs");

        PowerMockito.mockStatic(MetricPathUtils.class);
    }

    @Test
    public void populateMetricsTest() {
        Map<String,String> metricMap = Maps.newHashMap();
        JobMetrics jobMetrics = new JobMetrics(applicationName, jobsFromApplication, jobMetricsFromCfg,metricPrefix);

        when(MetricPathUtils.buildMetricPath(anyString(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn("Custom Metrics|Spark|App1|Jobs|0|reduce at SparkPi.scala:38");

        List<Metric> metrics = jobMetrics.populateMetrics();

        for (Metric metric: metrics){
            metricMap.put(metric.getMetricPath(),metric.getMetricValue());
        }

        Assert.assertTrue(metrics.size()==4);
        Assert.assertTrue(metricMap.containsKey("Custom Metrics|Spark|App1|Jobs|0|reduce at SparkPi.scala:38|Number of skipped tasks"));
        Assert.assertTrue(metricMap.containsKey("Custom Metrics|Spark|App1|Jobs|0|reduce at SparkPi.scala:38|Number of completed tasks in the application"));
        Assert.assertTrue(metricMap.containsKey("Custom Metrics|Spark|App1|Jobs|0|reduce at SparkPi.scala:38|Number of tasks in the application"));
        Assert.assertTrue(metricMap.containsKey("Custom Metrics|Spark|App1|Jobs|0|reduce at SparkPi.scala:38|Number of active tasks in the application"));
        Assert.assertTrue(metricMap.get("Custom Metrics|Spark|App1|Jobs|0|reduce at SparkPi.scala:38|Number of skipped tasks").equals("0"));
        Assert.assertTrue(metricMap.get("Custom Metrics|Spark|App1|Jobs|0|reduce at SparkPi.scala:38|Number of completed tasks in the application").equals("0"));
        Assert.assertTrue(metricMap.get("Custom Metrics|Spark|App1|Jobs|0|reduce at SparkPi.scala:38|Number of tasks in the application").equals("10"));
        Assert.assertTrue(metricMap.get("Custom Metrics|Spark|App1|Jobs|0|reduce at SparkPi.scala:38|Number of active tasks in the application").equals("0"));
    }
}
