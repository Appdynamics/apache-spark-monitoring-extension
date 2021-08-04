package com.appdynamics.extensions.spark;

import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SparkMonitorTest {

    @Test
    public void test() throws TaskExecutionException {
        SparkMonitor monitor = new SparkMonitor();
        Map<String, String> taskArgs = new HashMap<>();

        taskArgs.put("config-file", "src/test/resources/conf/test_config.yml");

        monitor.execute(taskArgs, null);
    }
}
