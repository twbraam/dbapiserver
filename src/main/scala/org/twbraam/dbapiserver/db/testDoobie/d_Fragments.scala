package org.twbraam.dbapiserver.db.testDoobie

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import fs2.Stream
import org.twbraam.dbapiserver.db.testDoobie.domain._


object d_Fragments extends App {

  //////////////////////////// Setup //////////////////////////////////

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

  //////////// batch update and retrieve id (Postgres) ////////////////

  val a = fr"select name from country"
  val b = fr"where code = 'USA'"
  val c = a ++ b
  c.query[String].unique.quick.unsafeRunSync

  ///////////////////

  def whereCode(s: String) = fr"where code = $s"
  val whereFRA = whereCode("FRA")
  (fr"select name from country" ++ whereFRA).query[String].quick.unsafeRunSync

  ///////////////////

  def count(table: String) = (fr"select count(*) from" ++ Fragment.const(table)).query[Int].unique
  count("city").quick.unsafeRunSync

}
