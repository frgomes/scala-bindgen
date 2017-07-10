package bindgen

import scalanative.native._


case class Args(files: Seq[String]    = Seq(),
                chdir: Option[String] = None,
                pkg: Option[String]   = Some("scalanative.native.bindings"),
                out: Option[String]   = None,
                recursive: Boolean    = false,
                verbose: Int          = 0,
                debug: Boolean        = false)

object CLI {
  val parser = new scopt.OptionParser[Args]("bindgen") {
    head("bindgen", "0.1")

    arg[String]("files...")
      .unbounded()
      .optional()
      .action((x, c) => c.copy(files = c.files :+ x))
      .text("""Header file(s) to be converted to Scala.""")

    opt[String]('C', "chdir")
      .optional
      .valueName("DIR")
      .action((x, c) => c.copy(chdir = Option(x)))
      .text("""Change to DIR before performing any operations.""")

    opt[String]('P', "package")
      .valueName("PACKAGE")
      .action((x, c) => c.copy(pkg = Option(x)))
      .text("""Package name.""")

    opt[String]('o', "out")
      .optional
      .valueName("FILE")
      .action((x, c) => c.copy(out = Option(x)))
      .text("""Unified output file.""")

    opt[Unit]('r', "recursive")
      .action((_, c) => c.copy(recursive = true))
      .text("""Produces bindigs for #include(s) recursively.""")

    opt[Unit]('d', "debug")
      .action((_, c) => c.copy(debug = true))
      .text("""Emit Clang AST to stderr.""")

    opt[Unit]('v', "verbose")
      .minOccurs(0).maxOccurs(10)
      .action((_, c) => c.copy(verbose = c.verbose + 1))
      .text("""Increase verbosity.""")

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
    val cargc: CInt = cargs.length
    val cargv: Ptr[CString] = stackalloc[CString](cargs.length)
    var i = 0; while(i<cargc) { cargv(i) = toCString(cargs(i)); i = i + 1 }

    val index: CXIndex = createIndex(0, 1)
    val xs =
      args.files.map { name =>
        if (args.verbose > 1) cerr.println(s"[${name}]")
        val tu: CXTranslationUnit =
          parseTranslationUnit(index,
                               toCString(name),
                               cargv,
                               cargc,
                               null,
                               0,
                               CXTranslationUnit_SkipFunctionBodies)
        if(tu == null) throw new RuntimeException("CXTranslationUnit is null")
        val root: CXCursor = getTranslationUnitCursor(tu)
        if(root == null) throw new RuntimeException("CXCursor is null")
        val result = visitChildren(root, visitor, tree.cast[Data]).toInt
        if(args.verbose > 0) println(s"${result} ${name}")
        if(result==0) makeOutput(tree, resolve(args.chdir, args.out, Option(name), ".h", ".scala"))
        disposeTranslationUnit(tu)
        if (args.debug || args.verbose > 0) cerr.println("----------------------------------------------")
        result
      }
    if (index != null) disposeIndex(index)
    xs.sum
  }

  private def makeOutput(tree: Tree, out: String): Unit = {
    val cout = if("-" == out) new PrintStream(System.out) else new PrintStream(new FileOutputStream(mkdirs(out)))

    if(args.debug) {
      cerr.println(s"typedefs.size  = ${tree.typedefs.size}")
      cerr.println(s"enums.size     = ${tree.enums.size}")
      cerr.println(s"functions.size = ${tree.functions.size}")
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

 // def mkdirs(name: Option[String])
  def mkdirs(name: String): File = mkdirs(Paths.get(name).toFile)
  def mkdirs(file: java.io.File): File = {
    val dir = file.getParentFile
    if(!dir.isDirectory)
      if (!dir.mkdirs)
        throw new java.io.IOException(s"Cannot create directory ${dir.toString}")
    file
  }

  private def resolve(chdir: Option[String], name: Option[String]): String = {
    val dir  =
      chdir match {
        case None     => "."
        case Some("") => "."
        case Some(d)  => d
      }
    val path =
      if(name.isEmpty)
        throw new IllegalArgumentException("Cannot access a null name.")
      else
        Paths.get(name.get)
    if(path.isAbsolute) path.toString else Paths.get(dir, path.toString).toString
  }

  def resolve(chdir: Option[String], name: Option[String], default: Option[String]): String =
    name match {
      case Some("-") => "-"
      case None      => resolve(chdir, default)
      case Some("")  => resolve(chdir, default)
      case _         => resolve(chdir, name)
    }

  def resolve(chdir: Option[String], name: Option[String], default: Option[String], from: String, to: String): String = {
    def replaceThenResolve: String = {
        val name = default.getOrElse(throw new IllegalArgumentException("Cannot enforce extension on a null default name."))
        resolve(chdir, Option(name.replace(from, to)))
    }
    name match {
      case Some("-") => "-"
      case None      => replaceThenResolve
      case Some("")  => replaceThenResolve
      case _         => resolve(chdir, name)
    }
  }

  // Java-like API

  def resolve(chdir: String, name: String, default: String): String =
    resolve(Option(chdir), Option(name), Option(default))

  def resolve(chdir: String, name: String, default: String, from: String, to: String): String =
    resolve(Option(chdir), Option(name), Option(default), from, to)
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
