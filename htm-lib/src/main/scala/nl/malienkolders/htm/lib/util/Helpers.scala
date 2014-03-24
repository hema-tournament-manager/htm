package nl.malienkolders.htm.lib.util

import scala.sys.SystemProperties
import java.awt.Desktop
import java.net.URI
import java.net.NetworkInterface
import java.net.InetAddress
import java.util.Hashtable
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.BarcodeFormat
import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.Color
import javax.imageio.ImageIO
import java.io.ByteArrayOutputStream

object Helpers {
  implicit class PimpedInt(val i: Int) extends AnyVal {
    def isEven = i % 2 == 0
    def isOdd = !isEven
  }

  def openUrlFromSystemProperty(property: String): Unit = {
    new SystemProperties().get(property).foreach { url =>
      val desktop = if (Desktop.isDesktopSupported()) Some(Desktop.getDesktop()) else None
      desktop foreach {
        case dt if dt.isSupported(Desktop.Action.BROWSE) =>
          dt.browse(URI.create(url))
      }
    }
  }

  def getIpAddress: Option[String] = {
    import scala.collection.JavaConversions._

    val localNetworkPrefixes = Set("192.168.", "10.", "172.16.", "172.31.")
    val addresses = for {
      interface <- NetworkInterface.getNetworkInterfaces()
      address <- interface.getInetAddresses()
    } yield address.toString.substring(1)

    addresses.find(address => localNetworkPrefixes.exists(prefix => address.startsWith(prefix)))
  }

  def getMulticastGroup: InetAddress = InetAddress.getByName("224.0.0.1")

  def generateQrImage: Array[Byte] = {
    val hintMap = new Hashtable[EncodeHintType, Any]();
    hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
    val qrCodeWriter = new QRCodeWriter();
    getIpAddress match {
      case Some(ip) =>
        val byteMatrix = qrCodeWriter.encode(s"http://$ip:8080/participants/list",
          BarcodeFormat.QR_CODE, 512, 512, hintMap);
        // Make the BufferedImage that are to hold the QRCode
        val matrixWidth = byteMatrix.getWidth();
        val image = new BufferedImage(matrixWidth, matrixWidth,
          BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        val graphics = image.getGraphics().asInstanceOf[Graphics2D];
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // Paint and save the image using the ByteMatrix
        graphics.setColor(Color.BLACK);

        for (i <- 0 to (matrixWidth - 1)) {
          for (j <- 0 to (matrixWidth - 1)) {
            if (byteMatrix.get(i, j)) {
              graphics.fillRect(i, j, 1, 1);
            }
          }
        }
        val mem = new ByteArrayOutputStream
        ImageIO.write(image, "png", mem)
        mem.toByteArray()
      case _ =>
        Array()
    }

  }
}