package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.build.{PluginContext, SmithyBuildPlugin}
import software.amazon.smithy.codegen.core.directed.CodegenDirector

class SmithyScalaPlayCodegenPlugin extends SmithyBuildPlugin {
  override def getName: String = "scala-play-server-codegen"

  override def execute(pluginContext: PluginContext): Unit = {
    val codegenDirector = new CodegenDirector[ScalaPlayWriter, ScalaPlayIntegration, ScalaPlayContext, ScalaPlaySettings]

    codegenDirector.directedCodegen(new ScalaPlayGenerator())

    codegenDirector.integrationClass(classOf[ScalaPlayIntegration])

    codegenDirector.fileManifest(pluginContext.getFileManifest)
    codegenDirector.model(pluginContext.getModel)

    val settings = codegenDirector.settings(classOf[ScalaPlaySettings], pluginContext.getSettings)

    codegenDirector.service(settings.service)

    codegenDirector.performDefaultCodegenTransforms()

    codegenDirector.createDedicatedInputsAndOutputs()

    codegenDirector.run()
  }
}
