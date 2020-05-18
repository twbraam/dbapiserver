package org.twbraam.dbapiserver

import pureconfig.{ConfigSource, loadConfigOrThrow}
import zio._

package object configuration {

  type Configuration = Has[ApiConfig] with Has[DbConfig]

  case class AppConfig(api: ApiConfig, dbConfig: DbConfig)
  case class ApiConfig(endpoint: String, port: Int)
  case class DbConfig(
      driver: String,
      url: String,
      user: String,
      password: String
  )

  val apiConfig: ZIO[Has[ApiConfig], Throwable, ApiConfig] = ZIO.access(_.get)
  val dbConfig: ZIO[Has[DbConfig], Throwable, DbConfig] = ZIO.access(_.get)

  object Configuration {
    import pureconfig.generic.auto._
    val live: Layer[Throwable, Configuration] = ZLayer.fromEffectMany(
      Task
        .effect(ConfigSource.default.loadOrThrow[AppConfig])
        .map(c => Has(c.api) ++ Has(c.dbConfig)))
  }
}
