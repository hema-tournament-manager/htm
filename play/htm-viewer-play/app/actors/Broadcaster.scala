package actors

import akka.actor.Actor
import akka.actor.Props
import java.net.InetAddress
import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.net.DatagramSocket
import nl.malienkolders.htm.lib.util.Helpers

class Broadcaster extends Actor {

  val socket = new DatagramSocket(4447)

  def receive = {
    case (name: String, ip: String) =>
      val group = Helpers.getMulticastGroup
      println(name)
      println(ip)
      println(group)
      val buf = (ip.getBytes() ++ ByteBuffer.allocate(4).putInt(9000).array() ++ name.getBytes("UTF-8")) take 256
      val packet = new DatagramPacket(buf, buf.length, group, 4446)
      socket.send(packet)
  }

}