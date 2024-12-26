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
     └ net.codejitsu.smithy.generated.controllers
        └ PokemonServiceController.scala
     └ net.codejitsu.smithy.generated.rules
        └ PokemonServiceRules.scala
        └ PokemonServiceRulesDefaultImpl.scala
     └ net.codejitsu.smithy.generated.models
        └ GetPokemonInput.scala
        └ GetPokemonOutput.scala
    build.sbt
    conf
     └ application.conf
     └ routes // GET /pokemons/:input PokemonServiceController.getPokemon(input: net.codejitsu.smithy.generated.models.GetPokemonInput)
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
        .withMember("service", Node.from("net.codejitsu.smithy#PokemonService"))
        .withMember("package", Node.from("net.codejitsu.smithy"))
        .withMember("packageVersion", Node.from("1.0.0"))
        .build())
      .build()

    try {
      new SmithyScalaPlayCodegenPlugin().execute(pluginContext)
    } catch {
      case e: Exception => println(e.getMessage)
    }

    // input shape
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/models/GetPokemonInput.scala"))
    val contentInput = manifest.getFileString("/app/net/codejitsu/smithy/generated/models/GetPokemonInput.scala").get()

    val expectedInput =
     """|package net.codejitsu.smithy.generated.models
        |
        |import play.api.libs.json.{Json, OWrites}
        |import play.api.mvc.PathBindable
        |
        |case class GetPokemonInput(
        |    name: String
        |)
        |
        |object GetPokemonInput {
        |    implicit val getPokemonInputWrites: OWrites[GetPokemonInput] = Json.writes[GetPokemonInput]
        |
        |    implicit def pathBinder(implicit binder: PathBindable[String]): PathBindable[GetPokemonInput] = new PathBindable[GetPokemonInput] {
        |        override def bind(key: String, value: String): Either[String, GetPokemonInput] = {
        |            for {
        |                field <- binder.bind(key, value)
        |            } yield GetPokemonInput(field)
        |        }
        |
        |        override def unbind(key: String, getPokemonInput: GetPokemonInput): String = {
        |            getPokemonInput.name
        |        }
        |    }
        |}
        |""".stripMargin

    assertEquals(expectedInput.replaceAll("\\s+",""), contentInput.replaceAll("\\s+",""))

    // output shape
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/models/GetPokemonOutput.scala"))
    val contentOutput = manifest.getFileString("/app/net/codejitsu/smithy/generated/models/GetPokemonOutput.scala").get()

    val expectedOutput =
      """|package net.codejitsu.smithy.generated.models
         |
         |import play.api.libs.json.{Json, OWrites}
         |
         |case class GetPokemonOutput(
         |    name: String,
         |    age: Int
         |)
         |
         |object GetPokemonOutput {
         |    implicit val getPokemonOutputWrites: OWrites[GetPokemonOutput] = Json.writes[GetPokemonOutput]
         |}
         |""".stripMargin

    assertEquals(expectedOutput, contentOutput)
  }
}
