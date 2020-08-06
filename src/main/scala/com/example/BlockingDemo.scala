package com.example

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object BlockingDemo {

  def setup(count: Int): Behavior[Nothing] =
    Behaviors.setup[Nothing] { ctx =>
      (1 to count).foreach { n =>
        ctx.spawnAnonymous(behavior(n)) ! "start"
      }
      Behaviors.same
    }

  def behavior(x: Int): Behavior[String] = {
    Behaviors.receiveMessage[String] {
      case "start" =>
        concurrent.blocking {
          Thread.sleep(2000)
          println(s"actor $x on thread: ${Thread.currentThread().getName}")
        }
        Behaviors.stopped
    }
  }

  def main(args: Array[String]): Unit = {
    ActorSystem[Nothing](setup(50), "demo")
  }

}
