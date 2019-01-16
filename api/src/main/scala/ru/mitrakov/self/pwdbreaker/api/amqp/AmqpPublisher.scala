package ru.mitrakov.self.pwdbreaker.api.amqp

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, ClosedShape }
import akka.stream.alpakka.amqp.{ AmqpDetailsConnectionProvider, AmqpSinkSettings, QueueDeclaration }
import akka.stream.alpakka.amqp.scaladsl._
import akka.stream.scaladsl.{ GraphDSL, RunnableGraph, Sink, Source }
import akka.util.ByteString

import scala.concurrent.Future

class AmqpPublisher(implicit actorSystem: ActorSystem, materializer: ActorMaterializer) {

  private val queueName = "pwd-breaker-tasks-queue"
  private val provider = AmqpDetailsConnectionProvider("localhost", 5672)
  private val settings = AmqpSinkSettings(provider)
    .withRoutingKey(queueName) // routing key = queue name for Default Exchange bindings
    .withDeclaration(QueueDeclaration(queueName))

  protected val amqpSink: Sink[ByteString, Future[Done]] = AmqpSink.simple(settings)

  def send(message: ByteString): Future[Done] = {
    import GraphDSL.Implicits._
    RunnableGraph.fromGraph(GraphDSL.create(amqpSink) { implicit b => sink =>
      val source = Source.single(message)
      source ~> sink
      ClosedShape
    }).run()
  }
}
