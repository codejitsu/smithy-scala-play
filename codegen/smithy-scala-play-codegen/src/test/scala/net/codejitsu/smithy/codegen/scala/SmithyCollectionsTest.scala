package net.codejitsu.smithy.codegen.scala

import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import software.amazon.smithy.build.{MockManifest, PluginContext}
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.node.Node

class SmithyCollectionsTest {
  /*
    The expected project output:

    app
     └ net.codejitsu.smithy.generated.controllers
        └ ExampleController.scala
     └ net.codejitsu.smithy.generated.rules
        └ ExampleRules.scala
        └ ExampleRulesDefaultImpl.scala
     └ net.codejitsu.smithy.generated.models
        └ GetFooInput.scala
        └ GetFooOutput.scala
     └ net.codejitsu.smithy.generated.util
        └ ErrorHandler.scala
    build.sbt
    conf
     └ application.conf
     └ routes // GET /getfoo/:input ExampleController.getSomething(input: net.codejitsu.smithy.generated.models.GetSomethingInput)
    project
     └ build.properties
     └ plugins.sbt
    test
  */

  // TODO check if operation doesn't have http trait
  // TODO check if operation doesn't have input or output
  // TODO lists with complex types (not primitives)

  @Test
  def testGenerateLists(): Unit = {
    val model = Model
      .assembler()
      .addImport(getClass.getResource("lists.smithy"))
      .assemble()
      .unwrap()

    val manifest = new MockManifest()

    val pluginContext = PluginContext
      .builder()
      .model(model)
      .fileManifest(manifest)
      .settings(Node.objectNodeBuilder()
        .withMember("service", Node.from("net.codejitsu.smithy#Example"))
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
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/models/GetFooInput.scala"))
    val contentInput = manifest.getFileString("/app/net/codejitsu/smithy/generated/models/GetFooInput.scala").get()

    val expectedInput =
      """|package net.codejitsu.smithy.generated.models
         |
         |import play.api.libs.json.{Json, OWrites}
         |import play.api.mvc.PathBindable
         |
         |case class GetFooInput(
         |    id: Int
         |)
         |
         |object GetFooInput {
         |    implicit val getFooInputWrites: OWrites[GetFooInput] = Json.writes[GetFooInput]
         |
         |    implicit def pathBinder(implicit binder: PathBindable[Int]): PathBindable[GetFooInput] = new PathBindable[GetFooInput] {
         |        override def bind(key: String, value: String): Either[String, GetFooInput] = {
         |            for {
         |                field <- binder.bind(key, value)
         |            } yield GetFooInput(field)
         |        }
         |
         |        override def unbind(key: String, getFooInput: GetFooInput): String = {
         |            getFooInput.id.toString
         |        }
         |    }
         |}
         |""".stripMargin

    assertEquals(expectedInput.replaceAll("\\s+",""), contentInput.replaceAll("\\s+",""))

    // output shape
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/models/GetFooOutput.scala"))
    val contentOutput = manifest.getFileString("/app/net/codejitsu/smithy/generated/models/GetFooOutput.scala").get()

    val expectedOutput =
      """|package net.codejitsu.smithy.generated.models
         |
         |import play.api.libs.json.{Json, OWrites}
         |
         |case class GetFooOutput(
         |    foo: Seq[String]
         |)
         |
         |object GetFooOutput {
         |    implicit val getFooOutputWrites: OWrites[GetFooOutput] = Json.writes[GetFooOutput]
         |}
         |""".stripMargin

    assertEquals(expectedOutput, contentOutput)

    // rules trait
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/rules/ExampleRules.scala"))
    val contentBaseRules = manifest.getFileString("/app/net/codejitsu/smithy/generated/rules/ExampleRules.scala").get()

    val expectedBaseRules =
      """|package net.codejitsu.smithy.generated.rules
         |
         |import com.google.inject.ImplementedBy
         |import net.codejitsu.smithy.generated.models.{GetFooInput, GetFooOutput}
         |
         |@ImplementedBy(classOf[ExampleRulesDefaultImpl])
         |trait ExampleRules {
         |    def getFoo(getFooInput: GetFooInput): GetFooOutput
         |}
         |
         |""".stripMargin

    assertEquals(expectedBaseRules.replaceAll("\\s+",""), contentBaseRules.replaceAll("\\s+",""))

    // default implementation
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/rules/ExampleRulesDefaultImpl.scala"))
    val contentDefaultRules = manifest.getFileString("/app/net/codejitsu/smithy/generated/rules/ExampleRulesDefaultImpl.scala").get()

    val expectedDefaultRules =
      """|package net.codejitsu.smithy.generated.rules
         |
         |import net.codejitsu.smithy.generated.models.{GetFooInput, GetFooOutput}
         |
         |import javax.inject.Singleton
         |
         |@Singleton
         |class ExampleRulesDefaultImpl extends ExampleRules {
         |    override def getFoo(getFooInput: GetFooInput): GetFooOutput = {
         |        ???
         |    }
         |}
         |
         |""".stripMargin

    assertEquals(expectedDefaultRules.replaceAll("\\s+",""), contentDefaultRules.replaceAll("\\s+",""))

    // controller
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/controllers/ExampleController.scala"))
    val contentController = manifest.getFileString("/app/net/codejitsu/smithy/generated/controllers/ExampleController.scala").get()

    val expectedController =
      """|package net.codejitsu.smithy.generated.controllers
         |
         |import net.codejitsu.smithy.generated.models.{GetFooInput, GetFooOutput}
         |import net.codejitsu.smithy.generated.rules.ExampleRules
         |import play.api.mvc._
         |import javax.inject._
         |import play.api.libs.json.Json
         |
         |import net.codejitsu.smithy.generated.models.GetFooOutput.getFooOutputWrites
         |
         |@Singleton
         |class ExampleController @Inject()(val controllerComponents: ControllerComponents, val exampleRules: ExampleRules) extends BaseController {
         |    def getFoo(getFooInput: GetFooInput): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
         |        val result = exampleRules.getFoo(getFooInput)
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
      """|name := "Example"
         |organization := "Example"
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
      """|GET      /getfoo/:id      net.codejitsu.smithy.generated.controllers.ExampleController.getFoo(id: net.codejitsu.smithy.generated.models.GetFooInput)
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

  @Test
  def testGenerateMaps(): Unit = {
    val model = Model
      .assembler()
      .addImport(getClass.getResource("maps.smithy"))
      .assemble()
      .unwrap()

    val manifest = new MockManifest()

    val pluginContext = PluginContext
      .builder()
      .model(model)
      .fileManifest(manifest)
      .settings(Node.objectNodeBuilder()
        .withMember("service", Node.from("net.codejitsu.smithy#Example"))
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
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/models/GetFooInput.scala"))
    val contentInput = manifest.getFileString("/app/net/codejitsu/smithy/generated/models/GetFooInput.scala").get()

    val expectedInput =
      """|package net.codejitsu.smithy.generated.models
         |
         |import play.api.libs.json.{Json, OWrites}
         |import play.api.mvc.PathBindable
         |
         |case class GetFooInput(
         |    id: Int
         |)
         |
         |object GetFooInput {
         |    implicit val getFooInputWrites: OWrites[GetFooInput] = Json.writes[GetFooInput]
         |
         |    implicit def pathBinder(implicit binder: PathBindable[Int]): PathBindable[GetFooInput] = new PathBindable[GetFooInput] {
         |        override def bind(key: String, value: String): Either[String, GetFooInput] = {
         |            for {
         |                field <- binder.bind(key, value)
         |            } yield GetFooInput(field)
         |        }
         |
         |        override def unbind(key: String, getFooInput: GetFooInput): String = {
         |            getFooInput.id.toString
         |        }
         |    }
         |}
         |""".stripMargin

    assertEquals(expectedInput.replaceAll("\\s+",""), contentInput.replaceAll("\\s+",""))

    // output shape
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/models/GetFooOutput.scala"))
    val contentOutput = manifest.getFileString("/app/net/codejitsu/smithy/generated/models/GetFooOutput.scala").get()

    val expectedOutput =
      """|package net.codejitsu.smithy.generated.models
         |
         |import play.api.libs.json.{Json, OWrites}
         |
         |case class GetFooOutput(
         |    foo: Map[String, Int]
         |)
         |
         |object GetFooOutput {
         |    implicit val getFooOutputWrites: OWrites[GetFooOutput] = Json.writes[GetFooOutput]
         |}
         |""".stripMargin

    assertEquals(expectedOutput, contentOutput)

    // rules trait
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/rules/ExampleRules.scala"))
    val contentBaseRules = manifest.getFileString("/app/net/codejitsu/smithy/generated/rules/ExampleRules.scala").get()

    val expectedBaseRules =
      """|package net.codejitsu.smithy.generated.rules
         |
         |import com.google.inject.ImplementedBy
         |import net.codejitsu.smithy.generated.models.{GetFooInput, GetFooOutput}
         |
         |@ImplementedBy(classOf[ExampleRulesDefaultImpl])
         |trait ExampleRules {
         |    def getFoo(getFooInput: GetFooInput): GetFooOutput
         |}
         |
         |""".stripMargin

    assertEquals(expectedBaseRules.replaceAll("\\s+",""), contentBaseRules.replaceAll("\\s+",""))

    // default implementation
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/rules/ExampleRulesDefaultImpl.scala"))
    val contentDefaultRules = manifest.getFileString("/app/net/codejitsu/smithy/generated/rules/ExampleRulesDefaultImpl.scala").get()

    val expectedDefaultRules =
      """|package net.codejitsu.smithy.generated.rules
         |
         |import net.codejitsu.smithy.generated.models.{GetFooInput, GetFooOutput}
         |
         |import javax.inject.Singleton
         |
         |@Singleton
         |class ExampleRulesDefaultImpl extends ExampleRules {
         |    override def getFoo(getFooInput: GetFooInput): GetFooOutput = {
         |        ???
         |    }
         |}
         |
         |""".stripMargin

    assertEquals(expectedDefaultRules.replaceAll("\\s+",""), contentDefaultRules.replaceAll("\\s+",""))

    // controller
    assertTrue(manifest.hasFile("/app/net/codejitsu/smithy/generated/controllers/ExampleController.scala"))
    val contentController = manifest.getFileString("/app/net/codejitsu/smithy/generated/controllers/ExampleController.scala").get()

    val expectedController =
      """|package net.codejitsu.smithy.generated.controllers
         |
         |import net.codejitsu.smithy.generated.models.{GetFooInput, GetFooOutput}
         |import net.codejitsu.smithy.generated.rules.ExampleRules
         |import play.api.mvc._
         |import javax.inject._
         |import play.api.libs.json.Json
         |
         |import net.codejitsu.smithy.generated.models.GetFooOutput.getFooOutputWrites
         |
         |@Singleton
         |class ExampleController @Inject()(val controllerComponents: ControllerComponents, val exampleRules: ExampleRules) extends BaseController {
         |    def getFoo(getFooInput: GetFooInput): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
         |        val result = exampleRules.getFoo(getFooInput)
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
      """|name := "Example"
         |organization := "Example"
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
      """|GET      /getfoo/:id      net.codejitsu.smithy.generated.controllers.ExampleController.getFoo(id: net.codejitsu.smithy.generated.models.GetFooInput)
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
