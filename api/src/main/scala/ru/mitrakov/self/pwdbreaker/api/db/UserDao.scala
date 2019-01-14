package ru.mitrakov.self.pwdbreaker.api.db

import doobie.implicits._
import ru.mitrakov.self.pwdbreaker.api.models.User

object UserDao extends Transactors {
  def find(userId: Long): Option[User] = {
    val select = sql"""SELECT * FROM "user" WHERE user_id = $userId""".query[User]
    select.option.transact(xa).unsafeRunSync()
  }

  def find(name: String): Option[User] = {
    val select = sql"""SELECT * FROM "user" WHERE name = $name""".query[User]
    select.option.transact(xa).unsafeRunSync()
  }

  def persist(user: User) = {
    val insert = sql"""INSERT INTO "user" (name, "password") VALUES (${user.name}, ${user.password})""".update //.withUniqueGeneratedKeys("user_id")
    insert.run.transact(xa).unsafeRunSync()
  }

  def remove(userId: Long): Int = {
    val delete = sql"""DELETE FROM "user" WHERE user_id = $userId""".update
    delete.run.transact(xa).unsafeRunSync()
  }
}
