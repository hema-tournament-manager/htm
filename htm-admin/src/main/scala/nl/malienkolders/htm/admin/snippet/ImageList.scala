package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import common._
import http._
import mapper._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._
import java.text.SimpleDateFormat
import java.util.Date
import java.io.File
import java.util.zip.ZipOutputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import org.apache.commons.io.IOUtils
import org.apache.commons.io.FileUtils
import java.io.FileInputStream
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

object ImageList {

  def render = {
    var name = ""
    var upload: Box[FileParamHolder] = Empty
    var resolution = Resolution.supported.head

    def process() = {
      if (name.isEmpty()) {
        S.notice("Name must not be empty")
      } else if (upload.isEmpty) {
        S.notice("Choose an image to upload")
      } else {
        val format = upload.get.mimeType match {
          case "image/jpeg" => Some("jpeg")
          case "image/png" => Some("png")
          case _ => None
        }
        if (format.isDefined) {
          val extension = upload.get.fileName.reverse.takeWhile(_ != '.').reverse
          val img = Image.find(By(Image.name, name)).getOrElse(Image.create.name(name).mimeType(upload.get.mimeType).extension(extension))
          if (img.mimeType.get == upload.get.mimeType) {
            val bitmap = ImageIO.read(upload.get.fileStream)
            val dir = new File(s"Images/$resolution/")
            dir.mkdirs()
            ImageIO.write(bitmap, format.get, new File(dir, name + "." + extension))
            val thumb = new BufferedImage(bitmap.getWidth() / 20, bitmap.getHeight() / 20, bitmap.getType())
            val g = thumb.createGraphics()
            g.drawImage(bitmap, 0, 0, thumb.getWidth(), thumb.getHeight(), 0, 0, bitmap.getWidth(), bitmap.getHeight(), null)
            g.dispose
            val thumbDir = new File(s"Thumbs/$resolution/")
            thumbDir.mkdirs()
            ImageIO.write(thumb, format.get, new File(thumbDir, name + "." + extension))
            img.addResolution(resolution)
            img.save()
          } else {
            S.notice("Image must be of type " + img.mimeType.get)
          }
        } else {
          S.notice("Unsupported file format " + upload.get.mimeType)
        }
      }
    }

    def selectResolution = SHtml.select(Resolution.supported.map(r => r.toString -> r.toString), Full(resolution.toString), r => resolution = Resolution.fromString(r), "class" -> "form-control")

    "thead" #> (
      ".resolution" #> Resolution.supported.map(res =>
        <th>{ res.toString }</th>)) &
      ".image" #> Image.findAll.map(i =>
        "tr [class+]" #> (if (i.hasAllResolutions) "" else "warning") &
          ".name *" #> i.name.get &
          ".mimetype *" #> i.mimeType.get &
          ".resolution" #> Resolution.supported.map(res =>
            <td>{ if (i.hasResolution(res)) <img src={ "/image/" + res.toString + "/" + i.name }/> else <em>missing</em> }</td>)) &
      "name=name" #> SHtml.text(name, name = _, "class" -> "form-control") &
      "name=upload" #> SHtml.fileUpload(f => upload = Full(f)) &
      "name=resolution" #> selectResolution &
      "name=submit" #> SHtml.onSubmitUnit(process)
  }

  def image(resolution: String, name: String): Box[LiftResponse] = {
    for {
      i <- Image.find(By(Image.name, name))
    } yield {
      val bytes = FileUtils.readFileToByteArray(new File(s"Thumbs/$resolution/${name}.${i.extension.get}"))
      InMemoryResponse(bytes, ("Content-Type" -> i.mimeType.get) :: Nil, Nil, 200)
    }

  }

}