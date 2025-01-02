package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.ScalaPlayWriter
import software.amazon.smithy.model.shapes.ServiceShape

import java.util.logging.Logger

class ConfigCodegen(
  val serviceShape: ServiceShape,
  val writer: ScalaPlayWriter) {
  val logger: Logger = Logger.getLogger(classOf[ConfigCodegen].getName)

  def generate(): Unit = {
    logger.info(s"[ConfigCodegen]: start 'generate' for ${serviceShape.getId.getName}")

    // TODO config
    val content =
      s"""
         |play.http.errorHandler = "${serviceShape.getId.getNamespace}.generated.util.ErrorHandler"
         |""".stripMargin

    writer.write(content)
  }
}
