package ru.mitrakov.self.pwdbreaker.api.db

import scala.concurrent.ExecutionContext

import cats.effect.{ ContextShift, IO }
import doobie.util.transactor.Transactor

trait Transactors {
  implicit val shift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",                       // driver classname
    "jdbc:postgresql://localhost:5432/pwdbreaker", // connect URL
    "tommy",                                       // user
    "tommy"                                        // password
  )
}
