package bindgen

import scalanative.native._


case class Args(files: Seq[String] = Seq(),
                chdir: String = ".",
                pkg: String = "scalanative.native.bindings",
                out: String = "",
                recursive: Boolean = false,
                debug: Boolean = false)

object CLI {
  val parser = new scopt.OptionParser[Args]("bindgen") {
    head("bindgen", "0.1")

    arg[String]("files...")
      .unbounded()
      .optional()
      .action((x, c) => c.copy(files = c.files :+ x))
      .text("""Header file(s) to be converted to Scala.""")

    opt[String]('C', "chdir")
      .valueName("DIR")
      .action((x, c) => c.copy(chdir = x))
      .text("""Change to DIR before performing any operations.""")

    opt[String]('P', "package")
      .valueName("PACKAGE")
      .action((x, c) => c.copy(pkg = x))
      .text("""Package name.""")

    opt[String]('o', "out")
      .optional
      .valueName("FILE")
      .action((x, c) => c.copy(out = x))
      .text("""Unified output file.""")

    opt[Unit]('r', "recursive")
      .action((_, c) => c.copy(recursive = true))
      .text("""Produces bindigs for #include(s) recursively.""")

    opt[Unit]('d', "debug")
      .action((_, c) => c.copy(debug = true))
      .text("""Emit Clang AST to stderr.""")

    help("help")
      .text("""prints this usage text""")
  }
}

object Main {

  def main(args: Array[String]): Unit = {
    val (left, right) = args.span(item => "--" != item)
    val cargs = if(right.isEmpty) Array[String]() else right.tail.toArray
    val errno: Int =
      CLI.parser.parse(left, Args()) match {
        case Some(a) => (new Generator(a, cargs)).process
        case None    => -1 // arguments are bad, error message will have been displayed
      }

    System.exit(errno)
  }
}

class Generator(args: Args, cargs: Array[String]) extends FileUtils {
  import scala.scalanative.bindings.clang._
  import scala.scalanative.bindings.clang.api._
  import scala.collection.mutable.ListBuffer

  import java.io.{File, FileOutputStream, PrintStream}

  private val tree    = new Object with Tree
  private val visitor = AST.visitor
  private val cerr    = new PrintStream(System.err)

  def process: Int = Zone { implicit z =>
    val clang_argc: CInt         = 0
    val clang_argv: Ptr[CString] = null //TODO: c.clang_args.zipWithIndex.foreach { case (p, i) => clang_argv(i) = p }

    val index: CXIndex = createIndex(0, 1)
    val xs =
      args.files.map { name =>
        if (args.debug) cerr.println(name)
        val tu: CXTranslationUnit =
          parseTranslationUnit(index,
                               toCString(name),
                               clang_argv,
                               clang_argc,
                               null,
                               0,
                               CXTranslationUnit_SkipFunctionBodies)
        assert(tu != null)
        if (tu == null) -1
        else {
          val root: CXCursor = getTranslationUnitCursor(tu)
          assert(root != null)
          val result = visitChildren(root, visitor, tree.cast[Data])
          //TODO assert(result == CXChildVisit_Continue)
          makeOutput(tree, resolve(args.chdir, args.out, name, ".h", ".scala"))
          disposeTranslationUnit(tu)
          result.toInt
        }
      }
    if (index != null) disposeIndex(index)
    0 //FIXME: obtain from iterator
  }

  private def makeOutput(tree: Tree, out: String): Unit = {
    val cout = if("-" == out) new PrintStream(System.out) else new PrintStream(new FileOutputStream(mkdirs(out)))

    if(args.debug) {
      cerr.println("----------------------------------------------")
      cerr.println(s"typedefs.size  = ${tree.typedefs.size}")
      cerr.println(s"enums.size     = ${tree.enums.size}")
      cerr.println(s"functions.size = ${tree.functions.size}")
      cerr.println("----------------------------------------------")
    }

    tree.typedefs.foreach { entry =>
      cout.println(s"type ${entry.name} = ${entry.underlying}")
    }

    tree.enums.foreach { entry =>
      cout.println(s"object ${entry.name}_Enum {")
      cout.println(
        entry.values
          .map(enum => s"  val ${enum.name} = ${enum.value}")
          .mkString("\n"))
      cout.println("}")
    }

    tree.functions.foreach { entry =>
      cout.println(s"def ${entry.name}(")
      cout.println(
        entry.args
          .map(param => s"  ${param.name}: ${param.tpe}")
          .mkString("\n"))
      cout.println(s"  ): ${entry.returnType} = extern")
    }

    cout.flush()
    if("-" != out) cout.close
  }
}


trait FileUtils {
  import java.io.File
  import java.nio.file.Paths
  import java.nio.file.Path

  def mkdirs(name: String): File = mkdirs(Paths.get(name).toFile)
  def mkdirs(file: java.io.File): File = {
    val dir = file.getParentFile
    if(!dir.isDirectory)
      if (!dir.mkdirs)
        throw new java.io.IOException(s"Cannot create directory ${dir.toString}")
    file
  }

  def resolve(chdir: String, name: String): String = {
    val dir  = if(null==chdir || ""==chdir) "." else chdir
    val path = Paths.get(name)
    if(path.isAbsolute) path.toString else Paths.get(dir, path.toString).toString
  }

  def resolve(chdir: String, name: String, default: String): String =
    (if(name==null) "" else name) match {
      case "-" => "-"
      case ""  => resolve(chdir, default)
      case _   => resolve(chdir, name)
    }

  def resolve(chdir: String, name: String, default: String, from: String, to: String): String =
    (if(name==null) "" else name) match {
      case "-" => "-"
      case ""  =>
        if(default==null)
          throw new IllegalArgumentException("Cannot enforce extension on a null default name.")
        else
          resolve(chdir, default.replace(from, to))
      case _   => resolve(chdir, name)
    }
}

object AST {
  import scala.collection.mutable.ListBuffer
  import scala.scalanative.bindings.clang._
  import scala.scalanative.bindings.clang.api._

  def visitor: Visitor = (cursor: CXCursor, parent: CXCursor, data: Data) => {

    val tree               = data.cast[Tree]
    val kind: CXCursorKind = getCursorKind(cursor)

    if (kind == CXCursor_FunctionDecl) {
      val name               = getCursorSpelling(cursor)
      val cursorType         = getCursorType(cursor)
      val returnType         = getResultType(cursorType)
      val returnTypeSpelling = getTypeSpelling(returnType)
      val argc               = Cursor_getNumArguments(cursor)

      tree.functions += Function(fromCString(name),
                                 fromCString(returnTypeSpelling),
                                 functionParams(cursor))

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

      tree.typedefs += Typedef(fromCString(name),
                               fromCString(typedefTypeSpelling))

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
case class Typedef (name: String,
                    underlying: String)        extends Node
case class Function(name: String,
                    returnType: String,
                    args: Seq[Function.Param]) extends Node
case class Enum    (name: String,
                    values: Seq[Enum.Value])   extends Node

object Function {
  case class Param(name: String, tpe: String)
}

object Enum {
  case class Value(name: String, value: CLongLong)
}
