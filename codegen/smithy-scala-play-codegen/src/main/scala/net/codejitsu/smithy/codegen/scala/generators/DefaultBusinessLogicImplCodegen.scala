package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.ScalaPlayWriter
import software.amazon.smithy.codegen.core.SymbolProvider
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.knowledge.TopDownIndex
import software.amazon.smithy.model.shapes.ServiceShape

import java.util.logging.Logger
import scala.jdk.CollectionConverters.SetHasAsScala

class DefaultBusinessLogicImplCodegen(
  val serviceShape: ServiceShape,
  val symbolProvider: SymbolProvider,
  val writer: ScalaPlayWriter,
  val model: Model) {
  val logger: Logger = Logger.getLogger(classOf[DefaultBusinessLogicImplCodegen].getName)

  def generateDefaultBusinessLogicImpl(): Unit = {
    logger.info(s"[DefaultRulesImplCodegen]: start 'generate' for ${serviceShape.getId.getName}")

    val service = symbolProvider.toSymbol(model.getServiceShapes.asScala.head)
    val index = TopDownIndex.of(model)
    val operationsShapes = index.getContainedOperations(model.getServiceShapes.asScala.head)

    val inputOutputModels = operationsShapes.asScala.flatMap { operation =>
      Seq(operation.getInputShape.getName, operation.getOutputShape.getName)
    }

    writer.write("package ${L}.generated.rules", serviceShape.getId.getNamespace)
    writer.write("")

    writer.write("import ${L}.generated.models.{${L}}", serviceShape.getId.getNamespace, inputOutputModels.toSeq.sorted.mkString(", "))
    writer.write("")
    writer.write("import javax.inject.Singleton")
    writer.write("")
    writer.write("@Singleton")

    writer.openBlock("class ${L}RulesDefaultImpl extends ${L}Rules {", service.getName, service.getName)

    val httpEndpoints = operationsShapes.asScala.toSeq.filter(_.hasTrait("http"))

    httpEndpoints.sorted.zipWithIndex.foreach { case (operation, i) =>
      val methodName = s"${operation.getId.getName.head.toLower}${operation.getId.getName.substring(1)}"

      writer.openBlock("override def ${L}(${L}: ${L}): ${L} = {", methodName, s"${methodName}Input", operation.getInputShape.getName, operation.getOutputShape.getName)
      writer.write("???") // TODO default implementation config
      writer.closeBlock("}")

      if (i != httpEndpoints.size - 1) {
        writer.write("")
      }
    }
    writer.closeBlock("}")
  }
}
