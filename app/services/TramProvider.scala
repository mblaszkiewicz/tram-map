package services


import javax.inject.Singleton
import javax.inject.Inject

import scala.concurrent.Future
import scala.concurrent.duration._
import play.api.mvc._
import play.api.libs.ws._
import play.api.http.HttpEntity
import play.api.Logger
import akka.actor.ActorSystem

import scala.util.{Failure, Success}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import data_structures.Tram
import play.api.libs.json._

import scala.concurrent.ExecutionContext


@Singleton
class TramProvider @Inject()(actorSystem: ActorSystem, ws: WSClient)
                                    (implicit exec: ExecutionContext){

  //nie mam pojęcia czy zostało to uieszczone w dobrym miejscu w kontekście sztuki developerskiej, ale trudno
  actorSystem.scheduler.schedule(100 milliseconds, 1 seconds)(requestForTrams())

  private val request: WSRequest = ws.url("http://www.ttss.krakow.pl/internetservice/" +
    "geoserviceDispatcher/services/vehicleinfo/vehicles?positionType=CORRECTED")

  //powinno się je przenieść do odpowiednich klas, ale nie wiedzieć czemu wtey się nie kompiluje
  implicit val tramReads = Json.reads[Tram]

  def requestForTrams() = {

    val futureResponse: Future[WSResponse] = request.withRequestTimeout(5.second).get()

    var lastUpdate: Future[Long] = futureResponse.map {
      response =>
        (response.json \ "lastUpdate").as[Long]
    }
    var vehicles: Future[List[Tram]] = futureResponse.map {
      response =>
        (response.json \ "vehicles").as[List[Tram]]
    }

    lastUpdate onComplete  {
      case Success(time) => vehicles onComplete {
        case Success(trams) => {
          Logger.info(s"Requested ${time}!");
          Tram.update(time, trams);
        }
      }
    }
  }
}
