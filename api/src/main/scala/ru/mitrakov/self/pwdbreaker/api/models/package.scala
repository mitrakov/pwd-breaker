package ru.mitrakov.self.pwdbreaker.api

package object models {
  case class User(id: Option[Long], name: String, password: String, createdAt: Option[String])
}
