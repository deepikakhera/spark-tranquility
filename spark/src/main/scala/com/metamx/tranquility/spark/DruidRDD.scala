package com.metamx.tranquility.spark

/**
 * Created by dkhera on 11/23/15.
 */
import com.twitter.util.Await
import org.apache.spark.rdd.RDD
import scala.reflect.ClassTag
import com.metamx.common.scala.Logging
import scala.language.implicitConversions

/**
 * Created by dkhera on 11/20/15.
 */
class DruidRDD[T: ClassTag](rdd:RDD[T]) extends Logging with Serializable{

  def writeToDruid (beamFactory: BeamFactory[T]) = {
  //  print("WIRTING" + rdd.partitions.length)
    rdd.foreachPartition {
      partitionOfRecords => {
        print("create beam")
        val beam = beamFactory.makeBeam //Code is stuck here
        print("checkpoint 3")
        print("written => "+ Await.result(beam.propagate(partitionOfRecords.toIndexedSeq)))
     //   log.info("Sent "+ Await.result(beam.propagate(partitionOfRecords.toIndexedSeq)) + " events to Druid")
      }
    }
  }
}

object DruidRDD extends Serializable{
  //define an implicit function, addDruidFunctionsToRDD which will add all the druid functions (defined in DruidFunctions) to the RDD[T]
  implicit def addDruidFunctionsToRDD[T: ClassTag](rdd: RDD[T]) = new DruidRDD(rdd)
}
