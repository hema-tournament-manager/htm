package actors

import akka.actor.Actor
import akka.actor.Props
import java.net.InetAddress
import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.net.DatagramSocket
import nl.malienkolders.htm.lib.util.Helpers
import play.Logger

class Broadcaster extends Actor {

  val logger = Logger.of(classOf[Broadcaster])

  val socket = new DatagramSocket()

  def receive = {
    case (name: String, ip: String) =>
      val group = Helpers.getMulticastGroup
      logger.debug("Broadcasting: %s:9000 (%s)" format (ip, name))
      val buf = (InetAddress.getByName(ip).getAddress() ++ ByteBuffer.allocate(4).putInt(9000).array() ++ name.getBytes("UTF-8")) take 256
      val packet = new DatagramPacket(buf, buf.length, group, 4446)
      socket.send(packet)
  }

}