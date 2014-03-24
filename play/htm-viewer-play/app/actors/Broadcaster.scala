package actors

import akka.actor.Actor
import akka.actor.Props
import java.net.InetAddress
import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.net.DatagramSocket
import nl.malienkolders.htm.lib.util.Helpers
import play.Logger
import scala.util.Properties

class Broadcaster extends Actor {

  val logger = Logger.of(classOf[Broadcaster])

  val socket = new DatagramSocket()

  def receive = {
    case (name: String, ip: String) =>
      val group = Helpers.getMulticastGroup
      val port = Properties.propOrElse("http.port", "9000").toInt
      logger.debug("Broadcasting: %s:%d (%s)" format (ip, port, name))
      val buf = (InetAddress.getByName(ip).getAddress() ++ ByteBuffer.allocate(4).putInt(port).array() ++ name.getBytes("UTF-8")) take 256
      val packet = new DatagramPacket(buf, buf.length, group, 4446)
      socket.send(packet)
  }

}