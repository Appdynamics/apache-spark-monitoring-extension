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
public class StageMetricsTest {
    private String applicationName;
    private List<JsonNode> stagesFromApplication;
    private List<Map> stageMetricsFromCfg;
    private String metricPrefix;

    @Before
    public void setup() throws IOException {
        metricPrefix="Custom Metrics|Spark|";
        applicationName = "App1";
        ObjectMapper mapper = new ObjectMapper();
        stagesFromApplication = Lists.newArrayList();
        stagesFromApplication.add(mapper.readValue(new File("src/test/resources/stages.json"), JsonNode.class));
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config_metrics.yml"));
        Map allMetrics = (Map) config.get("metrics");
        stageMetricsFromCfg = (List) allMetrics.get("stages");

        PowerMockito.mockStatic(MetricPathUtils.class);
    }

    @Test
    public void populateMetricsTest() throws IOException {
        Map<String,String> metricMap = Maps.newHashMap();
        StageMetrics stageMetrics = new StageMetrics(applicationName, stagesFromApplication, stageMetricsFromCfg,metricPrefix);

        when(MetricPathUtils.buildMetricPath(anyString(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn("Custom Metrics|Spark|App1|Stages|0|reduce at SparkPi.scala:38");

        List<Metric> metrics = stageMetrics.populateMetrics();

        for (Metric metric: metrics){
            metricMap.put(metric.getMetricPath(),metric.getMetricValue());
        }

        Assert.assertTrue(metrics.size() == 2);
        Assert.assertTrue(metricMap.containsKey("Custom Metrics|Spark|App1|Stages|0|reduce at SparkPi.scala:38|Number of active tasks in the application's stages"));
        Assert.assertTrue(metricMap.containsKey("Custom Metrics|Spark|App1|Stages|0|reduce at SparkPi.scala:38|Number of complete tasks in the application's stages"));
        Assert.assertTrue(metricMap.get("Custom Metrics|Spark|App1|Stages|0|reduce at SparkPi.scala:38|Number of active tasks in the application's stages").equals("4"));
        Assert.assertTrue(metricMap.get("Custom Metrics|Spark|App1|Stages|0|reduce at SparkPi.scala:38|Number of complete tasks in the application's stages").equals("3"));

    }
}
