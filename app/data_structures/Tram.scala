package data_structures

import play.api.Logger

case class Tram(id: String, isDeleted: Option[Boolean], name: Option[String], longitude: Option[Long], latitude: Option[Long])

object Tram{
  var currentList: List[Tram] = List()
  var previousList: List[Tram] = List()
  var lastUpdate: Long = 0

  def update(time: Long, newList: List[Tram]): Unit = {
    lastUpdate = time
    previousList = currentList
    currentList = newList
  }
}
