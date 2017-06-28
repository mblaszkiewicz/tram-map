package data_structures

import play.api.Logger

case class Trip(tripId: String, status: String){

  def canEqual(a: Any) = a.isInstanceOf[Trip]
  override def equals(that: Any): Boolean =
    that match {
      case that: Trip => that.canEqual(this) && this.tripId == that.tripId
      case _ => false
    }
}

object Trip{
  var plannedTrips = Set[Trip]()
  def update(trips: List[Trip]) = {
    plannedTrips.synchronized {
      plannedTrips = plannedTrips ++ trips.toSet
      plannedTrips = plannedTrips.filter(trip => trip.status == "PLANNED")
      Logger.info(s"Set of planned trips = ${plannedTrips}")
    }
  }
}
