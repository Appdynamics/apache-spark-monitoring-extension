## AppDynamics Monitoring Extension for use with Apache Spark

#### Use Case

Apache Spark is a fast and general purpose cluster computing system. It provides high level APIs in Java, Scala, Python & R as well as an optimized engine that supports general execution graphs. It also supports a tool called Spark SQL for SQL and relational data processing. 

The AppDynamics Spark Extension can monitor multiple Spark clusters and worker nodes and extracts metrics from every running and completed Spark application, more specifically the jobs, executors, stages and storage RDDs within these applications. The metrics reported by the extension can be configured by users. We have developed this extension using the latest version of Spark (2.1.0). 

#### Prerequisites

1. This extension requires an AppDynamics Java Machine Agent up and running. 
2. This extension will fetch metrics from Spark applications running in a cluster setup. Spark application metrics persist only as long as the application is alive, which makes it essential to have a repository or metric dump which stores these metrics even after the application has been terminated. 
3. Spark offers a number of metric dumps - REST, JMX, CSV etc. This extension uses a REST dump in the form of a Spark History Server. Please refer to the next section for instructions on how to configure and use the History Server. 
4. More general Spark related information can be found on the Spark homepage - http://spark.apache.org/docs/2.1.0/ 

#### Configuring Spark for metric persistence

As mentioned above, the Spark History Server is essential for metrics to persist even when applications have been killed or completed. To use the history server, the following needs to be done: 

1. Make a copy of SPARK_HOME/conf/spark-defaults.conf.template and name it as spark-defaults.conf. This file can be used to specify properties for your Spark applications. By default, the .template file is used. 
2. Uncomment/add the following properties to your spark-defaults.conf file: 
  <pre>
  spark.eventLog.enabled           true
  spark.eventLog.dir               file:/tmp/spark-events #Can be modified to your preference. Create a directory called    spark-events in your /tmp/ folder if you wish to use this exact same configuration.
  spark.history.provider		  org.apache.spark.deploy.history.FsHistoryProvider
  spark.history.fs.logDirectory		file:/tmp/spark-events #Can be modified to your preference
  spark.history.fs.update.interval	10s #Can be modified to your preference
  spark.history.retainedApplications	50 #Can be modified to your preference
  spark.history.ui.maxApplications	5000 #Can be modified to your preference
  spark.history.ui.port 			18080 #Can be modified to your preference
  spark.history.kerberos.enabled		false
  </pre>
 3. Once this is done, use this property **--properties-file path_to_spark_defaults_conf/spark-defaults.conf** along with your spark-submit script. For example: 
  <pre>
  ./bin/spark-submit xyz --properties-file <path_to_spark_defaults_conf/spark-defaults.conf>
 </pre>
 
 4. Repeat this for a worker node prior to deploying apps any apps on it. 
 
#### Installing the Extension
1.  Unzip the contents of 'SparkMonitor'-<version>.zip file and copy the directory to `<your-machine-agent-dir>/monitors</your-machine-agent-dir>`.</version>
2.  Edit the config.yml file. An example config.yml file follows these installation instructions.
3.  Restart the Machine Agent.

**Sample config.yaml:** The following is a sample config.yaml file that uses one Spark history server to monitor data. Once you have configured the history server across your various nodes, they can be added to the servers tab. The metrics shown in the file are customizable. You can choose to remove metrics or en entire section (jobs, stages etc) and they won't be reported. You can also add properties to individual metrics. The following properties can be added: 
1. alias: The actual name of the metric as you would see it in the metric browser
2. multiplier: Used to transform the metric value, particularly for cases where memory is reported in bytes. 1.0 by default. 
3. delta: Used to display a 'delta' or a difference between metrics that have an increasing value every minute. False by default.
4. cluster: The cluster-rollup qualifier specifies how the Controller aggregates metric values in a tier (a cluster of nodes). The value is an enumerated type. Valid values are **INDIVIDUAL** (default) or **COLLECTIVE**. 
5. aggregation: The aggregator qualifier specifies how the Machine Agent aggregates the values reported during a one-minute period. Valid values are **AVERAGE** (default) or **SUM** or **OBSERVATION**. 
6. time: The time-rollup qualifier specifies how the Controller rolls up the values when it converts from one-minute granularity tables to 10-minute granularity and 60-minute granularity tables over time. Valid values are **AVERAGE** (default) or **SUM** or **CURRENT**. 
<pre>
#This will publish metrics to one tier (highly recommended)
#Instructions on how to retrieve the Component ID can be found in the Metric Path section of https://docs.appdynamics.com/display/PRO42/Build+a+Monitoring+Extension+Using+Java
#For Controllers 4.2.10+, you can also use the tier name instead of the component ID.
metricPrefix: "Server|Component:<COMPONENT_ID>|Custom Metrics|Spark Monitor|"

