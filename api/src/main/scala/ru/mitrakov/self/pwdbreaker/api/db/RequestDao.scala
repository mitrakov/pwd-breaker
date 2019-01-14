package ru.mitrakov.self.pwdbreaker.api.db

import akka.util.ByteString
import doobie.implicits._

import ru.mitrakov.self.pwdbreaker.api.models.User

object RequestDao extends Transactors {
  def persist(user: User, filename: String, content: ByteString): Int = {
    val insert = sql"""INSERT INTO request (user_id, filename, "data") VALUES (${user.id}, $filename, ${content.toArray})""".update
    insert.run.transact(xa).unsafeRunSync()
  }
}
