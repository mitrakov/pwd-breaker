package ru.mitrakov.self.pwdbreaker.api

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._

import ru.mitrakov.self.pwdbreaker.api.db.UserDao
import ru.mitrakov.self.pwdbreaker.api.models.User

object Starter extends App with FailFastCirceSupport {
  implicit val actorSystem: ActorSystem = ActorSystem("breaker")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val routes: Route = pathSingleSlash {
    get {
      complete {
        HttpEntity("ok")
      }
    }
  } ~ path("users") {
    post {
      entity(as[User]) { user =>
        val result = UserDao.persist(user)
        complete {
          HttpEntity(s"OK. $result rows affected")
        }
      }
    } ~ delete {
      entity(as[Long]) { userId =>
        UserDao.remove(userId)
        complete {
          HttpEntity("OK")
        }
      }
    }
  }

  // Run Server
  Http().bindAndHandle(routes, "localhost", 9000).onComplete {
    case Success(binding) => println(s"Ready: $binding")
    case Failure(exception) => exception.printStackTrace(); actorSystem.terminate()
  }
  Await.result(actorSystem.whenTerminated, Duration.Inf)
}
