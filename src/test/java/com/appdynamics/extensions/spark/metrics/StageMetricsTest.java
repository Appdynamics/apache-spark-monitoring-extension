/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.spark.metrics.StageMetrics;
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

public class StageMetricsTest {
    private String applicationName;
    private List<JsonNode> stagesFromApplication;
    private List<Map> stageMetricsFromCfg;

    @Before
    public void setup() throws IOException {
        applicationName = "App1";
        ObjectMapper mapper = new ObjectMapper();
        stagesFromApplication = Lists.newArrayList();
        stagesFromApplication.add(mapper.readValue(new File("src/test/resources/stages.json"), JsonNode.class));
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
        Map allMetrics = (Map) config.get("metrics");
        stageMetricsFromCfg = (List) allMetrics.get("stages");
    }

    @Test
    public void populateMetricsTest() throws IOException {
        StageMetrics stageMetrics = new StageMetrics(applicationName, stagesFromApplication, stageMetricsFromCfg);
        Map<String, BigDecimal> metrics = stageMetrics.populateMetrics();
        Assert.assertTrue(metrics.size() == 2);
        Assert.assertTrue(metrics.containsKey("Applications|App1|Stages|0|reduce at SparkPi.scala:38|numActiveTasks"));
        Assert.assertTrue(metrics.containsKey("Applications|App1|Stages|0|reduce at SparkPi.scala:38|numCompleteTasks"));
        Assert.assertTrue(metrics.get("Applications|App1|Stages|0|reduce at SparkPi.scala:38|numActiveTasks").equals(new BigDecimal(4)));
        Assert.assertTrue(metrics.get("Applications|App1|Stages|0|reduce at SparkPi.scala:38|numCompleteTasks").equals(new BigDecimal(3)));

    }
}
