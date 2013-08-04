package nl.malienkolders.htm.viewer.jmonkey.lib.state

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import _root_.com.jme3._
import app.{ Application, SimpleApplication }
import app.state.{ AbstractAppState, AppStateManager }
import material.Material
import material.RenderState.{ BlendMode, FaceCullMode }
import scene.{ Geometry, Node }
import scene.shape.Quad
import animation._
import cinematic._
import cinematic.events._
import math._
import nl.malienkolders.htm.lib.model._
import scala.collection.mutable.Queue
import nl.malienkolders.htm.viewer.jmonkey.lib.util._
import java.awt.Color
import com.jme3.material.RenderState
import com.jme3.renderer.queue.RenderQueue

abstract class TournamentAppState(val rootNodeName: String) extends AbstractAppState {

  val rootNode: Node = new Node(rootNodeName)
  var app: SimpleApplication = null
  var customInitialization = false

  override def initialize(stateManager: AppStateManager, app: Application) {
    super.initialize(stateManager, app)
    this.app = app.asInstanceOf[SimpleApplication]

    if (!customInitialization) {
      initializeScene()
      customInitialization = true
    }
  }

  def initializeScene()

  override def stateAttached(stateManager: AppStateManager) {
    super.stateAttached(stateManager)
    app.getRootNode().attachChild(rootNode)
  }

  override def stateDetached(stateManager: AppStateManager) {
    rootNode.removeFromParent()
    super.stateDetached(stateManager)
  }

  protected def createPanel(name: String, quadSize: (Float, Float), texture: String, transparent: Boolean) = {
    val panelQuad = new Quad(quadSize._1, quadSize._2)
    val pivot = new Node(name + "Pivot")
    val panel = new Geometry(name, panelQuad)
    val mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setTexture("ColorMap", app.getAssetManager().loadTexture(texture))
    if (transparent) {
      mat.setTransparent(true)
      mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha)
      mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off)
      mat.getAdditionalRenderState().setAlphaTest(true)
    }
    panel.setMaterial(mat)
    panel.move(-quadSize._1 / 2f, -quadSize._2 / 2f, 0f)
    pivot.attachChild(panel)
    pivot
  }

  def createTexturedPanel(geometryName: String, textureName: String, upp: Float, transparent: Boolean, cullBackFace: Boolean = true) = {
    val texture = app.getAssetManager().loadTexture(textureName)
    val quad = new Quad(texture.getImage().getWidth() * upp, texture.getImage().getHeight() * upp)
    val panel = new Geometry(geometryName, quad)
    val material = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md")
    material.setTexture("ColorMap", texture)
    material.setTransparent(transparent)
    if (transparent) {
      material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha)
      panel.setQueueBucket(RenderQueue.Bucket.Transparent)
    }
    if (cullBackFace)
      material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back)
    else
      material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off)
    panel.setMaterial(material)
    panel
  }

}
