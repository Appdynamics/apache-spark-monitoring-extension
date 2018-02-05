/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark.metrics;

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

public class JobMetricsTest {
    private String applicationName;
    private List<JsonNode> jobsFromApplication;
    private List<Map> jobMetricsFromCfg;

    @Before
    public void setup() throws IOException {
        applicationName = "App1";
        ObjectMapper mapper = new ObjectMapper();
        jobsFromApplication = Lists.newArrayList();
        jobsFromApplication.add(mapper.readValue(new File("src/test/resources/jobs.json"), JsonNode.class));
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
        Map allMetrics = (Map) config.get("metrics");
        jobMetricsFromCfg = (List) allMetrics.get("jobs");
    }

    @Test
    public void populateMetricsTest() throws IOException {
        JobMetrics jobMetrics = new JobMetrics(applicationName, jobsFromApplication, jobMetricsFromCfg);
        Map<String, BigDecimal> metrics = jobMetrics.populateMetrics();
        Assert.assertTrue(metrics.size() == 4);
        Assert.assertTrue(metrics.containsKey("Applications|App1|Jobs|0|reduce at SparkPi.scala:38|numSkippedTasks"));
        Assert.assertTrue(metrics.containsKey("Applications|App1|Jobs|0|reduce at SparkPi.scala:38|numCompletedTasks"));
        Assert.assertTrue(metrics.containsKey("Applications|App1|Jobs|0|reduce at SparkPi.scala:38|numTasks"));
        Assert.assertTrue(metrics.containsKey("Applications|App1|Jobs|0|reduce at SparkPi.scala:38|numActiveTasks"));
        Assert.assertTrue(metrics.get("Applications|App1|Jobs|0|reduce at SparkPi.scala:38|numSkippedTasks").equals(BigDecimal.ZERO));
        Assert.assertTrue(metrics.get("Applications|App1|Jobs|0|reduce at SparkPi.scala:38|numCompletedTasks").equals(BigDecimal.ZERO));
        Assert.assertTrue(metrics.get("Applications|App1|Jobs|0|reduce at SparkPi.scala:38|numTasks").equals(BigDecimal.TEN));
        Assert.assertTrue(metrics.get("Applications|App1|Jobs|0|reduce at SparkPi.scala:38|numActiveTasks").equals(BigDecimal.ZERO));
    }
}
