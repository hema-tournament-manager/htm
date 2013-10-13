import com.jme3.system.AppSettings
import com.jme3.system.JmeCanvasContext
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel
import java.awt.FlowLayout
import java.awt.BorderLayout
import java.awt.EventQueue
import com.jme3.system.JmeContext
import nl.malienkolders.htm.viewer.jmonkey._
import nl.malienkolders.htm.lib.FightView
object TestJme extends App {

  //  val settings = new AppSettings(true)
  //  settings.setWidth(1024)
  //  settings.setHeight(768)
  //
  //  JmeApplication.setSettings(settings)
  //  JmeApplication.createCanvas()
  //  val context = JmeApplication.getContext().asInstanceOf[JmeCanvasContext]
  //  context.setSystemListener(JmeApplication)
  //  context.getCanvas().setPreferredSize(new Dimension(1024, 768))
  //
  //  EventQueue.invokeLater(new Runnable() {
  //    def run {
  //      val window = new JFrame("Test")
  //      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  //      val panel = new JPanel(new BorderLayout())
  //      panel.add(context.getCanvas(), BorderLayout.CENTER)
  //      window.add(panel)
  //      window.pack()
  //      window.setVisible(true)
  //
  //      JmeApplication.startCanvas()
  //    }
  //
  //  })

  JmeApplication.boot(SpectatorScreen, -1, true)

}