#Add your Spark History Servers here. Please refer to the extension docs for Spark history & event logging configuration details.
servers:
   - host: "localhost"
     port: 18080
     name: "Spark Master"


#Generic metric prefix used to show metrics in AppDynamics (not recommended)
#metricPrefix: "Custom Metrics|Spark"

numberOfThreads: 5

## This section can be used to configure metrics published by the extension. You have the ability to add multipliers & modify the metric qualifiers for each metric.
## Valid 'cluster' rollup values: INDIVIDUAL, COLLECTIVE
## Valid 'aggregation' types: AVERAGE, SUM, OBSERVATION
## Valid 'time' rollup types: AVERAGE, SUM, CURRENT
## You can choose to not add any or all of these fields to any metric and the default values for each of the above will be used (INDIVIDUAL, AVERAGE & AVERAGE for cluster, aggregation & time respectively).
metrics:
     jobs:
        - numTasks:
            alias: "numTasks"  #Number of tasks in the application
            multiplier: "1.0"
            cluster: "INDIVIDUAL"
            aggregation: "AVERAGE"
            time: "SUM"
            delta: "false"
        - numCompletedTasks:
            alias: "numCompletedTasks"  #Number of completed tasks in the application
            multiplier: ""
        - numSkippedTasks:
            alias: "numSkippedTasks"  #Number of completed tasks in the application"
            multiplier: ""
        - numFailedTasks:
            alias: "numFailedTasks"  #Number of completed tasks in the application
            multiplier: ""
        - numActiveStages:
            alias: "numActiveStages"  #Number of active stages
            multiplier: ""
        - numCompletedStages:
            alias: "numCompletedStages"  #Number of completed stages
            multiplier: ""
        - numSkippedStages:
            alias: "numSkippedStages"  #Number of completed stages"
            multiplier: ""
        - numFailedStages:
            alias: "numFailedStages"  #Number of failed stages"
            multiplier: ""

     stages:
        - numActiveTasks:
            alias: "numActiveTasks"  #Number of active tasks in the application's stages
            multiplier: ""
        - numCompleteTasks:
            alias: "numCompleteTasks"  #Number of complete tasks in the application's stages
            multiplier: ""
        - numFailedTasks:
            alias: "numCompleteTasks"  #Number of failed tasks in the application's stages
            multiplier: ""
        - executorRunTime:
            alias: "executorRunTime"  #Time spent by executor in the application's stages (ms)
            multiplier: ""
        - inputBytes:
            alias: "inputBytes"  #Input bytes in the application's stages
            multiplier: ""
        - inputRecords:
            alias: "inputRecords"  #Input records in the application's stages
            multiplier: ""
        - outputBytes:
            alias: "outputBytes"  #Output bytes in the application's stages
            multiplier: ""
        - outputRecords:
            alias: "outputRecords"  #Output records in the application's stages
            multiplier: ""
        - shuffleReadBytes:
            alias: "shuffleReadBytes"  #Number of bytes read during a shuffle in the application's stages
            multiplier: ""
        - shuffleReadRecords:
            alias: "shuffleReadRecords"  #Number of records read during a shuffle in the application's stages
            multiplier: ""
        - shuffleWriteBytes:
            alias: "shuffleReadRecords"  #Number of bytes written during a shuffle in an application's stages
            multiplier: ""
        - shuffleWriteRecords:
            alias: "shuffleWriteRecords"  #Number of shuffled records in the application's stages
            multiplier: ""
        - memoryBytesSpilled:
            alias: "memoryBytesSpilled"  #Number of bytes spilled to disk in the application's stages
            multiplier: ""
        - diskBytesSpilled:
            alias: "diskBytesSpilled"  #Max size on disk of the spilled bytes in the application's stages
            multiplier: ""

     executors:
        - rddBlocks:
            alias: "rddBlocks"  #Number of persisted RDD blocks in the application's executors
            multiplier: ""
        - memoryUsed:
            alias: "memoryUsed" #Amount of memory used for cached RDDs in the application's executors
            multiplier: ""
        - diskUsed:
            alias: "diskUsed" #Amount of disk space used by persisted RDDs in the application's executors
            multiplier: ""
        - activeTasks:
            alias: "activeTasks" #Number of active tasks in the application's executors
            multiplier: ""
        - failedTasks:
            alias: "failedTasks" #Number of failed tasks in the application's executors
            multiplier: ""
        - completedTasks:
            alias: "completedTasks" #Number of completed tasks in the application's executors
            multiplier: ""
        - totalTasks:
            alias: "totalTasks" #Total number of tasks in the application's executors
            multiplier: ""
        - totalDuration:
            alias: "totalDuration" #Time spent by the application's executors executing tasks (ms)
            multiplier: ""
        - totalInputBytes:
            alias: "totalInputBytes" #Total number of input bytes in the application's executors
            multiplier: ""
        - totalShuffleRead:
            alias: "totalShuffleRead" #Total number of bytes read during a shuffle in the application's executors
            multiplier: ""
        - totalShuffleWrite:
            alias: "totalShuffleWrite"  #Total number of shuffled bytes in the application's executors
            multiplier: ""
        - maxMemory:
            alias: "maxMemory" #Maximum memory available for caching RDD blocks in the application's executors
            multiplier: ""

     rdd:
        - numPartitions:
            alias: "numPartitions" #Number of persisted RDD partitions in the application/second
            multiplier: ""
        - numCachedPartitions:
            alias: "numCachedPartitions" #Number of in-memory cached RDD partitions in the application/second
            multiplier: ""
        - memoryUsed:
            alias: "memoryUsed" #Amount of memory used in the application's persisted RDDs
            multiplier: ""
        - diskUsed:
            alias: "diskUsed"  #Amount of disk space used by persisted RDDs in the application
            multiplier: ""
