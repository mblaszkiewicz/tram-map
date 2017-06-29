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
import data_structures.{Stop, Tram, Trip}
import play.api.libs.json._

import scala.concurrent.ExecutionContext


@Singleton
class TripProvider @Inject()(actorSystem: ActorSystem, ws: WSClient)
                            (implicit exec: ExecutionContext) {

  private val request: WSRequest = ws.url("http://www.ttss.krakow.pl/internetservice/services/" +
    "passageInfo/stopPassages/stop")
    .addQueryStringParameters("mode" -> "arrival")

  actorSystem.scheduler.schedule(5 seconds, 300 seconds)(requestForTrips())

  implicit val tripReads = Json.reads[Trip]

  def requestForTrips() = {

    for( stop <- Stop.stopList) {

      Thread.sleep(100)
      val stopRequest: WSRequest =
        request.addQueryStringParameters("stop" -> stop.shortName)

      val futureResponse: Future[WSResponse] = stopRequest.withRequestTimeout(5.second).get()

      var trips: Future[List[Trip]] = futureResponse.map {
        response =>
          (response.json \ "actual").as[List[Trip]]
      }

      trips onComplete{
        case Success(tripList) => {
          Logger.info(s"Trip list ${tripList}!");
          Trip.update(tripList)
        }
      }
    }
  }
}
