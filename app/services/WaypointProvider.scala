package services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.actor.FSM.Failure
import data_structures.{TramRoute, StopA, StopData, Waypoint}
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Success
import play.api.mvc._
import play.api.libs.ws._
import play.api.http.HttpEntity
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext

@Singleton
class WaypointProvider @Inject()(actorSystem: ActorSystem, ws: WSClient)
                                (implicit exec: ExecutionContext){
  var stopPairsToWaypoints: ListMap[(String, String), List[(Long, Long)]] = ListMap()

  private val requestRoutes: WSRequest = ws.url("http://www.ttss.krakow.pl/internetservice/services/routeInfo/route")
  implicit val routesReads = Json.reads[TramRoute]

  requestForRoutes()

  def requestForRoutes() = {

    val futureResponse: Future[WSResponse] = requestRoutes.withRequestTimeout(5.second).get()

    var getRoutes: Future[List[TramRoute]] = futureResponse.map {
      response =>
        (response.json \ "routes").as[List[TramRoute]]
    }

    getRoutes onComplete {
      case Success(routes) => {
        Logger.info("Routes download successful!")
        TramRoute.update(routes)
        for (route <- TramRoute.list)
          route.directions match {
            case Some(directions) =>
              for(direction <- directions)
                requestForWaypoints(route.id, direction)
            case None => Logger.info("Routes download failure!")
          }
      }
      case _ => {
        Logger.info("Routes download failure!")
      }
    }
  }


  def requestForWaypoints(routeId: String, directions: String) = {
    val requestWaypoints: WSRequest = ws.url("http://www.ttss.krakow.pl/internetservice/geoserviceDispatcher/services/pathinfo/route?id=" + routeId + "&direction=" + directions)
    implicit val waypointsReads = Json.reads[Waypoint]
    val futureResponse: Future[WSResponse] = requestWaypoints.withRequestTimeout(5.second).get()

    var getWaypoints: Future[List[Waypoint]] = futureResponse.map {
      response =>
        ((response.json \ "paths")(0) \ "wayPoints").as[List[Waypoint]]
    }

    getWaypoints onComplete {
        case Success(waypoints) => {
          Logger.info("Waypoints download successful!")
          Waypoint.update(waypoints)
          requestForStops()
        }
        case _ => {
          Logger.info("Waypoints download failure!")
        }
    }
  }

  private val requestStops: WSRequest = ws.url("http://www.ttss.krakow.pl/internetservice/services/routeInfo/routeStops?routeId=" + "6350571212602605601" + "&direction=Bronowice%20Małe")
  implicit val stopsReads = Json.reads[StopA]

  def requestForStops() = {

    val futureResponse: Future[WSResponse] = requestStops.withRequestTimeout(5.second).get()

    var getStops: Future[List[StopA]] = futureResponse.map {
      response =>
        (response.json \ "stops").as[List[StopA]]
    }

    getStops onComplete {
      case Success(stops) => {
        Logger.info("Stop list download successful!")
        StopA.update(stops)
        requestForStopsData()
      }
      case _ => {
        Logger.info("Stop list download failure!")
      }
    }
  }

  private val requestStopData: WSRequest = ws.url("http://www.ttss.krakow.pl/internetservice/geoserviceDispatcher/services/stopinfo/stops?left=-648000000&bottom=-324000000&right=648000000&top=324000000")
  implicit val stopDataReads = Json.reads[StopData]

  def requestForStopsData() = {

    val futureResponse: Future[WSResponse] = requestStopData.withRequestTimeout(5.second).get()

    var getStopData: Future[List[StopData]] = futureResponse.map {
      response =>
        (response.json \ "stops").as[List[StopData]]
    }

    getStopData onComplete {
      case Success(stopData) => {
        Logger.info("Stop data download successful!")
        StopData.update(stopData)
        addStopWaypoints()
      }
      case _ => {
        Logger.info("Stop data download failure!")
      }
    }
  }

  def addStopWaypoints() = {
    var stopToWaypoint: Map[String, String] = Map()
    for(stop <- StopA.list) {
      var diff: Long = 0
      var mdiff: Long = 1000000000
      var mwaypoint = "N/A"
      for(waypoint <- Waypoint.list) {
        diff = math.abs((StopData.list(stop.number).latitude - waypoint.lat) + (StopData.list(stop.number).longitude - waypoint.lon))
        if(diff < mdiff) {
          mdiff = diff
          mwaypoint = waypoint.seq
        }
      }
      stopToWaypoint += (stop.number -> mwaypoint)
    }
    waypointsBetweenStops(stopToWaypoint)
  }

  def waypointsBetweenStops(stopToWaypoint: Map[String, String]) = {
    var x = ListMap(stopToWaypoint.toSeq.sortBy(_._2.toInt):_*)
    Logger.info("LISTMAP" + x.toString())
    var prevNum: String = null
    var prevWaypoint: String = null
    //Logger.info(x.keys.toString())
    for(y <- x) {
      if(prevNum != null) {
        var tmp: List[(Long, Long)] = List()
        //Logger.info("WA " + prevWaypoint + " " + y._2 + " AT " + prevNum + " " + y._1)
        Waypoint.list.filter(t => t.seq >= prevWaypoint && t.seq < y._2).foreach(t => {
          tmp = tmp :+ (t.lat, t.lon)})
        //Logger.info("TMP " + tmp.toString())
        stopPairsToWaypoints += ((prevNum, y._1) -> tmp)
      }
      prevNum = y._1
      prevWaypoint = y._2
    }
    Logger.info("FINAL" + stopPairsToWaypoints.keys.toString())
  }
}
