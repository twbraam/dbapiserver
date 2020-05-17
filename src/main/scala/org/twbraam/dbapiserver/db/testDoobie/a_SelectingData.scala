package org.twbraam.dbapiserver.db.testDoobie

import cats.effect._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import fs2.Stream
import org.twbraam.dbapiserver.db.testDoobie.domain.Country




object a_SelectingData extends App {

  //////////////////////////// Setup //////////////////////////////////

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://192.168.29.131:30657/awesomedb",
    "amazinguser",
    "perfectpassword",
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  sql"select name from country"
    .query[String]    // Query0[String]
    .stream           // Stream[ConnectionIO, String]
    .take(5)          // Stream[ConnectionIO, String]
    .compile.toList   // ConnectionIO[List[String]]
    .transact(xa)     // IO[List[String]]
    .unsafeRunSync    // List[String]
    .foreach(println) // Unit

  val y = xa.yolo // a stable reference is required
  import y._

  sql"select code, name, population, gnp from country"
    .query[Country] // Query0[String]
    .stream        // Stream[ConnectionIO, String]
    .take(5)       // Stream[ConnectionIO, String]
    .quick         // IO[Unit]
    .unsafeRunSync


  val p: Stream[IO, Country] = {
    sql"select code, name, population, gnp from country"
      .query[Country] // Query0[Country2]
      .stream          // Stream[ConnectionIO, Country2]
      .transact(xa)    // Stream[IO, Country2]
  }


}
