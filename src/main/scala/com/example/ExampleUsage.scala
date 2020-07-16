package com.example

import akka.Done
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.control.NonFatal

object ExampleUsage {
  trait Msg
  case class Increment(requester: ActorRef[Done]) extends Msg
  case class GetTotal(requester: ActorRef[Int])   extends Msg
  case object CreateChild                         extends Msg

  val exampleBehaviour: Behavior[Msg] = {
    Behaviors.setup { ctx =>
      BehaviourExtensions.withActorBoundEc { actorBoundEc =>
        ctx.self.unsafeUpcast
        implicit val ec: ExecutionContextExecutor = actorBoundEc

        var total = 0

        Behaviors.receiveMessage {
          case CreateChild =>
            Future {
              ctx.spawnAnonymous(Behaviors.empty)
              println("*************** success!")
            }.recover {
              case NonFatal(ex) => ex.printStackTrace()
            }
            Behaviors.same

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
  }

}
