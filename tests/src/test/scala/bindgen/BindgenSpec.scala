package bindgen

import utest._


object BindgenSpec extends TestSuite {
  import scala.io.Source
  import java.io.File
  import scala.sys.process._

  def contentOf(name: String): String = contentOf(new File(name))
  def contentOf(file: File)  : String = Source.fromFile(file).getLines.mkString("\n")

  val inputDir  = new File("src/test/resources/samples")

  val outputDir = new File("target/bindgen-samples")
  Option(outputDir.listFiles()).foreach(_.foreach(_.delete()))
  outputDir.mkdirs()

  val bin = sys.props.get("native.bin").getOrElse {
    sys.error("native.bin is not set")
  }

  val util = new Object with FileUtils


  val tests = this {
    "ability to generate bindings"-{
      for {
        input <- inputDir.listFiles()                                                if input.getName.endsWith(".h")
        expected = util.resolve(None, None, Option(input.toString), ".h", ".scala")  if (new File(expected)).exists
      } {
        val actual = util.resolve(Option(outputDir.toString), None, Option(input.toString), ".h", ".scala")

        val cmd = Array(bin, "-v", "-d", "-o", actual, input.toString)
        assert(Process(cmd).! == 0)

        assert((new File(actual)).exists)
        val expectedContents = contentOf(expected)
        val actualContents   = contentOf(actual)
        assert(expectedContents == actualContents)
      }
    }
  }
}

//FIXME: https://github.com/frgomes/scala-bindgen/issues/27
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
