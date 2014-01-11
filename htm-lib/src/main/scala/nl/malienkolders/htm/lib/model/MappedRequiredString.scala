package nl.malienkolders.htm.lib.model

import net.liftweb.mapper.MappedString
import net.liftweb.common.Box
import scala.collection.immutable.List
import net.liftweb.mapper.Mapper
import java.lang.reflect.Method;

class MappedRequiredString[T <: Mapper[T]](towner: T, theMaxLen: Int) extends MappedString[T](towner, theMaxLen) {
  
  override def required_? = true
  override def validations = valMinLen(1, this.name + " is a required field") _ :: Nil
}