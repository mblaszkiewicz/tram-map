package data_structures

case class Stop(id: String, latitude: Option[Long], longitude: Option[Long], name: String, shortName: String)

object Stop{
  var stopList: List[Stop] = List()

  def update(newList: List[Stop]): Unit = {
    stopList.synchronized {
      stopList = newList
    }
  }

  def getLatitude(shortName: String) = {
    stopList.synchronized {
      var lat = for (stop <- stopList if stop.shortName == shortName) yield stop.latitude
      lat.head match{
        case Some(lat) => lat
        case _ => 0
      }
    }
  }
  def getLongitude(shortName: String): Long = {
    stopList.synchronized {
      var lon = for (stop <- stopList if stop.shortName == shortName) yield stop.longitude
      lon.head match{
        case Some(lon) => lon
        case _ => 0
      }
    }
  }
}
