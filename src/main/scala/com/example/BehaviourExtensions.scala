package com.example

import akka.actor.typed.BehaviorInterceptor.ReceiveTarget
import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

object BehaviourExtensions {

  def withActorBoundEc[T: ClassTag](factory: ExecutionContext => Behavior[T]): Behavior[T] = {
    Behaviors
      .setup[Any] { ctx =>
        Behaviors.intercept[Any, T](() => runnableInterceptor[T]) {
          factory(actorBoundEc(ctx.self))
        }
      }
      .narrow
  }

  private def runnableInterceptor[T: ClassTag]: BehaviorInterceptor[Any, T] = {
    new BehaviorInterceptor[Any, T] {
      override def aroundReceive(ctx: TypedActorContext[Any], msg: Any, target: ReceiveTarget[T]): Behavior[T] =
        msg match {
          case x: Runnable => x.run(); Behaviors.same
          case x: T        => target(ctx, x)
          case _           => Behaviors.unhandled
        }
    }
  }

  private def actorBoundEc(actorRef: ActorRef[Runnable]): ExecutionContext = {
    new ExecutionContext {
      override def execute(runnable: Runnable): Unit     = actorRef ! runnable
      override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
    }
  }

}
