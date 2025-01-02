package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.ScalaPlayWriter
import software.amazon.smithy.model.shapes.ServiceShape

import java.util.logging.Logger

class BuildPropertiesCodegen(
  val serviceShape: ServiceShape,
  val writer: ScalaPlayWriter) {
  val logger: Logger = Logger.getLogger(classOf[BuildPropertiesCodegen].getName)

  def generatePlayBuildProperties(): Unit = {
    logger.info(s"[BuildPropertiesCodegen]: start 'generate' for ${serviceShape.getId.getName}")

    // TODO config
    // TODO mustache
    val content =
      s"""
         |sbt.version=1.10.6
         |""".stripMargin

    writer.write(content)
  }
}