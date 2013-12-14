package lib

import javax.imageio.ImageIO
import java.io.File

object ImageUtil {

  def generateThresholded(filename: String): String = {
    val newFilename = filename.replace(".png", "-thresholded.png");

    if (new File(newFilename).exists()) {
      return newFilename;
    }

    val image = ImageIO.read(new File(filename))

    val alpha = image.getAlphaRaster()

    if (alpha == null) {
      return filename;
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

    ImageIO.write(image, "png", new File(newFilename))

    return newFilename;

  }

}