package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.ScalaPlayWriter
import software.amazon.smithy.model.shapes.ServiceShape

import java.util.logging.Logger

class PluginsSbtCodegen(
  val serviceShape: ServiceShape,
  val writer: ScalaPlayWriter) {
  val logger: Logger = Logger.getLogger(classOf[PluginsSbtCodegen].getName)

  def generatePluginsSbt(): Unit = {
    logger.info(s"[PluginsSbtCodegen]: start 'generate' for ${serviceShape.getId.getName}")

    // TODO config
    // TODO mustache
    val content =
      s"""
         |addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.6")
         |addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.17.0")
         |""".stripMargin

    writer.write(content)
  }
}