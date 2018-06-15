package com.github.atais

import akka.actor.{Actor, ActorSystem, Props, _}
import akka.pattern.ask
import akka.util.Timeout
import com.github.atais.AkkaTest.TickActor._
import com.github.atais.AkkaTest._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class AkkaTest extends FlatSpec with Matchers {

  behavior of "Akka"

  implicit val timeout: Timeout = 1.second

  it should "stream ticks and cache last 10" in {

    val system: ActorSystem = ActorSystem("TicksTest")
    val tickActor = system.actorOf(Props[TickActor], TickActor.name)

    // after short while, the list should start growing
    Thread.sleep(50)
    val check1 = take(tickActor)
    check1.length should be >= 1
    check1.length should be < 9

    // later it should be 10 elems, but different
    Thread.sleep(1000)
    val check2 = take(tickActor)
    check2.length should equal(10)
    check2 should not equal check1

    //    cleanup, not necessary
    system.stop(tickActor)
  }

  private def take(actor: ActorRef): List[Int] = {
    val r = ask(actor, GetList)
    Await.result(r, timeout.duration).asInstanceOf[List[Int]]
  }

}

object AkkaTest {

  object TickActor {
    //@formatter:off
    val name = "TickActor"
    case object GetList
    case object Inc
    //@formatter:on
  }

  class TickActor extends Actor {

    private val tick = context.system.scheduler.schedule(0.millis, 10.millis, self, Inc)

    var list = List.empty[Int]
    var i = 0

    def receive: PartialFunction[Any, Unit] = {
      case Inc =>
        i = i + 1
        list = i :: list.take(9)
      case GetList =>
        sender ! list
      case a =>
        System.err.println("Unhandled message" + a)
    }

    override def postStop(): Unit = tick.cancel()

  }

}
