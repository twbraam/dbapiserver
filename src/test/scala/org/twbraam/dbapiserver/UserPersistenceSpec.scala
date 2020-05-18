package org.twbraam.dbapiserver

import org.twbraam.dbapiserver.configuration.DbConfig
import org.twbraam.dbapiserver.persistence.UserPersistenceService
import org.twbraam.dbapiserver.dbapiserver._
import org.twbraam.dbapiserver.domain.User
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment
import zio.{Cause, ZLayer}


object UserPersistenceSpec extends DefaultRunnableSpec {
  val ec = concurrent.ExecutionContext.global

  val dbConfig = ZLayer.succeed(DbConfig(
    "org.postgresql.Driver",
    "jdbc:postgresql://192.168.29.131:30657/awesomedb",
    "amazinguser",
    "perfectpassword"))

  def spec =
    suite("Persistence integration test")(testM("Persistense Live") {
      for {
        _ <- createTable().either
        notFound <- getUser(100).either
        created <- createUser(User(14, "usr")).either
        deleted <- deleteUser(14).either
      } yield
          assert(notFound)(isLeft(anything)) &&
          assert(created)(isRight(equalTo(User(14, "usr")))) &&
          assert(deleted)(isRight(isTrue))
    }).provideSomeLayer[TestEnvironment](
      (dbConfig ++ Blocking.live) >>> UserPersistenceService
        .live(ec)
        .mapError(x => TestFailure.Runtime(Cause.die(x))))

}
