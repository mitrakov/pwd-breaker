package ru.mitrakov.self.pwdbreaker.api

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import ru.mitrakov.self.pwdbreaker.api.db.{ RequestDao, UserDao }
import ru.mitrakov.self.pwdbreaker.api.models.User
import ru.mitrakov.self.pwdbreaker.api.security.SimplePassAuthenticator

object Starter extends App with FailFastCirceSupport {
  implicit val actorSystem: ActorSystem = ActorSystem("breaker")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = actorSystem.dispatcher

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
      (get & authenticateBasic("realm", SimplePassAuthenticator))  { user =>
        complete("???")
      }
    } ~ path("start") {
      (post & authenticateBasic("realm", SimplePassAuthenticator)) { user =>
        fileUpload("fileUpload") {
          case (fileInfo, fileStream) =>
            val sink = Sink.reduce[ByteString]((a, b) => a ++ b)
            val writeResult = fileStream.runWith(sink)
            onSuccess(writeResult) { content =>
              if (RequestDao.persist(user, fileInfo.fileName, content) == 1) {
                complete(s"Successfully written ${content.length} bytes")
              } else complete((InternalServerError, s"Error occurred"))
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
