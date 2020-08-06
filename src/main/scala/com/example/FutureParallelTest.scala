package com.example

import akka.actor.typed.scaladsl.adapter.TypedSchedulerOps
import akka.actor.typed.{ActorSystem, Scheduler}
import com.example.ExampleUsage.exampleBehaviour

import scala.concurrent._
import duration.Duration.Inf
import akka.pattern.after

import scala.concurrent.duration.DurationLong

object FutureParallelTest {
  val demo: ActorSystem[ExampleUsage.Msg] = ActorSystem(exampleBehaviour, "demo")
  import demo.executionContext
  val scheduler: Scheduler = demo.scheduler

  def slow(key: String): Future[String] = {

    Future {
      println(s"$key start")
//      after(1.seconds, scheduler.toClassic) {
        Future {
          Thread.sleep(2000)
          println(s"$key end")
          key
        }
//      }
    }
  }.flatten

  def runAsyncSerial(): Future[Seq[String]] = {
    slow("A").flatMap { a =>
      Future.sequence(Seq(slow("B"), slow("C"), slow("D")))
    }
  }

  def main(args: Array[String]): Unit = {
    Await.result(runAsyncSerial(), Inf)
  }

}
