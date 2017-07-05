package bindgen

import scalanative.native._

//see: http://bastian.rieck.ru/blog/posts/2015/baby_steps_libclang_ast/


case class Args(
  files    : Seq[String] = Seq(),
  chdir    : String      = ".",
  pkg      : String      = "scalanative.native.bindings",
  recursive: Boolean     = false,
  debug    : Boolean     = false)

object CLI {
  //TODO: allow Clang args to be passed after a "--"
  val parser = new scopt.OptionParser[Args]("bindgen") {
    head("bindgen", "0.1")
   
    arg[String]("files...").unbounded().optional()
      .action( (x, c) => c.copy(files = c.files :+ x) )
      .text("""Header file(s) to be converted to Scala.""")

    opt[String]('C', "chdir").valueName("DIR")
      .action( (x, c) => c.copy(chdir = x) )
      .text("""Change to DIR before performing any operations.""")

    opt[String]('P', "package").valueName("PACKAGE")
      .action( (x, c) => c.copy(pkg = x) )
      .text("""Package name.""")

    opt[Unit]('r', "recursive")
      .action( (_, c) => c.copy(recursive = true) )
      .text("""Produces bindigs for #include(s) recursively.""")

    opt[Unit]('d', "debug")
      .action( (_, c) => c.copy(debug = true) )
      .text("""Emit Clang AST to stderr.""")

    help("help")
      .text("""prints this usage text""")
  }
}


object Main {

  def main(args: Array[String]): Unit = {
    val errno: Int =
      CLI.parser.parse(args, Args()) match {
        case Some(a) => val g = new Generator(a); g.process
        case None       => -1 // arguments are bad, error message will have been displayed
      }
    System.exit(errno)
  }
}


class Generator(c: Args) {
  import scala.scalanative.bindings.clang._
  import scala.scalanative.bindings.clang.api._
  import scala.collection.mutable.ListBuffer

  private val tree = new Object with Tree
  private val visitor = AST.visitor

  def process: Int = Zone { implicit z =>
    val clang_argc: CInt = 0
    val clang_argv: Ptr[CString] = null //TODO: c.clang_args.zipWithIndex.foreach { case (p, i) => clang_argv(i) = p }

    val index: CXIndex = createIndex(0, 1)
    val xs =
      c.files.map { name =>
        if(c.debug) println(name)
        val tu: CXTranslationUnit =
          parseTranslationUnit(
            index,
            toCString(name),
            clang_argv, clang_argc,
            null, 0,
            CXTranslationUnit_SkipFunctionBodies)
        assert(tu != null) //XXX: This causes link error
        if(tu == null) println("   assert(tu != null)")
        if (tu == null) -1
        else {
          val root: CXCursor = getTranslationUnitCursor( tu )
          if(root == null) println("   assert(root != null)")
          if(c.debug) println("[about to call visitChildren]")
          val result = visitChildren(root, visitor, tree.cast[Data])
          println(s"   result=${result}")
          if(result != CXChildVisit_Continue) println("   assert(result == CXChildVisit_Continue)")
          disposeTranslationUnit( tu )
          result.toInt
        }
      }
    if(index != null) disposeIndex( index )

    println("----------------------------------------------")
    println(s"typedefs.size  = ${tree.typedefs.size}")
    println(s"enums.size     = ${tree.enums.size}")
    println(s"functions.size = ${tree.functions.size}")
    println("----------------------------------------------")
     
    tree.typedefs.foreach { entry =>
      println(s"type ${entry.name} = ${entry.underlying}")
    }
     
    tree.enums.foreach { entry =>
      println(s"object ${entry.name}_Enum {")
      print(entry.values.map(enum => s"   ${enum.name} = ${enum.value}").mkString(",\n"))
      println("}")
    }
     
    tree.functions.foreach { entry =>
      println(s"def ${entry.name}(")
      print(entry.args.map(param => s"    ${param.name}:${param.tpe}").mkString(",\n"))
      println(s"  ): ${entry.returnType} = extern")
    }

    0 //TODO: obtain from iterator
  }
}


