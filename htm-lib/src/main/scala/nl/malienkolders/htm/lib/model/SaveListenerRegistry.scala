package nl.malienkolders.htm.lib.model

import net.liftweb.actor.LiftActor
import scala.collection.mutable.LinkedList
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.http.ListenerManager

trait SaveMessageBroadcaster[F <: SaveMessageBroadcaster[F]] extends LongKeyedMapper[F] {
  self: F =>
    
  override def save: Boolean = {
    println("save!")
    val result = super.save
    SaveListenerRegistry.notifyListeners;
    return result;
  }   
  
  override def delete_! = {
    println("delete!")
    val result = super.delete_!
    SaveListenerRegistry.notifyListeners;
    result;
  }
}

case object SavedMessage;

object SaveListenerRegistry {

  private var listeners: List[() => Unit] = Nil

  def addListener(listener: () => Unit) = {
    listeners = listeners :+ listener;
  }

  def notifyListeners {
    listeners.foreach(_());
  }

}