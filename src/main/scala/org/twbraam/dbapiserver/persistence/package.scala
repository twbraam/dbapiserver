package org.twbraam.dbapiserver

import org.twbraam.dbapiserver.domain.User
import zio.{Has, RIO, Task}

package object dbapiserver {

  object Persistence {
    trait Service[A] {
      def get(id: Int): Task[A]
      def create(a: A): Task[A]
      def createTable(): Task[Boolean]
      def delete(id: Int): Task[Boolean]
    }
  }

  type UserPersistence = Has[Persistence.Service[User]]

  def getUser(id: Int): RIO[UserPersistence, User] = RIO.accessM(_.get.get(id))
  def createUser(a: User): RIO[UserPersistence, User] = RIO.accessM(_.get.create(a))
  def deleteUser(id: Int): RIO[UserPersistence, Boolean] = RIO.accessM(_.get.delete(id))
  def createTable(): RIO[UserPersistence, Boolean] = RIO.accessM(_.get.createTable())
}
