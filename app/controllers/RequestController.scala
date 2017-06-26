package controllers

import javax.inject._

import data_structures.{LightTram, Tram}
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
    val listMoving: List[LightTram] = Tram.currentList
      .filter(tram => {tram.isDeleted.isEmpty})
      .map(tram => LightTram(tram))
    val listDeleted: List[Int] = Tram.currentList
        .filter(tram => {tram.isDeleted.isDefined})
        .map(tram => (tram.id takeRight 5).toInt)

    implicit val lightTramWrites: Writes[LightTram] = Json.writes[LightTram]

    val json = Json.toJson(listMoving)
    val json2 = Json.toJson(listDeleted)
    //bardzo profesjonalne rozwiązanie
    Logger.info(s"JSON ${json2}")
    Ok("{\"trams\":" + json + ", \"deleted\":" + json2 + "}")
  }
}

