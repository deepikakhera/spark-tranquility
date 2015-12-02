package com.metamx.tranquility.spark

/**
 * Created by dkhera on 11/23/15.
 */
import com.metamx.tranquility.beam.Beam

/**
 * Created by dkhera on 10/30/15.
 */
trait BeamFactory[EventType] extends Serializable
{
  /**
   * Create a Beam for a given EventType.
   * It is recommended that the event that extends this Factory, does that as a lazy val. For example:
   * lazy val makeBeam : Beam[SimpleEvent] = {
   *
   * }
   *
   * @return beam for a BeamProducer
   */
  def makeBeam : Beam[EventType]

}
