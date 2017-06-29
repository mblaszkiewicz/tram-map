package data_structures

import play.api.Logger

case class StopData(category: String, id: String, latitude: Long, longitude: Long, name: String, shortName: String)

object StopData{
  var list: Map[String, StopData] = Map()

  def update(newList: List[StopData]): Unit = {
    list.synchronized {
      list = newList filter (t => t.category == "tram") map (t => t.shortName -> t) toMap
    }
  }
}

