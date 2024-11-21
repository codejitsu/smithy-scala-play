package net.codejitsu.smithy.codegen.scala

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import software.amazon.smithy.build.{MockManifest, PluginContext}
import software.amazon.smithy.model.Model

class SmithyScalaPlayCodegenPluginTest {
  @Test
  def testGenerateSimpleService(): Unit = {
    val model = Model
      .assembler()
      .addImport(getClass.getResource("simple-service.smithy"))
      .assemble()
      .unwrap()

    val manifest = new MockManifest()

    val pluginContext = PluginContext
      .builder()
      .model(model)
      .fileManifest(manifest)
      .build()

    new SmithyScalaPlayCodegenPlugin().execute(pluginContext)

    //TODO assertTrue(manifest.hasFile("GetPokemonInput.scala"))
  }
}
