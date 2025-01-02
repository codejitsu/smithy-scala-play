package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.ScalaPlayWriter
import software.amazon.smithy.codegen.core.{Symbol, SymbolProvider}
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.knowledge.TopDownIndex
import software.amazon.smithy.model.shapes.ServiceShape
import software.amazon.smithy.model.traits.HttpTrait

import java.util.logging.Logger
import scala.jdk.CollectionConverters.CollectionHasAsScala

class RoutesCodegen(
  val serviceShape: ServiceShape,
  val symbolProvider: SymbolProvider,
  val writer: ScalaPlayWriter,
  val model: Model) {
  val logger: Logger = Logger.getLogger(classOf[RoutesCodegen].getName)

  def generateRoutes(): Unit = {
    logger.info(s"[RoutesCodegen]: start 'generate' for ${serviceShape.getId.getName}")
    val index = TopDownIndex.of(model)
    val operationsShapes = index.getContainedOperations(model.getServiceShapes.asScala.head)
    val httpEndpoints = operationsShapes.asScala.toSeq.filter(_.hasTrait("http"))

    val service = symbolProvider.toSymbol(model.getServiceShapes.asScala.head)
    val handler = service.expectProperty("handler", classOf[Symbol])

    httpEndpoints.sorted.zipWithIndex.foreach { case (operation, _) =>
      val httpTrait = operation.getTrait(classOf[HttpTrait])

      val methodName = s"${operation.getId.getName.head.toLower}${operation.getId.getName.substring(1)}"

      writer.write("$L      $L      $L.generated.controllers.${L}.$L($L: $L)", httpTrait.get().getMethod,
        httpTrait.get().getUri.toString.replace("{", ":").replace("}", ""),
        serviceShape.getId.getNamespace, handler.getName, methodName, httpTrait.get().getUri.getLabels.get(0).toString.replace("{", "").replace("}", ""),
        s"${serviceShape.getId.getNamespace}.generated.models.${operation.getInputShape.getName}")
    }
  }
}