package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.ScalaPlayWriter
import software.amazon.smithy.codegen.core.SymbolProvider
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.ServiceShape

import java.util.logging.Logger
import scala.jdk.CollectionConverters.CollectionHasAsScala

class SbtCodegen(
  val serviceShape: ServiceShape,
  val symbolProvider: SymbolProvider,
  val writer: ScalaPlayWriter,
  val model: Model) {
  val logger: Logger = Logger.getLogger(classOf[SbtCodegen].getName)

  def generateSbt(): Unit = {
    logger.info(s"[SbtCodegen]: start 'generate' for ${serviceShape.getId.getName}")

    val service = symbolProvider.toSymbol(model.getServiceShapes.asScala.head)

    // TODO config
    // TODO mustache
    val content =
      s"""
         |name := "${service.getName}"
         |organization := "${service.getName}"
         |
         |version := "1.0-SNAPSHOT"
         |
         |lazy val root = (project in file(".")).enablePlugins(PlayScala)
         |
         |scalaVersion := "2.13.15"
         |
         |libraryDependencies += guice
         |libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0" % Test
         |""".stripMargin

    writer.write(content)
  }
}
