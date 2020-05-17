package org.twbraam.dbapiserver.db.testDoobie

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import domain._
import doobie.postgres._


object e_ExceptionHandlingLogging extends App {

  //////////////////////////// Setup //////////////////////////////////

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://192.168.29.131:30657/awesomedb",
    "amazinguser",
    "perfectpassword",
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  val y = xa.yolo
  import y._

  //////////// example setup ////////////////

  List(
    sql"""DROP TABLE IF EXISTS person""",
    sql"""CREATE TABLE person (
          id    SERIAL,
          name  VARCHAR NOT NULL UNIQUE
        )"""
  ).traverse(_.update.quick).void.unsafeRunSync

  def insert(s: String): ConnectionIO[PersonException] = {
    sql"insert into person (name) values ($s)"
      .update
      .withUniqueGeneratedKeys("id", "name")
  }

  insert("bob").quick.unsafeRunSync
  try {
    insert("bob").quick.unsafeRunSync
  } catch {
    case e: java.sql.SQLException =>
      println(e.getMessage)
      println(e.getSQLState)
  }
  // second insert fails with:
  // ERROR: duplicate key value violates unique constraint "person_name_key"
  //   Detail: Key (name)=(bob) already exists.
  // 23505

  def safeInsert(s: String): ConnectionIO[Either[String, PersonException]] =
    insert(s).attemptSomeSqlState {
      case sqlstate.class23.UNIQUE_VIOLATION => "Oops!"
    }

  safeInsert("bob").quick.unsafeRunSync
  //   Left(Oops!)

  safeInsert("steve").quick.unsafeRunSync
  //   Right(Person(4,steve))


  //////////////////////////// Logging //////////////////////////////////

  def byName(pat: String) = {
    sql"select name, code from country where name like $pat"
      .queryWithLogHandler[(String, String)](LogHandler.jdkLogHandler)
      .to[List]
      .transact(xa)
  }

  byName("U%").unsafeRunSync

  /////////////////// using implicit loghandler //////////////////////////
  implicit val han: LogHandler = MyLogger.logHandler

  def byName2(pat: String) = {
    sql"select name, code from country where name like $pat"
      .query[(String, String)] // handler will be picked up here
      .to[List]
      .transact(xa)
  }
  byName2("U%").unsafeRunSync

}
