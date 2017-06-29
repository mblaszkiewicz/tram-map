package controllers

import javax.inject._
import java.util.Calendar
import scala.collection.mutable.Map
import data_structures.{LightTram, PieceOfSchedule, Schedule, Tram, Stop}
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Logger

//Ogólnie czy ten jason wysyłany mógłby być jako trams: [lista tych które się poruszają],
// deleted: [lista tych które trzeba usunąć] ??? (w ogóle czy trzeba tych do usuwania?)

@Singleton
class RequestController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action {
    //czy to modyfikuje na stałe Tram.currentList???
    var listMoving: List[LightTram] = Tram.currentList.
      filter(tram => {tram.isDeleted.isEmpty}).
      map(tram => LightTram(tram))
    var listDeleted: List[Int] = Tram.currentList
        .filter(tram => {tram.isDeleted.isDefined})
        .map(tram => (tram.id takeRight 5).toInt)

    //planned trips

    //MOCK routs to mapa indeksowana krótkimi nazwami przystanków (od, do) zawierająca listę kolejnych współrzędnych odcinka
    var routes = Map[(String, String),List[(Int, Int)]]()
    var lis = List((1,1), (2,2), (3,3))
    routes = routes + (("1","2") -> List((180367133,72043450), (180234831, 71825984), (180074257, 71632051), (180064597, 71601349)))


    var planned = List[LightTram]()
    for((vehicle, schedule) <- Schedule.schedules) {

      val prevStop = schedule.old take 1
      val nextStop = schedule.actual take 1

      (prevStop, nextStop) match {
        case (_, Nil) => {
          listDeleted =  (vehicle takeRight 5).toInt :: listDeleted
        }
        case (Nil, _) => {
          val lat = Stop.getLatitude(schedule.actual(0).stop.shortName)
          val lon = Stop.getLongitude(schedule.actual(0).stop.shortName)
          val tram = new LightTram((vehicle takeRight 5).toInt,
            "PLANNED " + schedule.routeName + " - " + schedule.directionText,
            lat.toDouble / 3600000, lon.toDouble / 3600000)
          listMoving = tram :: listMoving
        }
        case (prev, next) => {
          val route = routes.get(prev(0).stop.shortName, next(0).stop.shortName)
          route match {
            case Some(route) => {
              var duration = 0
              var offset = 0
              var now = Calendar.getInstance()
              (prev(0).plannedTime, next(0).plannedTime) match {
                case (Some(p), Some(n)) => {
                  duration = toTime(n) - toTime(p)
                  offset = now.get(Calendar.HOUR_OF_DAY) * 3600
                  offset += (now.get(Calendar.MINUTE) * 60)
                  offset += now.get(Calendar.SECOND)
                  offset -= toTime(p)
                }
                case (_, _) => {
                  duration = 1
                  offset = 0
                }
              }
              var position: Int = (offset * route.size) / duration
              if( position < route.size )
              {
                var tram = new LightTram((vehicle takeRight 5).toInt,
                  "PLANNED " + schedule.routeName + " - " + schedule.directionText,
                  route(position)._1.toDouble/3600000, route(position)._2.toDouble/3600000)
                listMoving = tram :: listMoving
              }
            }
            case None => None
          }
        }
      }
    }

    implicit val lightTramWrites: Writes[LightTram] = Json.writes[LightTram]

    val json = Json.toJson(listMoving)
    val json2 = Json.toJson(listDeleted)
    //bardzo profesjonalne rozwiązanie
    Logger.info(s"JSON ${json}")
    Ok("{\"trams\":" + json + ", \"deleted\":" + json2 + "}")
  }

  def toTime(s: String): Int = {
    (s take 2).toInt * 3600 + (s takeRight 2).toInt * 60
  }
}

