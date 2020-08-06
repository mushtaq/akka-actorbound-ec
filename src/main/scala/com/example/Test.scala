package com.example

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.util.Timeout

import scala.concurrent.duration.DurationDouble
import scala.concurrent.{Await, Future}

object Test {
  import ExampleUsage._
  implicit val timeout: Timeout = Timeout(5.seconds)

  implicit class Blocker[T](f: Future[T]) {
    def await: T = Await.result(f, timeout.duration)
  }

  def main(args: Array[String]): Unit = {
    val demo = ActorSystem(exampleBehaviour, "demo")
    import demo.executionContext
    implicit val scheduler: Scheduler = demo.scheduler

    def test1(): Unit = {
      demo ! CreateChild
    }

    def test2(): Unit = {
      // concurrent increments
      Future.traverse(1 to 100000)(_ => demo.ask(Increment)).await
      val total = demo.ask(GetTotal)(timeout, demo.scheduler).await

      // should be 100000, no lost update
      println(total)
      demo.terminate()
      Await.result(demo.whenTerminated, 5.seconds)
    }

    test1()
//    test2()
  }

}
