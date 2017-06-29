package data_structures

import scala.collection.immutable.ListMap


object WaypointMap{
  var routes: ListMap[(String, String), List[(Long, Long)]] = ListMap()

  def update(new_map: ListMap[(String, String), List[(Long, Long)]]): Unit ={
    routes = new_map
  }
}
