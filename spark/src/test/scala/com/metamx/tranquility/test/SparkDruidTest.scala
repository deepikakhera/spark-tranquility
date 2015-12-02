package com.metamx.tranquility.test

/**
  * Created by dkhera on 11/23/15.
  */
/*
 * Tranquility.
 * Copyright 2013, 2014, 2015  Metamarkets Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.metamx.common.scala.Predef._
import com.metamx.common.scala.timekeeper.TestingTimekeeper
import com.metamx.tranquility.beam.Beam
import com.metamx.tranquility.spark.BeamFactory
import com.metamx.tranquility.spark.DruidRDD.addDruidFunctionsToRDD
import com.metamx.tranquility.test.common.{JulUtils, CuratorRequiringSuite, DruidIntegrationSuite}
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.BoundedExponentialBackoffRetry
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scala_tools.time.Imports._
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import com.metamx.common.scala.Logging
import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class SparkDruidTest
  extends FunSuite with DruidIntegrationSuite with CuratorRequiringSuite with Logging with BeforeAndAfterAll {
  private var sparkContext: SparkContext = null
  private var checkpointDir: String = _
  private var ssc: StreamingContext = null
  override def beforeAll(): Unit = {
    sparkContext = new SparkContext(
      new SparkConf().setMaster("local").setAppName("SparkDruidTest").set("tranquility.zk.connect", "localhost:2181"))
    checkpointDir = "./checkpoint" //change this
    ssc = new StreamingContext(sparkContext, Seconds(3))
    //ssc.checkpoint(checkpointDir) //queue stream does not supper checkpointing

  }

  override def afterAll(): Unit = {
    if (sparkContext != null) {
      sparkContext.stop()
    }
    if (ssc!=null)
      ssc.stop()
  }

  JulUtils.routeJulThroughSlf4j()
  test("Spark to Druid") {
    withDruidStack {
      (curator, broker, overlord) =>
        val zkConnect = curator.getZookeeperClient.getCurrentConnectionString
        val now = new DateTime().hourOfDay().roundFloorCopy()

        val inputs = DirectDruidTest.generateEventsQ(now)
        val t = sparkContext.parallelize(inputs)
        val lines = mutable.Queue[RDD[SimpleEvent]]()
        val dstream = ssc.queueStream(lines)
        lines += sparkContext.makeRDD(inputs)


     //   print("dstream created" + dstream.count())
        // implicit val beamFactory = new SimpleEventBeam
        dstream.foreachRDD(rdd => rdd.writeToDruid(new SimpleEventBeam))
       // dstream.foreachRDD(rdd => print("len==="+rdd.partitions.length))
        ssc.start()
        runTestQueriesAndAssertions(
          broker, new TestingTimekeeper withEffect {
            timekeeper =>
              timekeeper.now = now
          }
        )

    }
  }

}

class SimpleEventBeam extends BeamFactory[SimpleEvent] {

  override def  makeBeam: Beam[SimpleEvent] = {
    val aDifferentCurator = CuratorFrameworkFactory.newClient(
      "localhost:2181",
      new BoundedExponentialBackoffRetry(100, 500, 5)
    )
    aDifferentCurator.start()
    val builder = DirectDruidTest.newBuilder(
      aDifferentCurator, new TestingTimekeeper withEffect {
        timekeeper =>
          timekeeper.now = DateTime.now
      }
    )
    print("reaches here ok")

    builder.buildBeam()
  }

  def main(args: Array[String]) {
    makeBeam
  }
}


