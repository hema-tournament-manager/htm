package lib

import org.specs2.mutable._
import java.util.zip.ZipOutputStream
import java.io.FileOutputStream
import java.io.File
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.io.ByteArrayInputStream
import java.awt.image.BufferedImage
import java.awt.Color
import javax.imageio.ImageIO

class ImageUtilSpec extends Specification {

  def checkOutput(file: File): Boolean = {
    val thresholded = ImageIO.read(file)

    val alpha = thresholded.getAlphaRaster()

    val checks = for {
      x <- 0 to thresholded.getWidth() - 1
      y <- 0 to thresholded.getHeight() - 1
    } yield {
      val pixel = Array[Int](0)
      alpha.getPixel(x, y, pixel);

      if (y == 0) {
        // top row has alpha >= 127
        pixel(0) == 255
      } else {
        // bottom row has alpha < 127
        pixel(0) == 0
      }
    }

    checks.forall(x => x)
  }

  "ImageUtil" should {
    "threshold images with transparent pixels" in {
      val image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB)
      val g = image.createGraphics()
      g.setColor(new Color(0, 0, 0, 255))
      g.fillRect(0, 0, 1, 1)
      g.setColor(new Color(0, 0, 0, 127))
      g.fillRect(1, 0, 1, 1)
      g.setColor(new Color(0, 0, 0, 126))
      g.fillRect(0, 1, 1, 1)
      g.setColor(new Color(0, 0, 0, 0))
      g.fillRect(1, 1, 1, 1)
      g.dispose()
      ImageIO.write(image, "png", new File("transparent.png"))
      ImageUtil.generateThresholded(new File("transparent.png"))
      val outputFile = new File("transparent-thresholded.png")

      outputFile.exists() && checkOutput(outputFile) must beTrue
    }

    "not crash when attempting to threshold an opaque image" in {
      val image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB)
      val g = image.createGraphics()
      g.setColor(new Color(255, 0, 0))
      g.fillRect(0, 0, 1, 1)
      g.setColor(new Color(0, 255, 0))
      g.fillRect(1, 0, 1, 1)
      g.setColor(new Color(0, 0, 255))
      g.fillRect(0, 1, 1, 1)
      g.setColor(new Color(255, 255, 255))
      g.fillRect(1, 1, 1, 1)
      g.dispose()
      ImageIO.write(image, "png", new File("opaque.png"))
      ImageUtil.generateThresholded(new File("opaque.png"))
      val outputFile = new File("opaque-thresholded.png")

      outputFile.exists() must beFalse
    }

  }

}
