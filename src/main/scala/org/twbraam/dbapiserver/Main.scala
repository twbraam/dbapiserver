package org.twbraam.dbapiserver

import cats.effect.ExitCode
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.twbraam.dbapiserver.configuration.Configuration
import org.twbraam.dbapiserver.dbapiserver.UserPersistence
import org.twbraam.dbapiserver.http.Api
import org.twbraam.dbapiserver.persistence.UserPersistenceService
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.putStrLn
import zio.interop.catz._

import scala.concurrent.ExecutionContext.global

object Main extends App {

  type AppEnvironment = Configuration with Clock with UserPersistence

  type AppTask[A] = RIO[AppEnvironment, A]

  val userPersistence = (Configuration.live ++ Blocking.live) >>> UserPersistenceService
    .live(platform.executor.asEC)

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val program: ZIO[AppEnvironment, Throwable, Unit] =
     for {
        api <- configuration.apiConfig
        httpApp = Router[AppTask](
          "/users" -> Api(s"${api.endpoint}/users").route
        ).orNotFound

        server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
          BlazeServerBuilder[AppTask](global)
            .bindHttp(api.port, api.endpoint)
            .withHttpApp(CORS(httpApp))
            .serve
            .compile[AppTask, AppTask, ExitCode]
            .drain
        }
      } yield server

    program.provideSomeLayer[ZEnv](Configuration.live ++ userPersistence)foldM(
      err => putStrLn(s"Execution failed with: $err") *> IO.succeed(1),
      _ => IO.succeed(0)
    )
  }
}
