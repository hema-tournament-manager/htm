package nl.malienkolders.htm.admin.lib

import java.io.File
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import java.util.zip.ZipEntry
import nl.malienkolders.htm.lib.model.Participant
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.RenderingHints
import java.io.OutputStream

object PhotoImporterBackend {

  val imageSize = (308, 462)

  def handle(in: ZipInputStream)(handler: (ZipInputStream, ZipEntry) => Any): Unit = in.getNextEntry() match {
    case e: ZipEntry =>
      handler(in, e)
      handle(in)(handler)
    case _ => in.close()
  }

  def cropImage(image: BufferedImage, targetSize: (Int, Int)): BufferedImage = {
    val (imageWidth, imageHeight) = targetSize
    val ratio = imageWidth.toDouble / imageHeight.toDouble
    val scaledWidth = (image.getHeight() * ratio).toInt
    val cropped = new BufferedImage(imageWidth, imageHeight, image.getType())
    val offsetX = (image.getWidth() - scaledWidth) / 2
    val g = cropped.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.drawImage(image, 0, 0, imageWidth, imageHeight, offsetX, 0, offsetX + scaledWidth, image.getHeight(), null)
    g.dispose()
    cropped
  }

  def doImport(file: Array[Byte], streamForImage: Int => OutputStream) = {

    val stream = new ByteArrayInputStream(file);
    val rootzip = new ZipInputStream(stream)

    import collection.JavaConverters._

    var i = 0
    handle(rootzip) { (in, entry) =>
      val image = ImageIO.read(in)

      val out = streamForImage(i)
      ImageIO.write(cropImage(image, imageSize), "jpg", out)
      out.close()

      i += 1
    }
  }
}