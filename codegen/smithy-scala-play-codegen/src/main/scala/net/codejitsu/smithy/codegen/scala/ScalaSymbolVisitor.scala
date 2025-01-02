package net.codejitsu.smithy.codegen.scala;

import software.amazon.smithy.codegen.core.{ReservedWordSymbolProvider, ReservedWordsBuilder, Symbol, SymbolProvider}
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.{BigDecimalShape, BigIntegerShape, BlobShape, BooleanShape, ByteShape, DocumentShape, DoubleShape, FloatShape, IntegerShape, ListShape, LongShape, MapShape, MemberShape, OperationShape, ResourceShape, ServiceShape, Shape, ShapeVisitor, ShortShape, StringShape, StructureShape, TimestampShape, UnionShape}
import software.amazon.smithy.utils.StringUtils

import java.nio.file.Paths
import java.util.logging.Logger;

class ScalaSymbolVisitor(model: Model) extends ShapeVisitor[Symbol] with SymbolProvider {
  val logger: Logger = Logger.getLogger(classOf[ScalaSymbolVisitor].getName)

  private val escaper: ReservedWordSymbolProvider.Escaper = initEscaper()

  override def toSymbol(shape: Shape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'toSymbol' for ${shape.getId.getName}")

    val symbol = shape.accept(this)

    logger.info(s"[ScalaSymbolVisitor]: mapping $shape to $symbol")

    escaper.escapeSymbol(shape, symbol)
  }

  private def initEscaper(): ReservedWordSymbolProvider.Escaper = {
    val reservedWords = new ReservedWordsBuilder()
      .loadWords(classOf[ScalaSymbolVisitor].getResource("scala-reserved-words.txt"))
      .build()

    ReservedWordSymbolProvider.builder()
      .nameReservedWords(reservedWords)
      .escapePredicate((_, symbol) => !StringUtils.isEmpty(symbol.getDefinitionFile))
      .buildEscaper()
  }

  override def blobShape(shape: BlobShape): Symbol = ???

  override def booleanShape(shape: BooleanShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'booleanShape' for ${shape.getId.getName}")

    Symbol.builder()
      .putProperty("shape", shape)
      .name("Boolean")
      .build()
  }

  override def listShape(shape: ListShape): Symbol = ???

  override def mapShape(shape: MapShape): Symbol = ???

  override def byteShape(shape: ByteShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'byteShape' for ${shape.getId.getName}")

    Symbol.builder()
      .putProperty("shape", shape)
      .name("Byte") // TODO Byte vs. byte in config
      .build()
  }

  override def shortShape(shape: ShortShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'shortShape' for ${shape.getId.getName}")

    Symbol.builder()
      .putProperty("shape", shape)
      .name("Short") // TODO Byte vs. byte in config
      .build()
  }

  override def integerShape(shape: IntegerShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'integerShape' for ${shape.getId.getName}")

    Symbol.builder()
      .putProperty("shape", shape)
      .name("Int") // TODO Byte vs. byte in config
      .build()
  }

  override def longShape(shape: LongShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'longShape' for ${shape.getId.getName}")

    Symbol.builder()
      .putProperty("shape", shape)
      .name("Long") // TODO Byte vs. byte in config
      .build()
  }

  override def floatShape(shape: FloatShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'floatShape' for ${shape.getId.getName}")

    Symbol.builder()
      .putProperty("shape", shape)
      .name("Float") // TODO Byte vs. byte in config
      .build()
  }

  override def documentShape(shape: DocumentShape): Symbol = ???

  override def doubleShape(shape: DoubleShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'doubleShape' for ${shape.getId.getName}")

    Symbol.builder()
      .putProperty("shape", shape)
      .name("Double") // TODO Byte vs. byte in config
      .build()
  }

  override def bigIntegerShape(shape: BigIntegerShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'bigIntegerShape' for ${shape.getId.getName}")

    Symbol.builder()
      .putProperty("shape", shape)
      .name("BigInt")
      .build()
  }

  override def bigDecimalShape(shape: BigDecimalShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'bigDecimalShape' for ${shape.getId.getName}")

    Symbol.builder()
      .putProperty("shape", shape)
      .name("BigDecimal")
      .build()
  }

  override def operationShape(shape: OperationShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'operationShape' for ${shape.getId.getName}")

    val actionName = shape.getId.getName + "Action"

    val symbolAction = Symbol.builder()
      .putProperty("shape", shape)
      .name(actionName)
      .namespace(shape.getId.getNamespace, ".")
      .build()

    val builder = symbolAction.toBuilder
    builder.putProperty("inputType", symbolAction.toBuilder.name(actionName + "Input").build())
    builder.putProperty("outputType", symbolAction.toBuilder.name(actionName + "Output").build())

    builder.build()
  }

  override def resourceShape(shape: ResourceShape): Symbol = ???

  override def serviceShape(shape: ServiceShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'serviceShape' for ${shape.getId.getName}")

    val controller = shape.getId.getName + "Controller"

    Symbol.builder()
      .putProperty("shape", shape)
      .name(controller)
      .namespace(shape.getId.getNamespace, ".")
      .build()
  }

  override def stringShape(shape: StringShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'stringShape' for ${shape.getId.getName}")

    Symbol.builder()
      .putProperty("shape", shape)
      .name("String")
      .build()
  }

  override def structureShape(shape: StructureShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'structureShape' for ${shape.getId.getName}")

    val namespace = Array("app") ++ shape.getId.getNamespace.split("\\.") ++ Array("generated", "models", s"${shape.getId.getName}.scala")
    val pathToFile = Paths.get(".", namespace: _*).toString // TODO this belongs to config (project structure)

    Symbol.builder()
      .putProperty("shape", shape)
      .name(shape.getId.getName)
      .namespace(shape.getId.getNamespace, ".")
      .definitionFile(pathToFile)
      .build()
  }

  override def unionShape(shape: UnionShape): Symbol = ???

  override def memberShape(shape: MemberShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'memberShape' for ${shape.getId.getName}")

    val target = model.getShape(shape.getTarget).get // TODO add exception

    val targetSymbol = toSymbol(target)

    targetSymbol
  }

  override def timestampShape(shape: TimestampShape): Symbol = {
    logger.info(s"[ScalaSymbolVisitor]: start 'timestampShape' for ${shape.getId.getName}")

    Symbol.builder()
      .putProperty("shape", shape)
      .name("Long") // TODO config different options: long vs. instant
      .build()
  }
}
