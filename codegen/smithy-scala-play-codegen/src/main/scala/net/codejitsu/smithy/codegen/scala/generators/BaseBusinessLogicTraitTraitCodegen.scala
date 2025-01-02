package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.ScalaPlayWriter
import software.amazon.smithy.codegen.core.SymbolProvider
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.knowledge.TopDownIndex
import software.amazon.smithy.model.shapes.ServiceShape

import java.util.logging.Logger
import scala.jdk.CollectionConverters.SetHasAsScala

class BaseBusinessLogicTraitTraitCodegen(
  val serviceShape: ServiceShape,
  val symbolProvider: SymbolProvider,
  val writer: ScalaPlayWriter,
  val model: Model) {
  val logger: Logger = Logger.getLogger(classOf[BaseBusinessLogicTraitTraitCodegen].getName)

  def generateBaseBusinessLogicTrait(): Unit = {
    logger.info(s"[BaseRulesTraitCodegen]: start 'generate' for ${serviceShape.getId.getName}")

    val service = symbolProvider.toSymbol(model.getServiceShapes.asScala.head)
    val index = TopDownIndex.of(model)
    val operationsShapes = index.getContainedOperations(model.getServiceShapes.asScala.head)

    val inputOutputModels = operationsShapes.asScala.flatMap { operation =>
      Seq(operation.getInputShape.getName, operation.getOutputShape.getName)
    }

    writer.write("package ${L}.generated.rules", serviceShape.getId.getNamespace)
    writer.write("")
    writer.write("import com.google.inject.ImplementedBy")

    writer.write("import ${L}.generated.models.{${L}}", serviceShape.getId.getNamespace, inputOutputModels.toSeq.sorted.mkString(", "))
    writer.write("")
    writer.write("@ImplementedBy(classOf[${L}RulesDefaultImpl])", service.getName) // TODO this suffix also belongs to config

    writer.openBlock("trait ${L}Rules {", service.getName)

    val httpEndpoints = operationsShapes.asScala.toSeq.filter(_.hasTrait("http"))

    httpEndpoints.sorted.zipWithIndex.foreach { case (operation, i) =>
      val methodName = s"${operation.getId.getName.head.toLower}${operation.getId.getName.substring(1)}"

      writer.write("def ${L}(${L}: ${L}): ${L}", methodName, s"${methodName}Input", operation.getInputShape.getName, operation.getOutputShape.getName)

      if (i != httpEndpoints.size - 1) {
        writer.write("")
      }
    }
    writer.closeBlock("}")
  }
}
