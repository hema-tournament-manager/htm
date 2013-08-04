package nl.malienkolders.htm.viewer.jmonkey.lib.util

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import _root_.com.jme3.texture._

abstract class PaintableImage(val size: (Int, Int), hasAlpha: Boolean) extends Image {
  private val backImg = new BufferedImage(size._1, size._2,
    if (hasAlpha) BufferedImage.TYPE_4BYTE_ABGR else BufferedImage.TYPE_3BYTE_BGR)
  setFormat(if (hasAlpha) Image.Format.RGBA8 else Image.Format.RGB8)
  setWidth(backImg.getWidth())
  setHeight(backImg.getHeight())
  private val scratch = ByteBuffer.allocateDirect(4 * backImg.getWidth()
    * backImg.getHeight())

  def refreshImage() {
    val g: Graphics2D = backImg.createGraphics()
    paint(g)
    g.dispose()

    /* get the image data */
    val lData: Array[Byte] = backImg.getRaster().getDataElements(0, 0, backImg.getWidth(), backImg.getHeight(), null).asInstanceOf[Array[Byte]]
    scratch.clear()
    scratch.put(lData, 0, lData.length)
    scratch.rewind()
    setData(scratch)
  }

  def paint(g: Graphics2D)
}