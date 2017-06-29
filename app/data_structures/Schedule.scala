package data_structures

import scala.collection.mutable.Map

case class PieceOfSchedule(actualTime: Option[String], plannedTime: Option[String], status: String, stop: Stop)
case class Schedule(actual: List[PieceOfSchedule], old: List[PieceOfSchedule], directionText: String, routeName: String)

object Schedule{
  var schedules = Map[String, (Schedule, String)]()
  var counter =1
  //MOCK
  //val prev = new PieceOfSchedule(None, Some("20:17"), "PLANNED", new Stop("1", None, None, "1", "1"))
  //val next = new PieceOfSchedule(None, Some("20:18"), "PLANNED", new Stop("2", None, None, "2", "2"))
  //schedules("123400001") = new Schedule(List(next), List(prev), "TEST", "milion")
  def update(tripId: String, schedule: Schedule) = {
    schedules.synchronized {
      schedules.get(tripId) match{
        case Some(trip) => schedules(tripId) = (schedule, trip._2)
        case None => {
          schedules(tripId) = (schedule, counter.toString)
          counter += 1
        }
      }
    }
  }
}
