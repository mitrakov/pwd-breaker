package ru.mitrakov.self.pwdbreaker.common

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }
import scala.util.{ Failure, Success }

object Starter extends App {
  implicit val actorSystem: ActorSystem = ActorSystem("breaker")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val routes: Route = path("health") {
    get {
      complete {
        HttpEntity("ok")
      }
    }
  } ~ path("users" / LongNumber / "archive" / Segment) { (userId, archive) =>
    get {
      complete {
        HttpEntity(s"File $archive is not found for user $userId")
      }
    }
  }

  val serverBinding = Http().bindAndHandle(routes, "localhost", 9000)

  serverBinding.onComplete {
    case Success(binding) => println(s"Ready: $binding")
    case Failure(exception) => exception.printStackTrace(); actorSystem.terminate()
  }
  Await.result(actorSystem.whenTerminated, Duration.Inf)
}
