package data_structures

case class LightTram(id: Int, name: String, lat: Double, lon:Double)

object LightTram{
  def apply(tram: Tram): LightTram = {
    new LightTram(
      (tram.id takeRight 5).toInt,
      tram.name.get,
      tram.latitude.get.toDouble / 3600000,
      tram.longitude.get.toDouble / 3600000
    )
  }
}
