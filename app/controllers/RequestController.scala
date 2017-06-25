package controllers

import javax.inject._

import play.api.mvc._

@Singleton
class RequestController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  var x = 0

  def index() = Action {
    x += 1
    implicit request: Request[AnyContent] =>
    if (x % 2 == 0)
      Ok("{\"trams\":[" +
      "{\"id\":0, \"name\": \"4 Wzgórza Krzesławickie\", \"lat\": 50.08186, \"lon\": 19.8820}," +
      "{\"id\":1, \"name\": \"22 Walcownia\", \"lat\": 50.0788, \"lon\": 19.9138}," +
      "{\"id\":2, \"name\": \"13 Nowy Bieżanów\", \"lat\": 50.0317, \"lon\": 19.9340}," +
      "{\"id\":3, \"name\": \"14 Bronowice\", \"lat\": 50.0950, \"lon\": 19.9962}" +
      "]}")
    else
      Ok("{\"trams\":[" +
        "{\"id\":0, \"name\": \"4 Wzgórza Krzesławickie\", \"lat\": 50.03186, \"lon\": 19.8823}," +
        "{\"id\":1, \"name\": \"22 Walcownia\", \"lat\": 50.0780, \"lon\": 19.915}," +
        "{\"id\":2, \"name\": \"13 Nowy Bieżanów\", \"lat\": 50.0315, \"lon\": 19.9338}," +
        "{\"id\":3, \"isDeleted\": \"true\"}" +
        "]}")
  }
}
