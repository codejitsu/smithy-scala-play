package net.codejitsu.smithy.codegen.scala

import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import software.amazon.smithy.build.{MockManifest, PluginContext}
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.node.Node

class SmithyScalaPlayCodegenPluginTest {
  /*
    The expected project output:

    app
     └ controllers
        └ PokemonServiceController.scala
     └ rules
        └ PokemonServiceRules.scala
     └ models
        └ GetPokemonInput.scala
        └ GetPokemonOutput.scala
    build.sbt
    conf
     └ application.conf
     └ routes // GET /pokemons/:name PokemonServiceController.getPokemon(name: String)
    project
     └ build.properties
     └ plugins.sbt
    test
  */

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
      .settings(Node.objectNodeBuilder()
        .withMember("service", Node.from("net.codejitsu.smithy.codegen.scala#PokemonService"))
        .withMember("package", Node.from("net.codejitsu.smithy.codegen.scala"))
        .withMember("packageVersion", Node.from("1.0.0"))
        .build())
      .build()

    try {
      new SmithyScalaPlayCodegenPlugin().execute(pluginContext)
    } catch {
      case e: Exception => println(e.getMessage)
    }

    assertTrue(manifest.hasFile("/src/main/scala/net/codejitsu/smithy/codegen/scala/GetPokemonInput.scala"))
    val content = manifest.getFileString("/src/main/scala/net/codejitsu/smithy/codegen/scala/GetPokemonInput.scala").get()

    // TODO add imports
    val expected =
      """|case class GetPokemonInput (
         |    name: String
         |)
         |
         |""".stripMargin

    assertEquals(expected, content)
  }
}
