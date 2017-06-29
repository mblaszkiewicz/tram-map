import com.google.inject.AbstractModule
import services.{TramProvider, WaypointProvider}
import services.{ScheduleProvider, StopsProvider, TramProvider, TripProvider}
//zostawiłam opis jakby ktoś chciał przypdakiem usunąć
/**
  * This class is a Guice module that tells Guice how to bind several
  * different types. This Guice module is created when the Play
  * application starts.

  * Play will automatically use any class called `Module` that is in
  * the root package. You can create modules in other locations by
  * adding `play.modules.enabled` settings to the `application.conf`
  * configuration file.
  */

class Module extends AbstractModule {

  override def configure() = {
    // Ask Guice to create an instance of TramProvider when the
    // application starts.
    bind(classOf[TramProvider]).asEagerSingleton()
    bind(classOf[WaypointProvider]).asEagerSingleton()
    bind(classOf[StopsProvider]).asEagerSingleton()
    bind(classOf[TripProvider]).asEagerSingleton()
    bind(classOf[ScheduleProvider]).asEagerSingleton()
  }

}
