package com.example

import akka.pattern.ask
import akka.pattern.pipe
import akka.io.IO
import akka.io.Dns
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Await
import akka.util.Timeout

object AkkaDnSpec {
  protected def name = "AkkaDnSpec"
  protected def configuration = ConfigFactory.load(
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

class AkkaDnsSpec {
  
}