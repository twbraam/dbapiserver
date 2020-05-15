package org.twbraam.dbapiserver.db.testDoobie

import cats.effect._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import fs2.Stream
import org.twbraam.dbapiserver.db.testDoobie.domain.Country


object b_Parameterized extends App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://192.168.39.11:32207/awesomedb",
    "amazinguser",
    "perfectpassword",
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  val y = xa.yolo
  import y._

  def biggerThan(minPop: Int) = sql"""
  select code, name, population, gnp
  from country
  where population > $minPop
""".query[Country]

  //biggerThan(150000000).quick.unsafeRunSync

  def populationIn(range: Range) = sql"""
  select code, name, population, gnp
  from country
  where population > ${range.min}
  and   population < ${range.max}
""".query[Country]

  //populationIn(150000000 to 200000000).quick.unsafeRunSync

  biggerThan(0).check.unsafeRunSync
}
