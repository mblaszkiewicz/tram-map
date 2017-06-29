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
import data_structures._
import play.api.libs.json._

import scala.concurrent.ExecutionContext


@Singleton
class ScheduleProvider @Inject()(actorSystem: ActorSystem, ws: WSClient)
                            (implicit exec: ExecutionContext) {

  private val request: WSRequest = ws.url("http://www.ttss.krakow.pl/internetservice/services/tripInfo/tripPassages")
    .addQueryStringParameters("mode" -> "arrival")

  actorSystem.scheduler.schedule(10 seconds, 10 seconds)(requestForTripsDetails())

  implicit val stopReads = Json.reads[Stop]
  implicit val pieceOfScheduleReads = Json.reads[PieceOfSchedule]
  implicit val scheduleReads = Json.reads[Schedule]

  def requestForTripsDetails() = {

    for( trip <- Trip.plannedTrips) {
      Thread.sleep(100)
      val tripRequest: WSRequest =
        request.addQueryStringParameters("tripId" -> trip.tripId)

      val futureResponse: Future[WSResponse] = tripRequest.withRequestTimeout(5.second).get()

      var schedule: Future[Schedule] = futureResponse.map {
        response =>
          (response.json ).as[Schedule]
      }

      schedule onComplete{
        case Success(s) => {
          //Logger.info(s"Schedule ${s}!")
          Schedule.update(trip.tripId, s)
        }
        case Failure(e) => {
          //Logger.info(s"Schedule problem with tripId = ${trip.tripId}")
        }
      }
    }
  }
}