package com.github.atais

import com.github.atais.MonixTest._
import monix.execution
import monix.execution.Ack
import monix.execution.Scheduler.Implicits.{global => mglobal}
import monix.reactive.Observable
import monix.reactive.observers.Subscriber
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.{global => sglobal}
import scala.concurrent.Future
import scala.concurrent.duration._

class MonixTest extends FlatSpec with Matchers {


  behavior of "Monix"

  it should "stream ticks and cache last 10" in {
    val s = Observable.intervalWithFixedDelay(10.milli)
    val r = new StatefulSubscriber

    // before subscription, the state is empty
    r.lastElems should equal(List.empty)

    val c = s.subscribe(r)

    // after short while, the list should start growing
    Thread.sleep(50)
    val check1 = r.lastElems
    check1.length should be >= 1
    check1.length should be < 9


    // later it should be 10 elems, but different
    Thread.sleep(1000)
    val check2 = r.lastElems
    check2.length should equal(10)
    check2 should not equal check1

    // cleanup
    c.cancel()
    val check3 = r.lastElems
    Thread.sleep(1000)
    // no new elems should come
    check3 shouldEqual r.lastElems
  }
}

object MonixTest {

  class StatefulSubscriber extends Subscriber[Long] {
    var lastElems: List[Long] = List.empty

    override implicit def scheduler: execution.Scheduler = mglobal

    override def onNext(elem: Long): Future[Ack] = Future {
      lastElems = elem :: lastElems.take(9)
      Ack.Continue
    }(sglobal)

    // we could use some logging framework here
    override def onError(ex: Throwable): Unit =
      System.err.println(ex.getMessage, ex)

    override def onComplete(): Unit = Unit
  }

}
