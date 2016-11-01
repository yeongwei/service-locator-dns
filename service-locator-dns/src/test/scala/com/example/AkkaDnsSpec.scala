package com.example

import akka.pattern.ask
import akka.pattern.pipe
import akka.io.IO
import akka.io.Dns
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.testkit.TestKit
import akka.actor.ActorRef
import com.typesafe.config.ConfigFactory
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSpecLike
import scala.concurrent.Await
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object AkkaDnSpec {
  def name = "AkkaDnSpec"
  def configuration = ConfigFactory.load(
    ConfigFactory.parseString(
      """
        |  akka {
        |    loglevel = "INFO"
        |    # log-config-on-start = "on"
        |    remote.netty.tcp.port = 0
        |    remote.netty.tcp.bind-port = 0
        |  }
        """.stripMargin)).withFallback(ConfigFactory.load())
        
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(name, configuration)
    implicit val timeout = new Timeout(10, TimeUnit.SECONDS)
    val future = IO(Dns)
      .ask(Dns.Resolve("tnpmklig03.persistent.co.in")).mapTo[Dns.Resolved]
    val result = Await.result(future, timeout.duration)
    println(s"${result}")
    system.terminate()
  }
}

class AkkaDnsSpec extends TestKit(ActorSystem(AkkaDnSpec.name, AkkaDnSpec.configuration)) 
  with FunSpecLike with BeforeAndAfterAll {
  
  private var dns: ActorRef = _
  private implicit val timeout = new Timeout(10, TimeUnit.SECONDS)
  
  override def beforeAll = {
    dns = IO(Dns)
  }
  override def afterAll = {
    system.stop(dns)
    system.terminate()
  }
  
  describe("DNS") {
    it("should resolve some well known hostname") {
      val result = IO(Dns).ask(Dns.Resolve("ns1.telstra.net")).mapTo[Dns.Resolved] // Ensure a return type
      Try(Await.result(result, timeout.duration)) match {
        case Success(r) => {
          info(s"${r}")
          assert(r.ipv4.size > 0)
          assert(r.ipv4.forall { inet => inet.getHostAddress.equals("139.130.4.5") })
        }
        case Failure(r) => assert(false)
      }
    }
  }
}