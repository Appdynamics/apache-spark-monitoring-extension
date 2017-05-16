package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.spark.metrics.ExecutorMetrics;
import com.appdynamics.extensions.yml.YmlReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by aditya.jagtiani on 5/16/17.
 */

public class ExecutorMetricsTest {
    private String applicationName;
    private List<JsonNode> executorsFromApplication;
    private List<Map> executorMetricsFromCfg;

    @Before
    public void setup() throws IOException {
        applicationName = "App1";
        ObjectMapper mapper = new ObjectMapper();
        executorsFromApplication = Lists.newArrayList();
        executorsFromApplication.add(mapper.readValue(new File("src/test/resources/executors.json"), JsonNode.class));
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
        Map allMetrics = (Map) config.get("metrics");
        executorMetricsFromCfg = (List) allMetrics.get("executors");
    }

    @Test
    public void populateMetricsTest() throws IOException {
        ExecutorMetrics executorMetrics = new ExecutorMetrics(applicationName, executorsFromApplication, executorMetricsFromCfg);
        Map<String, BigDecimal> metrics = executorMetrics.populateMetrics();
        Assert.assertTrue(metrics.size() == 2);
        Assert.assertTrue(metrics.containsKey("Applications|App1|Executors|driver|memoryUsed"));
        Assert.assertTrue(metrics.containsKey("Applications|App1|Executors|driver|rddBlocks"));
        Assert.assertTrue(metrics.get("Applications|App1|Executors|driver|memoryUsed").equals(new BigDecimal(10000)));
        Assert.assertTrue(metrics.get("Applications|App1|Executors|driver|rddBlocks").equals(new BigDecimal(19)));
    }
}
