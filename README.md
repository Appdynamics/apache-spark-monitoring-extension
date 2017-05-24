# Apache-spark-monitoring-extension
AppDynamics Machine Agent Extension for use with Apache Spark

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
  ./bin/spark-submit --class <main-class> --master <master-url> --deploy-mode <deploy-mode> --conf <key>=<value> --properties-  file <path_to_spark_defaults_conf/spark-defaults.conf>
 </pre>
 
 4. Repeat this for a worker node prior to deploying apps any apps on it. 
eer
