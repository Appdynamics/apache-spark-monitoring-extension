/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark.metrics;

/**
 * Created by aditya.jagtiani on 5/15/17.
 */
public class MetricProperties {
    private String metricName;
    private String alias;
    private String aggregationType;
    private String timeRollupType;
    private String clusterRollupType;
    private String multiplier;
    private String metricPath;
    private boolean delta;

    public String getAlias() {
        return alias;
    }

    void setAlias(String alias) {
        this.alias = alias;
    }

    String getMetricName() {
        return metricName;
    }

    void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getAggregationType() {
        return aggregationType;
    }

    void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
    }

    public String getTimeRollupType() {
        return timeRollupType;
    }

    void setTimeRollupType(String timeRollupType) {
        this.timeRollupType = timeRollupType;
    }

    public String getClusterRollupType() {
        return clusterRollupType;
    }

    void setClusterRollupType(String clusterRollupType) {
        this.clusterRollupType = clusterRollupType;
    }

    public String getMultiplier() {
        return multiplier;
    }

    void setMultiplier(String multiplier) {
        this.multiplier = multiplier;
    }

    void setMetricPath(String metricPath) {
        this.metricPath = metricPath;
    }
    public String getMetricPath() {
        return metricPath;
    }

    void setDelta(boolean delta) {
        this.delta = delta;
    }

    public boolean getDelta() {
        return delta;
    }
}
