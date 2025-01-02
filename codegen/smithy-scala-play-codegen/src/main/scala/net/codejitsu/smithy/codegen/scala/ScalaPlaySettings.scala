package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.codegen.core.CodegenException
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.node.ObjectNode
import software.amazon.smithy.model.shapes.{ServiceShape, ShapeId}

import scala.jdk.StreamConverters._

case class ScalaPlaySettings(service: ShapeId)

object ScalaPlaySettings {
  private final val SERVICE = "service"

  def mkSettings(settings: ObjectNode, model: Model): ScalaPlaySettings = {
    val service = settings
      .getStringMember(SERVICE)
      .map(_.expectShapeId())
      .orElseGet(() => readFromModel(model))

    ScalaPlaySettings(service)
  }

  private def readFromModel(model: Model): ShapeId = {
    val services = model
      .shapes(classOf[ServiceShape]).toScala(LazyList)
      .map(_.getId)

    if (services.isEmpty) {
      throw new CodegenException("The model does not provide any service.")
    } else if (services.size > 1) {
      throw new CodegenException("The model provides more than one service.")
    } else {
      services.head
    }
  }
}
