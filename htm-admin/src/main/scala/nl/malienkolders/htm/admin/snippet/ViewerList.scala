package nl.malienkolders.htm.admin.snippet

import net.liftweb._
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

object ViewerList {

  def render = {
    var name = ""
    var url = ""

    def process(): Unit = {
      Viewer.create.alias(name).url(url).save
      S.redirectTo("/viewers/list")
    }

    ".viewer" #> Viewer.findAll.map { v =>
      val status = v.rest.ping
      ".viewer [class+]" #> (if (status) "success" else "danger") &
        ".name *" #> v.alias.get &
        ".arena" #> Arena.findAll.map(a =>
          "*" #> arenaCheck(a, v)) &
        ".url *" #> v.url.get &
        ".status *" #> (if (status) "Up" else "Down") &
        ".action" #> Seq(
          SHtml.submit("Push photos", () => pushPhotos(v), "class" -> "btn btn-default btn-sm"),
          SHtml.submit("Push images", () => pushImages(v), "class" -> "btn btn-default btn-sm"),
          SHtml.submit("Delete", () => v.delete_!, "class" -> "btn btn-default btn-sm"))
    } &
      "name=name" #> SHtml.onSubmit(name = _) &
      "name=url" #> SHtml.onSubmit(url = _) &
      "name=add" #> SHtml.onSubmitUnit(process)
  }

  def arenaCheck(a: Arena, v: Viewer) =
    SHtml.ajaxCheckbox(v.arenas.contains(a), { b: Boolean =>
      if (b) v.arenas += a else v.arenas -= a
      v.save
      S.redirectTo("")
    }, "title" -> a.name.get)

  def pushPhotos(v: Viewer): Unit = {
    val zipFile = new File("Avatars.zip")
    val out = new ZipOutputStream(new FileOutputStream(zipFile))
    val generatedDir = new File("Avatars/Generated")
    for (f <- generatedDir.listFiles()) {
      out.putNextEntry(new ZipEntry("Avatars/Generated/" + f.getName()))
      IOUtils.copy(new FileInputStream(f), out)
    }
    out.close()
    v.rest.push(zipFile)
  }

  def pushImages(v: Viewer): Unit = {
    val zipFile = new File("Images.zip")
    val out = new ZipOutputStream(new FileOutputStream(zipFile))
    val rootDir = new File("Images")

    if (rootDir.exists()) {
      Resolution.supported foreach { res =>
        val resDir = new File(rootDir, res.toString)
        if (resDir.exists()) {
          for (f <- resDir.listFiles()) {
            out.putNextEntry(new ZipEntry("Images/" + res.toString + "/" + f.getName()))
            IOUtils.copy(new FileInputStream(f), out)
          }
        }
      }
    }
    out.close()
    v.rest.push(zipFile)
  }

}