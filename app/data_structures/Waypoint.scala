package data_structures

import play.api.Logger

case class Waypoint(lat: Long, lon: Long, seq: String)

object Waypoint{
  var list: List[Waypoint] = List()

  def update(newList: List[Waypoint]): Unit = {
    list = newList
  }
}
