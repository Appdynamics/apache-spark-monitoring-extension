/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark.helpers;

import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Created by aditya.jagtiani, abhishek.saxena on 08/July/2020.
 */

public class SparkUtils {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(SparkUtils.class);

    public static boolean validateMetricsInConfig(List configuredMetrics, String entityType) {
        if (configuredMetrics == null || configuredMetrics.isEmpty()) {
            logger.debug("No metrics have been configured for : " + entityType + " in your config.yml.");
            return false;
        }
        return true;
    }

    public static boolean validateEntityInApplication(String applicationName, List<JsonNode> sparkEntitiesInApplication, String entityType) {
        if (sparkEntitiesInApplication == null || sparkEntitiesInApplication.isEmpty()) {
            logger.debug("No " + entityType + " found for the current Spark application " + applicationName);
            return false;
        }
        return true;
    }

    public static String buildUrl(String serverUrl, String suffix) {
        StringBuilder url = new StringBuilder(serverUrl);
        return url.append(suffix).toString();
    }

    public static List<JsonNode> fetchSparkEntity(CloseableHttpClient httpClient, String url) {
        List<JsonNode> entities = Lists.newArrayList();
        ;
        try {
            JsonNode jsonNode = HttpClientUtils.getResponseAsJson(httpClient, url, JsonNode.class);
            for (JsonNode node : jsonNode) {
                entities.add(node);
            }
        } catch (Exception ex) {
            logger.error("Error while fetching spark entity from url : " + url);
        }
        return entities;
    }

    public static boolean validateAppResponse(List<JsonNode> applications) {

        if (applications != null && applications.size() > 0) {
            logger.info("Applications are recorded in your spark history server. Starting fetching metrics...");
            return true;
        } else {
            logger.warn("No applications are recorded by your spark history server or endpoint did not returned any response. Exiting...");
            return false;
        }
    }
}

