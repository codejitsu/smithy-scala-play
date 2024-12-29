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
     └ net.codejitsu.smithy.generated.util
        └ ErrorHandler.scala
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

    // rules trait
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/rules/PokemonServiceRules.scala"))
    val contentBaseRules = manifest.getFileString("/app/net/codejitsu/smithy/generated/rules/PokemonServiceRules.scala").get()

    val expectedBaseRules =
      """|package net.codejitsu.smithy.generated.rules
         |
         |import com.google.inject.ImplementedBy
         |import net.codejitsu.smithy.generated.models.{GetPokemonInput, GetPokemonOutput}
         |
         |@ImplementedBy(classOf[PokemonServiceRulesDefaultImpl])
         |trait PokemonServiceRules {
         |    def getPokemon(getPokemonInput GetPokemonInput): GetPokemonOutput
         |}
         |
         |""".stripMargin

    assertEquals(expectedBaseRules.replaceAll("\\s+",""), contentBaseRules.replaceAll("\\s+",""))

    // default implementation
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/rules/PokemonServiceRulesDefaultImpl.scala"))
    val contentDefaultRules = manifest.getFileString("/app/net/codejitsu/smithy/generated/rules/PokemonServiceRulesDefaultImpl.scala").get()

    val expectedDefaultRules =
      """|package net.codejitsu.smithy.generated.rules
         |
         |import net.codejitsu.smithy.generated.models.{GetPokemonInput, GetPokemonOutput}
         |
         |import javax.inject.Singleton
         |
         |@Singleton
         |class PokemonServiceRulesDefaultImpl extends PokemonServiceRules {
         |    override def getPokemon(getPokemonInput GetPokemonInput): GetPokemonOutput = {
         |        ???
         |    }
         |}
         |
         |""".stripMargin

    assertEquals(expectedDefaultRules.replaceAll("\\s+",""), contentDefaultRules.replaceAll("\\s+",""))

    // controller
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/controllers/PokemonServiceController.scala"))
    val contentController = manifest.getFileString("/app/net/codejitsu/smithy/generated/controllers/PokemonServiceController.scala").get()

    val expectedController =
      """|package net.codejitsu.smithy.generated.controllers
         |
         |import net.codejitsu.smithy.generated.models.{GetPokemonInput, GetPokemonOutput}
         |import net.codejitsu.smithy.generated.rules.PokemonServiceRules
         |import play.api.mvc._
         |import javax.inject._
         |import play.api.libs.json.Json
         |
         |import net.codejitsu.smithy.generated.models.GetPokemonOutput.getPokemonOutputWrites
         |
         |@Singleton
         |class PokemonServiceController @Inject()(val controllerComponents: ControllerComponents, val pokemonServiceRules PokemonServiceRules) extends BaseController {
         |    def getPokemon(getPokemonInput GetPokemonInput): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
         |        val result = pokemonServiceRules.getPokemon(getPokemonInput)
         |        Ok(Json.toJson(result))
         |    }
         |}
         |
         |""".stripMargin

    // default error handler
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/util/ErrorHandler.scala"))
    val contentHandler = manifest.getFileString("/app/net/codejitsu/smithy/generated/util/ErrorHandler.scala").get()

    val expectedHandler =
      """|package net.codejitsu.smithy.generated.util
         |
         |import javax.inject._
         |
         |import scala.concurrent._
         |
         |import play.api._
         |import play.api.http.DefaultHttpErrorHandler
         |import play.api.mvc._
         |import play.api.mvc.Results._
         |import play.api.routing.Router
         |
         |@Singleton
         |class ErrorHandler @Inject() (
         |     env: Environment,
         |     config: Configuration,
         |     sourceMapper: OptionalSourceMapper,
         |     router: Provider[Router]
         |   ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {
         |  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
         |    Future.successful(
         |      NotImplemented
         |    )
         |  }
         |}
         |
         |""".stripMargin

    assertEquals(expectedHandler.replaceAll("\\s+",""), contentHandler.replaceAll("\\s+",""))

    // sbt
    assertTrue(manifest.hasFile("/build.sbt"))
    val contentSbt = manifest.getFileString("/build.sbt").get()

    val expectedSbt =
      """|name := "PokemonService"
         |organization := "PokemonService"
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

    assertEquals(expectedSbt.replaceAll("\\s+",""), contentSbt.replaceAll("\\s+",""))

    // configuration
    assertTrue(manifest.hasFile("/conf/application.conf"))
    val contentConf = manifest.getFileString("/conf/application.conf").get()

    val expectedConf =
      """|play.http.errorHandler = "net.codejitsu.smithy.generated.util.ErrorHandler"
         |""".stripMargin

    assertEquals(expectedConf.replaceAll("\\s+",""), contentConf.replaceAll("\\s+",""))

    // routes
    assertTrue(manifest.hasFile("/conf/routes"))
    val contentRoutes = manifest.getFileString("/conf/routes").get()

    val expectedRoutes =
      """|GET      /pokemons/:name      net.codejitsu.smithy.generated.controllers.PokemonServiceController.getPokemon(name: net.codejitsu.smithy.generated.models.GetPokemonInput)
         |""".stripMargin

    assertEquals(expectedRoutes.replaceAll("\\s+",""), contentRoutes.replaceAll("\\s+",""))
  }
}
