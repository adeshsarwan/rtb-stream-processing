name := "xrtb-spark-streaming"

version := "0.1"

scalaVersion := "2.11.12"
val sparkVersion = "2.4.6"

resolvers ++= Seq (
  "Spark Packages Repo" at "https://dl.bintray.com/spark-packages/maven"
)

libraryDependencies ++= Seq (
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.bahir" %% "spark-streaming-zeromq" % "2.4.0",
  "org.elasticsearch" %% "elasticsearch-spark-20" % "7.13.3",
  "org.reactivestreams" % "reactive-streams" % "1.0.3",
  "org.yaml" % "snakeyaml" % "1.27",
  "com.typesafe" % "config" % "1.4.1"
)

