package data_structures

case class Stop(id: String, latitude: Option[Long], longitude: Option[Long], name: String, shortName: String)

object Stop{
  var stopList: List[Stop] = List()

  def update(newList: List[Stop]): Unit = {
    stopList.synchronized {
      stopList = newList
    }
  }
}
