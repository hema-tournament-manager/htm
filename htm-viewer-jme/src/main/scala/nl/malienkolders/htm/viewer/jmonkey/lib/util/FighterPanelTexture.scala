/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.malienkolders.htm.viewer.jmonkey.lib.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

class FighterPanelTexture(var _countryLeft: Option[String], var _countryRight: Option[String]) extends PaintableImage((1024, 120), true) {

  private var fighterBarImg: BufferedImage = ImageIO.read(new File("RuntimeResources/Graphics/Fight/Footer/fighter_bar.png"))

  refreshImage()

  override def paint(g: Graphics2D) {
    g.clearRect(0, 0, getWidth(), getHeight())
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

    g.drawImage(fighterBarImg, 0, 0, null)
    if (_countryLeft.isDefined)
      g.drawImage(ImageIO.read(new File("RuntimeResources/Graphics/Flags/" + _countryLeft.get + "red.png")), 0, 19, null)
    if (_countryRight.isDefined)
      g.drawImage(ImageIO.read(new File("RuntimeResources/Graphics/Flags/" + _countryRight.get + "blue.png")), 1024 - 122, 19, null)
  }

  def countryLeft_=(countryName: Option[String]) {
    _countryLeft = countryName
    refreshImage()
  }

  def countryRight_=(countryName: Option[String]) {
    _countryRight = countryName
    refreshImage()
  }

}
