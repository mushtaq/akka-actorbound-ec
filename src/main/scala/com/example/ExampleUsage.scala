package com.example

import akka.Done
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.Future

object ExampleUsage {
  trait Msg
  case class Increment(requester: ActorRef[Done]) extends Msg
  case class GetTotal(requester: ActorRef[Int])   extends Msg

  val exampleBehaviour: Behavior[Msg] = BehaviourExtensions.withActorBoundEc { implicit actorBoundEc =>
    var total = 0

    Behaviors.receiveMessage {
      case Increment(requester) =>
        // this will use the actorBoundEc
        // actorBoundEc will send the callback as a 'Runnable' message to ctx.self
        // runnableInterceptor will handle that message sequentially with other actor messages
        Future {
          total += 1
          requester ! Done
        }
        Behaviors.same
      case GetTotal(requester) =>
        requester ! total
        Behaviors.same
    }

  }
}
