package lib

import javax.imageio.ImageIO
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

object ImageUtil {

  def generateThresholded(file: File): Option[File] = generateThresholded(file, new FileInputStream(file))

  def generateThresholded(file: File, input: InputStream): Option[File] = {
    val newFile = new File(file.getAbsolutePath().replace(".png", "-thresholded.png"))

    if (newFile.exists()) {
      return Some(newFile);
    }

    val image = ImageIO.read(input)

    val alpha = image.getAlphaRaster()

    if (alpha == null) {
      return None;
    }

    for {
      x <- 0 to image.getWidth() - 1
      y <- 0 to image.getHeight() - 1
    } {
      val pixel = Array[Int](0)
      alpha.getPixel(x, y, pixel);

      if (pixel(0) > 127) {
        pixel.update(0, 255)
      } else {
        pixel.update(0, 0)
      }

      alpha.setPixel(x, y, pixel)
    }

    ImageIO.write(image, "png", newFile)

    return Some(newFile)

  }

}