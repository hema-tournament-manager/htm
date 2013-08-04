package nl.malienkolders.htm.viewer.jmonkey.lib.util

import java.awt.Color
import java.awt.Font
import _root_.com.jme3._
import asset.AssetManager
import material.Material
import material.RenderState.BlendMode
import material.RenderState.FaceCullMode
import renderer.queue.RenderQueue.Bucket
import scene.Geometry
import scene.Node
import scene.shape.Quad
import texture.Texture2D;
import java.awt.image.BufferedImage

class TextLabel(private var _text: String,
                var align: Align,
                val fontFamily: String,
                private var _color: Color,
                val size: (Float, Float),
                val pixelsPerUnit: Int,
                val assetManager: AssetManager,
                val faceCullMode: FaceCullMode = FaceCullMode.Off) extends Node {

  val resolution: (Int, Int) = ((size._1 * pixelsPerUnit).toInt, (size._2 * pixelsPerUnit).toInt)
  val quad = new Quad(size._1, size._2, true);
  val label = new Geometry("TextLabel" + TextLabel.nextId.toString, quad);
  val material = new Material(assetManager,
    "Common/MatDefs/Misc/Unshaded.j3md");
  material.setTexture("ColorMap", createTexture());
  material.setTransparent(true);
  material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
  material.getAdditionalRenderState().setFaceCullMode(faceCullMode);
  material.getAdditionalRenderState().setAlphaTest(true);
  label.setMaterial(material);
  label.setQueueBucket(Bucket.Transparent);

  attachChild(label);

  def this(text: String, fontFamily: String, color: Color, size: (Float, Float), pixelsPerUnit: Int, assetManager: AssetManager,
           faceCullMode: FaceCullMode = FaceCullMode.Off) =
    this(text, AlignCenter, fontFamily, color, size, pixelsPerUnit, assetManager, faceCullMode)

  def this(text: String, color: Color, size: (Float, Float), pixelsPerUnit: Int, assetManager: AssetManager, faceCullMode: FaceCullMode = FaceCullMode.Off) =
    this(text, AlignCenter, Font.DIALOG, color, size, pixelsPerUnit, assetManager, faceCullMode)

  def createTexture() = {
    val img: PaintableImage = new TextImage(text, align, fontFamily, color, resolution)
    img.refreshImage()
    new Texture2D(img)
  }

  def text = _text
  def text_=(newText: String) {
    _text = newText;
    material.setTexture("ColorMap", createTexture());
    material.setTransparent(true);
    material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    material.getAdditionalRenderState().setFaceCullMode(faceCullMode);
    material.getAdditionalRenderState().setAlphaTest(true);
  }

  def color = _color
  def color_=(color: Color) {
    this.color = color;
    material.setTexture("ColorMap", createTexture());
    material.setTransparent(true);
    material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    material.getAdditionalRenderState().setFaceCullMode(faceCullMode);
    material.getAdditionalRenderState().setAlphaTest(true);
  }
}

class VarTextLabel(val text: String,
                   val fontFamily: String,
                   private var _color: Color,
                   val height: Float,
                   val pixelsPerUnit: Int,
                   val assetManager: AssetManager,
                   val faceCullMode: FaceCullMode = FaceCullMode.Off) extends Node {

  val tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
  val tempG = tempImg.createGraphics()
  val pixelWidth = tempG.getFontMetrics(new Font(fontFamily, Font.PLAIN, (height * pixelsPerUnit).toInt)).stringWidth(text).max(1)
  val width = (pixelWidth.toFloat / pixelsPerUnit).max(0.001f)
  val resolution: (Int, Int) = (pixelWidth, (height * pixelsPerUnit).toInt)
  val quad = new Quad(width, height, true)
  val label = new Geometry("TextLabel" + TextLabel.nextId.toString, quad)
  val material = new Material(assetManager,
    "Common/MatDefs/Misc/Unshaded.j3md");
  material.setTexture("ColorMap", createTexture());
  material.setTransparent(true);
  material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
  material.getAdditionalRenderState().setFaceCullMode(faceCullMode);
  material.getAdditionalRenderState().setAlphaTest(true);
  label.setMaterial(material);
  label.setQueueBucket(Bucket.Transparent);

  attachChild(label);

  def this(text: String, color: Color, height: Float, pixelsPerUnit: Int, assetManager: AssetManager, faceCullMode: FaceCullMode = FaceCullMode.Off) =
    this(text, Font.DIALOG, color, height, pixelsPerUnit, assetManager, faceCullMode)

  def createTexture() = {
    val img: PaintableImage = new TextImage(text, AlignLeft, fontFamily, color, resolution)
    img.refreshImage()
    new Texture2D(img)
  }

  def color = _color
  def color_=(color: Color) {
    this.color = color;
    material.setTexture("ColorMap", createTexture());
    material.setTransparent(true);
    material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    material.getAdditionalRenderState().setFaceCullMode(faceCullMode);
    material.getAdditionalRenderState().setAlphaTest(true);
  }
}

object TextLabel {
  var counter = 0
  def nextId = {
    counter += 1
    counter
  }
}

abstract class Align
case object AlignLeft extends Align
case object AlignCenter extends Align
case object AlignRight extends Align