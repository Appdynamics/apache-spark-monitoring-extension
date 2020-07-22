/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark.helpers;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by aditya.jagtiani, abhishek.saxena on 08/July/2020.
 */

public class SparkUtils {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(SparkUtils.class);

    public static boolean isValidationSuccessful(List<Map> configuredMetrics, List<JsonNode> sparkEntitiesInApplication, String entityType) {
        if (configuredMetrics == null || configuredMetrics.isEmpty()) {
            logger.debug("No metrics have been configured for : " + entityType + " in your config.yml.");
            return false;
        } else if (sparkEntitiesInApplication == null || sparkEntitiesInApplication.isEmpty()) {
            logger.debug("No " + entityType + " found for the current Spark application.");
            return false;
        }
        return true;
    }
}

