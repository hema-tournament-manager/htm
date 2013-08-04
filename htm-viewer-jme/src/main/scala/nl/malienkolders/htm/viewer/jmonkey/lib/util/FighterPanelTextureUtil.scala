/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.malienkolders.htm.viewer.jmonkey.lib.util;

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import com.jme3.asset.AssetManager
import com.jme3.texture.Texture;
import nl.malienkolders.htm.viewer.jmonkey._

object FighterPanelTextureUtil {

  def getTexture(flagLeft: String, flagRight: String, assetManager: AssetManager): Texture = {
    val texturename = "Fight/Footer/fighter_bar_" + flagLeft + "_" + flagRight + ".png";
    val filename = "RuntimeResources/" + texturename
    try {
      assetManager.loadTexture(texturename)
    } catch {
      case _ =>
        val img = ImageIO.read(new File("RuntimeResources/Fight/Footer/fighter_bar.png"))
        val g = img.createGraphics()
        if (flagLeft != "") {
          try {
            val imgLeft = ImageIO.read(new File("RuntimeResources/Flags/" + flagLeft + "red.png"))
            g.drawImage(imgLeft, 0, 19, null)
          } catch {
            case _ => println("Flag not found: " + flagLeft)
          }
        }
        if (flagRight != "") {
          try {
            val imgRight = ImageIO.read(new File("RuntimeResources/Flags/" + flagRight + "blue.png"))
            g.drawImage(imgRight, 1024 - 122, 19, null)
          } catch {
            case _ => println("Flag not found: " + flagRight)
          }
        }
        g.dispose()
        ImageIO.write(img, "PNG", new File(filename))
        assetManager.loadTexture(texturename)
    }
  }
  def getLiveTexture(left: Boolean, flag: String, assetManager: AssetManager): Texture = {
    val texturename = "LiveStream/Fight/Generated/bar_" + flag + "_" + (if (left) "l" else "r") + ".png";
    val filename = "RuntimeResources/" + texturename
    if (new File(filename).exists()) {
      assetManager.loadTexture(texturename)
    } else {
      val img = ImageIO.read(new File("RuntimeResources/LiveStream/Fight/bar_" + (if (left) "l" else "r") + ".jpg"))
      val g = img.createGraphics()
      if (flag != "") {
        try {
          val flagImg = ImageIO.read(new File("RuntimeResources/Flags/" + flag + (if (left) "red" else "blue") + ".png"))
          if (left)
            g.drawImage(flagImg, 1, 1, 90, 50, 0, 0, flagImg.getWidth(), flagImg.getHeight(), null)
          else
            g.drawImage(flagImg, img.getWidth() - 91, 1, img.getWidth() - 1, 50, 0, 0, flagImg.getWidth(), flagImg.getHeight(), null)
        } catch {
          case _ => println("Flag not found: " + flag)
        }
      }
      g.dispose()
      new File("RuntimeResources/LiveStream/Fight/Generated/").mkdirs
      ImageIO.write(img, "PNG", new File(filename))
      assetManager.loadTexture(texturename)
    }
  }

}
