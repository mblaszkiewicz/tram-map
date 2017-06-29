package data_structures

import play.api.Logger

case class StopA(id: String, name: String, number: String)

object StopA{
  var list: List[StopA] = List()

  def update(newList: List[StopA]): Unit = {
    list.synchronized {
      list = newList
    }
  }
}
