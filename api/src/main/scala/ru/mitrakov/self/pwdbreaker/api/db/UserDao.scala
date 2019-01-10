package ru.mitrakov.self.pwdbreaker.api.db

import doobie.implicits._
import ru.mitrakov.self.pwdbreaker.api.models.User

object UserDao extends Transactors {
  def persist(user: User): Int = {
    val insert = sql"""INSERT INTO "user" (name, password) VALUES (${user.name}, ${user.password})""".update
    insert.run.transact(xa).unsafeRunSync()
  }

  def remove(userId: Long): Long = ???
}