</pre>

#### Workbench

Workbench is a feature by which you can preview the metrics before registering it with the controller. This is useful if you want to fine tune the configurations. Workbench is embedded into the extension jar.  
To use the workbench

1.  Follow all the installation steps
2.  Start the workbench with the command

    <pre>      java -jar <machine-agent-dir>/monitors/SparkMonitor/spark-monitoring-extension.jar</machine-agent-dir> </pre>

    This starts an http server at http://host:9090/. This can be accessed from the browser.
3.  If the server is not accessible from outside/browser, you can use the following end points to see the list of registered metrics and errors.

    <pre>#Get the stats
        curl http://localhost:9090/api/stats
        #Get the registered metrics
        curl http://localhost:9090/api/metric-paths
    </pre>

4.  You can make the changes to config.yml and validate it from the browser or the API
5.  Once the configuration is complete, you can kill the workbench and start the Machine Agent.

#### Support

Please contact [help@appdynamics.com](mailto:help@appdynamics.com) with the following details

1.  config.yml
2.  debug logs

#### Compatibility

<table border="0" cellpadding="0">

<tbody>

<tr>

<td style="text-align: right; width: 210px">Version</td>

<td>1.6.0</td>

</tr>

<tr>

<td style="text-align: right">Machine Agent Compatibility</td>

<td>4.0+</td>

</tr>

<tr>

<td style="text-align: right">Last Update</td>

<td>06/09/15</td>

</tr>

</tbody>

</table>

#### Codebase

You can contribute your development ideas [here.](https://github.com/Appdynamics/apache-spark-monitoring-extension)
