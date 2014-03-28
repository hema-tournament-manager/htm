package nl.malienkolders.htm.admin.worker

import nl.malienkolders.htm.lib.util.Helpers
import java.net.MulticastSocket
import java.net.DatagramPacket
import java.net.InetAddress
import java.nio.ByteBuffer
import nl.malienkolders.htm.lib.model.Viewer
import net.liftweb.mapper.By
import net.liftweb.common.Loggable

object BroadcastListener extends Loggable {

  val socket = new MulticastSocket(4446)

  def run: Unit = {
    val group = Helpers.getMulticastGroup
    logger.info("JOINING the multicast group " + group.toString())
    socket.joinGroup(group)
    logger.info("JOINED the multicast group")

    while (true) {
      val buffer = new Array[Byte](256)
      val packet = new DatagramPacket(buffer, buffer.length)
      socket.receive(packet)
      val ip = InetAddress.getByAddress(buffer.take(4))
      val port = ByteBuffer.wrap(buffer.drop(4).take(4)).getInt()
      val name = new String(buffer.drop(8), "UTF-8").trim()
      val url = ip.toString().drop(1) + ":" + port

      logger.debug("RECEIVED: %s (%s)" format (url, name))

      val viewer = Viewer.find(By(Viewer.url, url)).map(viewer =>
        viewer.alias(name)).openOr(
        Viewer.create.alias(name).url(url))
      viewer.save
    }
  }

}