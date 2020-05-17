package org.twbraam.dbapiserver.db.testDoobie

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import domain._
import fs2.Stream


object c_InsertingUpdating extends App {

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

  val drop =
    sql"""
    DROP TABLE IF EXISTS person
  """.update.run

  val create =
    sql"""
    CREATE TABLE person (
      id   SERIAL,
      name VARCHAR NOT NULL UNIQUE,
      age  SMALLINT
    )
  """.update.run

  (drop, create).mapN(_ + _).transact(xa).unsafeRunSync

  //////////// insert ////////////////

  def insert1(name: String, age: Option[Short]): Update0 =
    sql"insert into person (name, age) values ($name, $age)".update

  insert1("Alice", Some(12)).run.transact(xa).unsafeRunSync

  insert1("Bob", None).quick.unsafeRunSync // switch to YOLO mode

  sql"select id, name, age from person".query[PersonInserting].quick.unsafeRunSync

  //////////// update ////////////////
  sql"update person set age = 15 where name = 'Alice'".update.quick.unsafeRunSync
  sql"select id, name, age from person".query[PersonInserting].quick.unsafeRunSync


  //////////// retrieving inserted id method 1 ////////////////

  def insert2(name: String, age: Option[Short]): ConnectionIO[PersonInserting] =
    for {
      _  <- sql"insert into person (name, age) values ($name, $age)".update.run
      id <- sql"select lastval()".query[Long].unique
      p  <- sql"select id, name, age from person where id = $id".query[PersonInserting].unique
    } yield p

  insert2("Jimmy", Some(42)).quick.unsafeRunSync

  //////////// retrieving inserted id method 2 ////////////////

  def insert2_H2(name: String, age: Option[Short]): ConnectionIO[PersonInserting] =
    for {
      id <- sql"insert into person (name, age) values ($name, $age)"
        .update
        .withUniqueGeneratedKeys[Int]("id")
      p  <- sql"select id, name, age from person where id = $id"
        .query[PersonInserting]
        .unique
    } yield p

  insert2_H2("Ramone", Some(42)).quick.unsafeRunSync

  //////////// retrieving inserted id method 3 (Postgres) ////////////////

  def insert3(name: String, age: Option[Short]): ConnectionIO[PersonInserting] =
    sql"insert into person (name, age) values ($name, $age)"
      .update
      .withUniqueGeneratedKeys("id", "name", "age")

  insert3("Elvis", None).quick.unsafeRunSync

  //////////// batch update ////////////////

  def insertMany(ps: List[PersonInfo]): ConnectionIO[Int] = {
    val sql = "insert into person (name, age) values (?, ?)"
    Update[PersonInfo](sql).updateMany(ps)
  }

  val data = List[PersonInfo](
    ("Frank", Some(12)),
    ("Daddy", None))
  insertMany(data).quick.unsafeRunSync

  //////////// batch update and retrieve id (Postgres) ////////////////

  def insertMany2(ps: List[PersonInfo]): Stream[ConnectionIO, PersonInserting] = {
    val sql = "insert into person (name, age) values (?, ?)"
    Update[PersonInfo](sql).updateManyWithGeneratedKeys[PersonInserting]("id", "name", "age")(ps)
  }

  // Some rows to insert
  val data2 = List[PersonInfo](
    ("Banjo",   Some(39)),
    ("Skeeter", None),
    ("Jim-Bob", Some(12)))

  insertMany2(data2).quick.unsafeRunSync
}
