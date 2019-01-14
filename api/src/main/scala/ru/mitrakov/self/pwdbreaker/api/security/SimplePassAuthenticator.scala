package ru.mitrakov.self.pwdbreaker.api.security

import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials._
import akka.http.scaladsl.server.Directives._

import ru.mitrakov.self.pwdbreaker.api.db.UserDao
import ru.mitrakov.self.pwdbreaker.api.models.User

object SimplePassAuthenticator extends Authenticator[User] {

  override def apply(v1: Credentials): Option[User] = {
    v1 match {
      case p @ Provided(userName) => UserDao.find(userName).filter(user => p.verify(user.password))
      case Missing => None
    }
  }
}
