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
         |    def getPokemon(getPokemonInput: GetPokemonInput): GetPokemonOutput
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
         |    override def getPokemon(getPokemonInput: GetPokemonInput): GetPokemonOutput = {
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
         |class PokemonServiceController @Inject()(val controllerComponents: ControllerComponents, val pokemonServiceRules: PokemonServiceRules) extends BaseController {
         |    def getPokemon(getPokemonInput: GetPokemonInput): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
         |        val result = pokemonServiceRules.getPokemon(getPokemonInput)
         |        Ok(Json.toJson(result))
         |    }
         |}
         |
         |""".stripMargin

    assertEquals(expectedController.replaceAll("\\s+",""), contentController.replaceAll("\\s+",""))

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

    // build.properties
    assertTrue(manifest.hasFile("/project/build.properties"))
    val contentBuildProperties = manifest.getFileString("/project/build.properties").get()

    val expectedBuildProperties =
      """|sbt.version=1.10.6
         |""".stripMargin

    assertEquals(expectedBuildProperties.replaceAll("\\s+",""), contentBuildProperties.replaceAll("\\s+",""))

    // plugins.sbt
    assertTrue(manifest.hasFile("/project/plugins.sbt"))
    val contentPluginsSbt = manifest.getFileString("/project/plugins.sbt").get()

    val expectedPluginsSbt =
      """|addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.6")
         |addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.17.0")
         |""".stripMargin

    assertEquals(expectedPluginsSbt.replaceAll("\\s+",""), contentPluginsSbt.replaceAll("\\s+",""))
  }

  /*
    The expected project output:

    app
     └ net.codejitsu.smithy.generated.controllers
        └ PrimitivesServiceController.scala
     └ net.codejitsu.smithy.generated.rules
        └ PrimitivesServiceRules.scala
        └ PrimitivesServiceRulesDefaultImpl.scala
     └ net.codejitsu.smithy.generated.models
        └ GetSomethingInput.scala
        └ GetSomethingOutput.scala
     └ net.codejitsu.smithy.generated.util
        └ ErrorHandler.scala
    build.sbt
    conf
     └ application.conf
     └ routes // GET /primitives/:input PrimitivesServiceController.getSomething(input: net.codejitsu.smithy.generated.models.GetSomethingInput)
    project
     └ build.properties
     └ plugins.sbt
    test
  */

  @Test
  def testGeneratePrimitives(): Unit = {
    val model = Model
      .assembler()
      .addImport(getClass.getResource("primitives.smithy"))
      .assemble()
      .unwrap()

    val manifest = new MockManifest()

    val pluginContext = PluginContext
      .builder()
      .model(model)
      .fileManifest(manifest)
      .settings(Node.objectNodeBuilder()
        .withMember("service", Node.from("net.codejitsu.smithy#PrimitivesService"))
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
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/models/GetSomethingInput.scala"))
    val contentInput = manifest.getFileString("/app/net/codejitsu/smithy/generated/models/GetSomethingInput.scala").get()

    val expectedInput =
      """|package net.codejitsu.smithy.generated.models
         |
         |import play.api.libs.json.{Json, OWrites}
         |import play.api.mvc.PathBindable
         |
         |case class GetSomethingInput(
         |    id: Int
         |)
         |
         |object GetSomethingInput {
         |    implicit val getSomethingInputWrites: OWrites[GetSomethingInput] = Json.writes[GetSomethingInput]
         |
         |    implicit def pathBinder(implicit binder: PathBindable[Int]): PathBindable[GetSomethingInput] = new PathBindable[GetSomethingInput] {
         |        override def bind(key: String, value: String): Either[String, GetSomethingInput] = {
         |            for {
         |                field <- binder.bind(key, value)
         |            } yield GetSomethingInput(field)
         |        }
         |
         |        override def unbind(key: String, getSomethingInput: GetSomethingInput): String = {
         |            getSomethingInput.id.toString
         |        }
         |    }
         |}
         |""".stripMargin

    assertEquals(expectedInput.replaceAll("\\s+",""), contentInput.replaceAll("\\s+",""))

    // output shape
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/models/GetSomethingOutput.scala"))
    val contentOutput = manifest.getFileString("/app/net/codejitsu/smithy/generated/models/GetSomethingOutput.scala").get()

    val expectedOutput =
      """|package net.codejitsu.smithy.generated.models
         |
         |import play.api.libs.json.{Json, OWrites}
         |
         |case class GetSomethingOutput(
         |    id: Int,
         |    age: Int,
         |    bool: Boolean,
         |    byte: Byte,
         |    short: Short,
         |    long: Long,
         |    float: Float,
         |    double: Double,
         |    bigInteger: BigInt,
         |    bigDecimal: BigDecimal,
         |    timestamp: Long
         |)
         |
         |object GetSomethingOutput {
         |    implicit val getSomethingOutputWrites: OWrites[GetSomethingOutput] = Json.writes[GetSomethingOutput]
         |}
         |""".stripMargin

    assertEquals(expectedOutput, contentOutput)

    // rules trait
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/rules/PrimitivesServiceRules.scala"))
    val contentBaseRules = manifest.getFileString("/app/net/codejitsu/smithy/generated/rules/PrimitivesServiceRules.scala").get()

    val expectedBaseRules =
      """|package net.codejitsu.smithy.generated.rules
         |
         |import com.google.inject.ImplementedBy
         |import net.codejitsu.smithy.generated.models.{GetSomethingInput, GetSomethingOutput}
         |
         |@ImplementedBy(classOf[PrimitivesServiceRulesDefaultImpl])
         |trait PrimitivesServiceRules {
         |    def getSomething(getSomethingInput: GetSomethingInput): GetSomethingOutput
         |}
         |
         |""".stripMargin

    assertEquals(expectedBaseRules.replaceAll("\\s+",""), contentBaseRules.replaceAll("\\s+",""))

    // default implementation
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/rules/PrimitivesServiceRulesDefaultImpl.scala"))
    val contentDefaultRules = manifest.getFileString("/app/net/codejitsu/smithy/generated/rules/PrimitivesServiceRulesDefaultImpl.scala").get()

    val expectedDefaultRules =
      """|package net.codejitsu.smithy.generated.rules
         |
         |import net.codejitsu.smithy.generated.models.{GetSomethingInput, GetSomethingOutput}
         |
         |import javax.inject.Singleton
         |
         |@Singleton
         |class PrimitivesServiceRulesDefaultImpl extends PrimitivesServiceRules {
         |    override def getSomething(getSomethingInput: GetSomethingInput): GetSomethingOutput = {
         |        ???
         |    }
         |}
         |
         |""".stripMargin

    assertEquals(expectedDefaultRules.replaceAll("\\s+",""), contentDefaultRules.replaceAll("\\s+",""))

    // controller
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/controllers/PrimitivesServiceController.scala"))
    val contentController = manifest.getFileString("/app/net/codejitsu/smithy/generated/controllers/PrimitivesServiceController.scala").get()

    val expectedController =
      """|package net.codejitsu.smithy.generated.controllers
         |
         |import net.codejitsu.smithy.generated.models.{GetSomethingInput, GetSomethingOutput}
         |import net.codejitsu.smithy.generated.rules.PrimitivesServiceRules
         |import play.api.mvc._
         |import javax.inject._
         |import play.api.libs.json.Json
         |
         |import net.codejitsu.smithy.generated.models.GetSomethingOutput.getSomethingOutputWrites
         |
         |@Singleton
         |class PrimitivesServiceController @Inject()(val controllerComponents: ControllerComponents, val primitivesServiceRules: PrimitivesServiceRules) extends BaseController {
         |    def getSomething(getSomethingInput: GetSomethingInput): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
         |        val result = primitivesServiceRules.getSomething(getSomethingInput)
         |        Ok(Json.toJson(result))
         |    }
         |}
         |
         |""".stripMargin

    assertEquals(expectedController.replaceAll("\\s+",""), contentController.replaceAll("\\s+",""))

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
      """|name := "PrimitivesService"
         |organization := "PrimitivesService"
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
      """|GET      /primitives/:id      net.codejitsu.smithy.generated.controllers.PrimitivesServiceController.getSomething(id: net.codejitsu.smithy.generated.models.GetSomethingInput)
         |""".stripMargin

    assertEquals(expectedRoutes.replaceAll("\\s+",""), contentRoutes.replaceAll("\\s+",""))

    // build.properties
    assertTrue(manifest.hasFile("/project/build.properties"))
    val contentBuildProperties = manifest.getFileString("/project/build.properties").get()

    val expectedBuildProperties =
      """|sbt.version=1.10.6
         |""".stripMargin

    assertEquals(expectedBuildProperties.replaceAll("\\s+",""), contentBuildProperties.replaceAll("\\s+",""))

    // plugins.sbt
    assertTrue(manifest.hasFile("/project/plugins.sbt"))
    val contentPluginsSbt = manifest.getFileString("/project/plugins.sbt").get()

    val expectedPluginsSbt =
      """|addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.6")
         |addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.17.0")
         |""".stripMargin

    assertEquals(expectedPluginsSbt.replaceAll("\\s+",""), contentPluginsSbt.replaceAll("\\s+",""))
  }
}
