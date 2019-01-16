package ru.mitrakov.self.pwdbreaker.api

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import ru.mitrakov.self.pwdbreaker.api.amqp.AmqpPublisher
import ru.mitrakov.self.pwdbreaker.api.db.{ RequestDao, UserDao }
import ru.mitrakov.self.pwdbreaker.api.models.{ Task, User }
import ru.mitrakov.self.pwdbreaker.api.security.SimplePassAuthenticator

object Starter extends App with FailFastCirceSupport {
  import akka.http.scaladsl.server.Directives._
  import io.circe.generic.auto._
  import io.circe.syntax._

  implicit val actorSystem: ActorSystem = ActorSystem("breaker")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val amqp = new AmqpPublisher()

  val routes: Route = pathSingleSlash {
    get {
      complete("ok")
    }
  } ~ path("users") {
    (post & entity(as[User])) { user =>
      val result = UserDao.persist(user)
      complete(s"OK. $result rows affected")
    } ~ (delete & entity(as[Long])) { userId =>
      val result = UserDao.remove(userId)
      complete(s"OK. $result rows affected")
    }
  } ~ pathPrefix("user" / LongNumber) { userId =>
    path("current") {
      (get & authenticateBasic("realm", SimplePassAuthenticator)) { user =>
        complete("???")
      }
    } ~ path("start") {
      (post & authenticateBasic("realm", SimplePassAuthenticator)) { user =>
        fileUpload("fileUpload") {
          case (fileInfo, fileStream) =>
            val sink = Sink.reduce[ByteString]((a, b) => a ++ b)
            val writeResult = fileStream.runWith(sink)
            onSuccess(writeResult) { content =>
              val newId = RequestDao.persist(user, fileInfo.fileName, content)
              val task = Task(newId)
              val sendResult = amqp.send(ByteString(task.asJson.toString))
              onSuccess(sendResult) { status =>
                complete(s"Successfully written ${content.length} bytes (AMQP status: $status)")
              }
            }
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
