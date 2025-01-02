package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.{ScalaPlayContext, ScalaPlaySettings, ScalaPlayWriter}
import software.amazon.smithy.codegen.core.directed.GenerateServiceDirective

import java.util.logging.Logger

object ConfigCodegen {
  val logger: Logger = Logger.getLogger(classOf[ConfigCodegen.type].getName)

  def generateConfiguration(directive: GenerateServiceDirective[ScalaPlayContext, ScalaPlaySettings], writer: ScalaPlayWriter): Unit = {
    logger.info(s"[ConfigCodegen]: start 'generate' for ${directive.shape.getId.getName}")

    // TODO config
    val content =
      s"""
         |play.http.errorHandler = "${directive.shape.getId.getNamespace}.generated.util.ErrorHandler"
         |""".stripMargin

    writer.write(content)
  }
}
