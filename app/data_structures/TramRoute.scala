package data_structures

import play.api.Logger

case class TramRoute(alerts: List[String], authority: String, directions: Option[List[String]], id: String, name: String, shortName: String)

object TramRoute{
  var list: List[TramRoute] = List()

  def update(newList: List[TramRoute]): Unit = {
    list = newList
  }
}
