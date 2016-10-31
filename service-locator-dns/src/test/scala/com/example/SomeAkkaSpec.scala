package com.example

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSpec
import akka.pattern.ask
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

class SomeAkkaSpec extends FunSpec with BeforeAndAfterAll {
  implicit val system = ActorSystem("SomeAkkaSpec", ConfigFactory.load())
  
  private object DummyActor {
    case class Ask(msg: String)
    case class Tell(msg: String)
    
    def props(probe: ActorRef) = Props(new DummyActor(probe))  
  }
  
  private class DummyActor(probe: ActorRef) extends Actor {
    import context.dispatcher
    
    def receive = {
      // In return sends to probe
      case DummyActor.Ask(msg) => self.ask(msg)(new FiniteDuration(10, TimeUnit.SECONDS))
      case DummyActor.Tell(msg) =>  self ! msg
      case any @ _ => probe ! any
    }
  }

  describe("Testprobe") {
    it("should update sender on every dequeued message") {
      val testProbe = TestProbe()
      val dummyActor1 = system.actorOf(DummyActor.props(testProbe.ref), "dummyActor1")
      val dummyActor2 = system.actorOf(DummyActor.props(testProbe.ref), "dummyActor2")
      Thread.sleep(3000)
      
      dummyActor1 ! DummyActor.Ask("hello")
      testProbe.expectMsg("hello")
      assert(testProbe.sender().path.equals(dummyActor1.path))
      
      dummyActor2 ! DummyActor.Tell("hey")
      testProbe.expectMsg("hey")
      assert(testProbe.sender().path.equals(dummyActor2.path))
    }
  }
}