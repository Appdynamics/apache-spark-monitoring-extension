/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark.metrics;

import java.util.Map;

/**
 * Created by aditya.jagtiani on 5/15/17, abhishek.saxena on 7/8/20.
 */
public class MetricProperties {
    private String name;
    private String alias;
    private String aggregationType;
    private String timeRollUpType;
    private String clusterRollUpType;
    private String multiplier;
    private boolean delta;
    private Map convert;

    public String getAlias() {
        return alias;
    }

    void setAlias(String alias) {
        this.alias = alias;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getAggregationType() {
        return aggregationType;
    }

    void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
    }

    public String getTimeRollUpType() {
        return timeRollUpType;
    }

    void setTimeRollUpType(String timeRollUpType) {
        this.timeRollUpType = timeRollUpType;
    }

    public String getClusterRollUpType() {
        return clusterRollUpType;
    }

    void setClusterRollUpType(String clusterRollUpType) {
        this.clusterRollUpType = clusterRollUpType;
    }

    public String getMultiplier() {
        return multiplier;
    }

    void setMultiplier(String multiplier) {
        this.multiplier = multiplier;
    }

    void setDelta(boolean delta) {
        this.delta = delta;
    }

    public boolean getDelta() {
        return delta;
    }

    public Map getConvert() {
        return convert;
    }

    public void setConvert(Map convert) {
        this.convert = convert;
    }
}
