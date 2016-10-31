package com.example

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import akka.io.Tcp
import com.typesafe.config.ConfigFactory
import java.net.InetSocketAddress
import akka.actor.Terminated
import java.net.InetAddress

private object TcpEchoService {
  def props(endpoint: InetSocketAddress): Props = Props(new TcpEchoService(endpoint))
}

private class TcpEchoService(endpoint: InetSocketAddress) extends Actor with ActorLogging {
  def receive = {
    case Tcp.Connected(remote, local) => {
      log.info(s"Connected from ${remote} to ${local}")
      sender ! Tcp.Register(self) // Register self to handle TCP
    }
    case Tcp.Received(data) => {
      val text = data.utf8String.trim()
      log.info(s"Received ${text}")
      text match {
        case "exit" => {
          context.stop(self)
          context.system.terminate() // This is cause JVM to stop
        }
        case _      => sender ! Tcp.Write(data)
      }
    }
    case Tcp.Bound(local) => log.info(s"Bounded with ${local}")
    case _: Tcp.ConnectionClosed => {
      log.info("Connection closed")
      context.stop(self)
    } // no parameters
    case Terminated => {
      log.info("About to be terminated")
    }
    case any @ _ => log.error(s"${any}")
  }
}

object AkkaTcpSpec {
  protected def name = "AkkaTcpSpec"
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
    val endpoint = new InetSocketAddress(InetAddress.getLocalHost.getHostName, 10116)
    val echoService = system.actorOf(TcpEchoService.props(endpoint), "echoService")
    Thread.sleep(3000)
    IO(Tcp) ! Tcp.Bind(echoService, endpoint)
  }
}

class AkkaTcpSpec {

}