object AST {
  import scala.collection.mutable.ListBuffer
  import scala.scalanative.bindings.clang._
  import scala.scalanative.bindings.clang.api._

  def visitor: Visitor = (cursor: CXCursor, parent: CXCursor, data: Data) => {

    val tree                 = data.cast[Tree]
    val kind: CXCursorKind   = getCursorKind(cursor)
     
    //XXX insert some fake data
    tree.typedefs  += Typedef("data", "Array[Byte]") //XXX
    tree.enums     += Enum("weekend", List(Enum.Value("Sat", 7L), Enum.Value("Sun", 7L))) //XXX
    tree.functions += Function("f", "Int", List(Function.Param("index", "Int"))) //XXX
    //XXX should crash if this function is being called
    val n = 0
    val crash = 1 / n
     
    if (kind == CXCursor_FunctionDecl) {
      val name               = getCursorSpelling(cursor)
      val cursorType         = getCursorType(cursor)
      val returnType         = getResultType(cursorType)
      val returnTypeSpelling = getTypeSpelling(returnType)
      val argc               = Cursor_getNumArguments(cursor)
     
      tree.functions += Function(fromCString(name), fromCString(returnTypeSpelling), functionParams(cursor))
     
    } else if (kind == CXCursor_EnumDecl) {
      val name       = getCursorSpelling(cursor)
      val enumType   = getEnumDeclIntegerType(cursor)
      val enumValues = ListBuffer[Enum.Value]()
     
      visitChildren(cursor, enumVisitor, enumValues.cast[Data])
     
      tree.enums += Enum(fromCString(name), List(enumValues: _*))
     
    } else if (kind == CXCursor_TypedefDecl) {
      val name                = getCursorSpelling(cursor)
      val typedefType         = getTypedefDeclUnderlyingType(cursor)
      val typedefTypeSpelling = getTypeSpelling(typedefType)
     
      tree.typedefs += Typedef(fromCString(name), fromCString(typedefTypeSpelling))
     
    } else {
      val name         = getCursorSpelling(cursor)
      val kindSpelling = getCursorKindSpelling(kind)
      println(s"Unhandled cursor kind for ${name}: ${kindSpelling}")
    }

    CXChildVisit_Continue
  }

  def functionParam(i: Int, parent: CXCursor): Function.Param = {
    val cursor       = Cursor_getArgument(parent, i)
    val tpe          = getCursorType(cursor)
    val name         = getCursorSpelling(cursor)
    val typeSpelling = getTypeSpelling(tpe)
    val nonEmptyName =
      Option(fromCString(name)).filter(_.nonEmpty).getOrElse(s"arg$i")

    Function.Param(nonEmptyName, fromCString(typeSpelling))
  }

  def functionParams(cursor: CXCursor): Seq[Function.Param] = {
    val argc = Cursor_getNumArguments(cursor)
    var i    = 0
    var args = List.empty[Function.Param]

    while (i < argc) {
      args = args :+ functionParam(i, cursor)
      i += 1
    }
    args
  }

  val enumVisitor: Visitor =
    (cursor: CXCursor, parent: CXCursor, data: Data) => {
      val enumValues         = data.cast[ListBuffer[Enum.Value]]
      val kind: CXCursorKind = getCursorKind(cursor)
      assert(kind == CXCursor_EnumConstantDecl)
      val name  = getCursorSpelling(cursor)
      val value = getEnumConstantDeclValue(cursor)
      enumValues += Enum.Value(fromCString(name), value)
      CXChildVisit_Continue
    }
}


trait Tree {
  import scala.collection.mutable.ListBuffer
  val typedefs : ListBuffer[Typedef]  = ListBuffer()
  val functions: ListBuffer[Function] = ListBuffer()
  val enums    : ListBuffer[Enum]     = ListBuffer()
}

sealed trait Node
case class Typedef (name: String, underlying: String) extends Node
case class Function(name: String, returnType: String, args: Seq[Function.Param]) extends Node
case class Enum    (name: String, values: Seq[Enum.Value]) extends Node

object Function {
  case class Param(name: String, tpe: String)
}

object Enum {
  case class Value(name: String, value: CLongLong)
}
