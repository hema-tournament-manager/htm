/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.malienkolders.htm.viewer.jmonkey.lib.util;

import java.awt._
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import com.jme3.asset.AssetManager
import com.jme3.texture.Texture
import nl.malienkolders.htm.lib.model.MarshalledParticipant
import java.awt.RenderingHints
import scala.collection.immutable.List

object AvatarTextureUtil {

  val WIDTH = 307
  val HEIGHT = 462
  val PLACEHOLDER = "placeholder"

  abstract class Type {
    def suffix: String
    def extension: String
  }
  abstract class Base extends Type {
    override val suffix = ""
  }
  case object BasePng extends Base {
    override val extension = ".png"
  }
  case object BaseJpg extends Base {
    override val extension = ".jpg"
  }
  case object BaseJpeg extends Base {
    override val extension = ".jpeg"
  }
  case object BaseGif extends Base {
    override val extension = ".gif"
  }
  abstract class Generated extends Type {
    override val extension = ".png"
  }
  case object Left extends Generated {
    override val suffix = "_l"
  }
  case object Right extends Generated {
    override val suffix = "_r"
  }

  def getTextureName(participant: MarshalledParticipant, side: Type, tournamentName: String): String = getTextureName(participant.externalId, side, tournamentName)

  def getTextureOrPlaceholderName(participant: MarshalledParticipant, side: Type, tournamentName: String): String = getTextureOrPlaceholderName(participant.externalId, side, tournamentName)

  def getTextureOrPlaceholderName(participantId: String, side: Type, tournamentName: String): String = {
    if (new File(getFileName(participantId, side, tournamentName)).exists()) {
      getTextureName(participantId, side, tournamentName)
    } else {
      getTextureName(PLACEHOLDER, side, tournamentName)
    }
  }

  def folder(side: Type) = "Avatars/" + (side match {
    case _: Base => ""
    case _ => "Generated/"
  })

  def getTextureName(participantId: String, side: Type, tournamentName: String): String = folder(side) + participantId + tournamentName + side.suffix + side.extension

  def getFileName(participant: MarshalledParticipant, side: Type, tournamentName: String): String = getFileName(participant.externalId, side, tournamentName)

  def getFileName(participantId: String, side: Type, tournamentName: String): String = "RuntimeResources/" + getTextureName(participantId, side, tournamentName)

  def createSideImages(participant: MarshalledParticipant, tournamentName: String) {
    createSideImages(participant.externalId, tournamentName)
  }

  def createSideImages(participantId: String, tournamentName: String) {
    new File("RuntimeResources/Avatars/Generated/").mkdirs()
    val fileTypes = List(BaseJpg, BaseJpeg, BasePng, BaseGif)
    val fileBases = (fileTypes.map((_, "_" + tournamentName)) ++ fileTypes.map((_, ""))).map {
      case (f, t) =>
        new File(getFileName(participantId, f, t))
    }
    val fileBase = fileBases.find(_.exists())

    val file_l = new File(getFileName(participantId, Left, "_" + tournamentName))
    val file_r = new File(getFileName(participantId, Right, "_" + tournamentName))
    if (fileBase.isDefined && (!file_l.exists() || !file_r.exists())) {
      try {
        val base = ImageIO.read(fileBase.get)
        if (!file_l.exists()) {
          val l = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB)
          val g_l = l.createGraphics()
          g_l.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
          g_l.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
          g_l.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
          g_l.drawImage(base, 0, 0, WIDTH, HEIGHT, 0, 0, base.getWidth(), base.getHeight(), null)
          g_l.dispose()
          ImageIO.write(l, "PNG", file_l)
        }

        if (!file_r.exists()) {
          val r = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB)
          val g_r = r.createGraphics()
          g_r.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
          g_r.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
          g_r.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
          g_r.drawImage(base, 0, 0, WIDTH, HEIGHT, base.getWidth(), 0, 0, base.getHeight(), null)
          g_r.dispose()

          ImageIO.write(r, "PNG", file_r)
        }
      } catch {
        case e: Exception =>
          Logger.getLogger("AvatarTextureUtil").log(Level.SEVERE, null, e)
      }
    }
  }

  def loadImage(participant: MarshalledParticipant, side: Type, tournamentName: String): BufferedImage = {
    try {
      createSideImages(participant, tournamentName)
      ImageIO.read(new File(getFileName(participant, side, tournamentName)))
    } catch {
      case ex: Exception =>
        createSideImages(PLACEHOLDER, tournamentName)
        try {
          ImageIO.read(new File(getFileName(PLACEHOLDER, side, tournamentName)))
        } catch {
          case ex: Exception =>
            Logger.getLogger("AvatarTextureUtil").log(Level.SEVERE, null, ex)
            null
        }
    }
  }

  def getTexture(participant: MarshalledParticipant, side: Type, touenamentName: String, assetManager: AssetManager): Texture = getTexture(participant.externalId, side, touenamentName, assetManager)

  def getTexture(participantId: String, side: Type, tournamentName: String, assetManager: AssetManager): Texture = {
    createSideImages(participantId, tournamentName)
    createSideImages(PLACEHOLDER, tournamentName)
    assetManager.loadTexture(getTextureOrPlaceholderName(participantId, side, "_" + tournamentName))
  }

}
