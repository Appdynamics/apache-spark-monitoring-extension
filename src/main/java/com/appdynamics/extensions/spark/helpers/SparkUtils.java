package com.appdynamics.extensions.spark.helpers;

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

public class SparkUtils {
    private static final Logger logger = LoggerFactory.getLogger(SparkUtils.class);
    public static JsonNode getJsonNode(CloseableHttpResponse response) throws IOException {
        String data = EntityUtils.toString(response.getEntity(), "UTF-8");
        return getJsonNode(data);
    }

    private static JsonNode getJsonNode(String data) throws IOException {
        if (data == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(data, JsonNode.class);
    }

    public static BigDecimal convertDoubleToBigDecimal(Double value) {
        return new BigDecimal(Math.round(value));
    }

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

