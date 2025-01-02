package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.{ScalaPlayContext, ScalaPlaySettings, ScalaPlayWriter}
import software.amazon.smithy.codegen.core.directed.GenerateServiceDirective
import software.amazon.smithy.model.knowledge.TopDownIndex

import java.util.logging.Logger
import scala.jdk.CollectionConverters.SetHasAsScala

object BaseBusinessLogicTraitTraitCodegen {
  val logger: Logger = Logger.getLogger(classOf[BaseBusinessLogicTraitTraitCodegen.type].getName)

  def generateBaseBusinessLogicTrait(directive: GenerateServiceDirective[ScalaPlayContext, ScalaPlaySettings], writer: ScalaPlayWriter
  ): Unit = {
    logger.info(s"[BaseRulesTraitCodegen]: start 'generate' for ${directive.shape.getId.getName}")

    val service = directive.symbolProvider.toSymbol(directive.model.getServiceShapes.asScala.head)
    val index = TopDownIndex.of(directive.model)
    val operationsShapes = index.getContainedOperations(directive.model.getServiceShapes.asScala.head)

    val inputOutputModels = operationsShapes.asScala.flatMap { operation =>
      Seq(operation.getInputShape.getName, operation.getOutputShape.getName)
    }

    writer.write("package ${L}.generated.rules", directive.shape.getId.getNamespace)
    writer.write("")
    writer.write("import com.google.inject.ImplementedBy")

    writer.write("import ${L}.generated.models.{${L}}", directive.shape.getId.getNamespace, inputOutputModels.toSeq.sorted.mkString(", "))
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
