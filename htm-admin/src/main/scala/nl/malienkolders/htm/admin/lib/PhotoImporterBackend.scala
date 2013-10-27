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
import org.apache.commons.io.IOUtils
import java.io.FileOutputStream
import java.io.FileInputStream

object PhotoImporterBackend {

  val imageSize = (308, 462)

  def handle(in: ZipInputStream, baseNames: Iterator[String])(handler: (String, ZipInputStream, ZipEntry) => Unit): Unit = in.getNextEntry() match {
    case e: ZipEntry =>
      handler(baseNames.next, in, e)
      handle(in, baseNames)(handler)
    case _ => in.close()
  }

  def handlePhoto(baseName: String, in: ZipInputStream, entry: ZipEntry): Unit = {
    println("baseName: " + baseName)
    println("entry: " + entry.getName())

    val targetDir = new File("Avatars")
    targetDir.mkdir()

    val image = ImageIO.read(in)
    ImageIO.write(image, "jpg", new File(targetDir, baseName + ".jpg"))
    
    val generatedDir = new File(targetDir, "Generated")
    generatedDir.mkdir()

    val outFileLeft = new File(generatedDir, baseName + "_default_l.jpg")
    val outLeft = new FileOutputStream(outFileLeft)
    ImageIO.write(cropImage(image, imageSize), "jpg", outFileLeft)
    outLeft.close()

    val outFileRight = new File(generatedDir, baseName + "_default_r.jpg")
    val outRight = new FileOutputStream(outFileRight)
    ImageIO.write(cropImage(image, imageSize, Some(Mirror)), "jpg", outFileRight)
    outRight.close()
  }

  abstract class CropOption
  case object Mirror extends CropOption

  def cropImage(image: BufferedImage, targetSize: (Int, Int), option: Option[CropOption] = None): BufferedImage = {
    val (imageWidth, imageHeight) = targetSize
    val ratio = imageWidth.toDouble / imageHeight.toDouble
    val scaledWidth = (image.getHeight() * ratio).toInt
    val cropped = new BufferedImage(imageWidth, imageHeight, image.getType())
    val offsetX = (image.getWidth() - scaledWidth) / 2
    val g = cropped.createGraphics()
    val (sourceX1, sourceX2) = option match {
      case Some(Mirror) => (offsetX + scaledWidth, offsetX)
      case _ => (offsetX, offsetX + scaledWidth)
    }
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.drawImage(image, 0, 0, imageWidth, imageHeight, sourceX1, 0, sourceX2, image.getHeight(), null)
    g.dispose()
    cropped
  }

  def doImport(file: Array[Byte], baseNames: Iterator[String]) = {

    val stream = new ByteArrayInputStream(file);
    val rootzip = new ZipInputStream(stream)

    import collection.JavaConverters._

    handle(rootzip, baseNames)(handlePhoto _)
  }
}