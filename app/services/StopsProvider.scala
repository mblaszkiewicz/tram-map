package services

import javax.inject.{Inject, Singleton}

import data_structures.{Stop}
import play.api.Logger
import play.api.libs.json.Json

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}

import scala.concurrent.duration._
import play.api.mvc._
import play.api.libs.ws._
import play.api.http.HttpEntity
import play.api.Logger
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString


@Singleton
class StopsProvider @Inject()(actorSystem: ActorSystem, ws: WSClient)
                            (implicit exec: ExecutionContext){

  actorSystem.scheduler.schedule(200 milliseconds, 1 days)(requestForStops())

  private val request: WSRequest = ws.url("http://www.ttss.krakow.pl/internetservice/geoserviceDispatcher/services/" +
    "stopinfo/stops?left=-648000000&bottom=-324000000&right=648000000&top=324000000")

  implicit val stopReads = Json.reads[Stop]

  def requestForStops() = {
    val futureResponse: Future[WSResponse] = request.withRequestTimeout(5.second).get()

    var stops: Future[List[Stop]] = futureResponse.map {
      response =>
        (response.json \ "stops").as[List[Stop]]
    }

    stops onComplete{
      case Success(stopsList) => {
        //Logger.info(s"Stop list ${stopsList}!");
        Stop.update(stopsList);
      }
    }
  }
}
