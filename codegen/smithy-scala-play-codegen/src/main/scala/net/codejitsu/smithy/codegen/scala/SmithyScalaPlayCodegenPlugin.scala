package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.build.{PluginContext, SmithyBuildPlugin}
import software.amazon.smithy.codegen.core.directed.CodegenDirector

import java.util.logging.Logger

class SmithyScalaPlayCodegenPlugin extends SmithyBuildPlugin {
  val logger = Logger.getLogger(classOf[SmithyScalaPlayCodegenPlugin].getName)

  override def getName: String = "scala-play-server-codegen"

  override def execute(pluginContext: PluginContext): Unit = {
    logger.info("[SmithyScalaPlayCodegenPlugin]: start 'execute'")

    val codegenDirector = new CodegenDirector[ScalaPlayWriter, ScalaPlayIntegration, ScalaPlayContext, ScalaPlaySettings]
    codegenDirector.directedCodegen(new ScalaPlayGenerator())

    codegenDirector.integrationClass(classOf[ScalaPlayIntegration])

    codegenDirector.fileManifest(pluginContext.getFileManifest)
    codegenDirector.model(pluginContext.getModel)

    val settings = ScalaPlaySettings.mkSettings(pluginContext.getSettings, pluginContext.getModel)

    codegenDirector.settings(settings)

    codegenDirector.service(settings.service)

    codegenDirector.performDefaultCodegenTransforms()

    codegenDirector.createDedicatedInputsAndOutputs()

    codegenDirector.run()
  }
}
