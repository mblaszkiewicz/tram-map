package data_structures

import play.api.Logger

case class StopsToWaypoints(map: Map[(String, String), List[(Long, Long)]])
/*
object StopsToWaypoints{

  def update(newMap: StopsToWaypoints): Unit = {
    map = newMap
  }
}
*/