package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.{ScalaPlayContext, ScalaPlaySettings, ScalaPlayWriter}
import software.amazon.smithy.codegen.core.Symbol
import software.amazon.smithy.codegen.core.directed.GenerateServiceDirective
import software.amazon.smithy.model.knowledge.TopDownIndex
import software.amazon.smithy.model.traits.HttpTrait

import java.util.logging.Logger
import scala.jdk.CollectionConverters.CollectionHasAsScala

object RoutesCodegen {
  val logger: Logger = Logger.getLogger(classOf[RoutesCodegen.type].getName)

  def generateRoutes(directive: GenerateServiceDirective[ScalaPlayContext, ScalaPlaySettings], writer: ScalaPlayWriter): Unit = {
    logger.info(s"[RoutesCodegen]: start 'generate' for ${directive.shape.getId.getName}")
    val index = TopDownIndex.of(directive.model)
    val operationsShapes = index.getContainedOperations(directive.model.getServiceShapes.asScala.head)
    val httpEndpoints = operationsShapes.asScala.toSeq.filter(_.hasTrait("http"))

    val service = directive.symbolProvider.toSymbol(directive.model.getServiceShapes.asScala.head)
    val handler = service.expectProperty("handler", classOf[Symbol])

    httpEndpoints.sorted.zipWithIndex.foreach { case (operation, _) =>
      val httpTrait = operation.getTrait(classOf[HttpTrait])

      val methodName = s"${operation.getId.getName.head.toLower}${operation.getId.getName.substring(1)}"

      writer.write("$L      $L      $L.generated.controllers.${L}.$L($L: $L)", httpTrait.get().getMethod,
        httpTrait.get().getUri.toString.replace("{", ":").replace("}", ""),
        directive.shape.getId.getNamespace, handler.getName, methodName, httpTrait.get().getUri.getLabels.get(0).toString.replace("{", "").replace("}", ""),
        s"${directive.shape.getId.getNamespace}.generated.models.${operation.getInputShape.getName}")
    }
  }
}