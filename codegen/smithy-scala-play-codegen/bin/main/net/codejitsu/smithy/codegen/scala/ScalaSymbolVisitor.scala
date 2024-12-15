package net.codejitsu.smithy.codegen.scala;

import software.amazon.smithy.codegen.core.{ReservedWordSymbolProvider, ReservedWordsBuilder, Symbol, SymbolProvider}
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.{BigDecimalShape, BigIntegerShape, BlobShape, BooleanShape, ByteShape, DocumentShape, DoubleShape, FloatShape, IntegerShape, ListShape, LongShape, MapShape, MemberShape, OperationShape, ResourceShape, ServiceShape, Shape, ShapeVisitor, ShortShape, StringShape, StructureShape, TimestampShape, UnionShape}
import software.amazon.smithy.utils.StringUtils

import java.util.logging.Logger;

class ScalaSymbolVisitor(model: Model, settings: ScalaPlaySettings) extends ShapeVisitor[Symbol] with SymbolProvider {
  val logger = Logger.getLogger(classOf[ScalaSymbolVisitor].getName)

  val escaper: ReservedWordSymbolProvider.Escaper = initEscaper()

  override def toSymbol(shape: Shape): Symbol = {
    val symbol = shape.accept(this)

    logger.info(s"Mapping $shape to $symbol")

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

  override def booleanShape(shape: BooleanShape): Symbol = ???

  override def listShape(shape: ListShape): Symbol = ???

  override def mapShape(shape: MapShape): Symbol = ???

  override def byteShape(shape: ByteShape): Symbol = ???

  override def shortShape(shape: ShortShape): Symbol = ???

  override def integerShape(shape: IntegerShape): Symbol = ???

  override def longShape(shape: LongShape): Symbol = ???

  override def floatShape(shape: FloatShape): Symbol = ???

  override def documentShape(shape: DocumentShape): Symbol = ???

  override def doubleShape(shape: DoubleShape): Symbol = ???

  override def bigIntegerShape(shape: BigIntegerShape): Symbol = ???

  override def bigDecimalShape(shape: BigDecimalShape): Symbol = ???

  override def operationShape(shape: OperationShape): Symbol = {
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
    val controller = shape.getId.getName + "Controller"

    Symbol.builder()
      .putProperty("shape", shape)
      .name(controller)
      .namespace(shape.getId.getNamespace, ".")
      .build()
  }

  override def stringShape(shape: StringShape): Symbol = {
    Symbol.builder()
      .putProperty("shape", shape)
      .name("String")
      .build()
  }

  override def structureShape(shape: StructureShape): Symbol = {
    val namespace = shape.getId.getNamespace.split("\\.").mkString("/")
    val pathToFile = s"./src/main/scala/$namespace" // TODO this belongs to config (project structure)

    Symbol.builder()
      .putProperty("shape", shape)
      .name(shape.getId.getName)
      .namespace(shape.getId.getNamespace, ".")
      .definitionFile(s"$pathToFile/${shape.getId.getName}.scala")
      .build()
  }

  override def unionShape(shape: UnionShape): Symbol = ???

  override def memberShape(shape: MemberShape): Symbol = {
    val target = model.getShape(shape.getTarget).get // TODO add exception

    val targetSymbol = toSymbol(target)

    targetSymbol
  }

  override def timestampShape(shape: TimestampShape): Symbol = ???
}
