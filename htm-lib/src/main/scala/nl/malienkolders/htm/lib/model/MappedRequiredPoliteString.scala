package nl.malienkolders.htm.lib.model

import net.liftweb.mapper.MappedPoliteString
import net.liftweb.mapper.Mapper
import net.liftweb.common.Box
import scala.collection.immutable.List
import java.lang.reflect.Method;

class MappedRequiredPoliteString[T <: Mapper[T]](towner: T, theMaxLen: Int) extends MappedPoliteString[T](towner, theMaxLen) {

  override def required_? = true
  override def validations = valMinLen(1, this.name + " is a required field") _ :: Nil

}