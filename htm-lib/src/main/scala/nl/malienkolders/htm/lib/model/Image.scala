package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import net.liftweb.json._

class Image extends LongKeyedMapper[Image] with IdPK with CreatedUpdated with OneToMany[Long, Image] {
  def getSingleton = Image

  object name extends MappedString(this, 128)
  object scaledImages extends MappedOneToMany(ScaledImage, ScaledImage.image, OrderBy(ScaledImage.resolution, Ascending))

  object mimeType extends MappedString(this, 64)
  object extension extends MappedString(this, 4)

  def findResolution(res: Resolution): Box[ScaledImage] = scaledImages.find(_.resolution.get == res.toString)
  def addResolution(res: Resolution): ScaledImage = findResolution(res).getOrElse {
    val newRes = ScaledImage.create.resolution(res.toString)
    scaledImages += newRes
    newRes
  }
  def hasResolution(res: Resolution) = findResolution(res).isDefined
}

object Image extends Image with LongKeyedMetaMapper[Image] {
  override def dbTableName = "images"
  override def dbIndexes = UniqueIndex(name) :: super.dbIndexes
}

class ScaledImage extends LongKeyedMapper[ScaledImage] with IdPK {
  def getSingleton = ScaledImage

  object image extends MappedLongForeignKey(this, Image)
  object resolution extends MappedString(this, 16)
}

object ScaledImage extends ScaledImage with LongKeyedMetaMapper[ScaledImage] {
  override def dbTableName = "scaled_images"
}

case class Resolution(width: Int, height: Int) {
  override def toString = s"${width}x${height}"
}

object Resolution {
  def fromString(s: String) = Resolution(s.takeWhile(_.isDigit).toInt, s.reverse.takeWhile(_.isDigit).reverse.toInt)

  def supported = List(
    Resolution(1024, 768),
    Resolution(1920, 1080),
    Resolution(1280, 720))
}