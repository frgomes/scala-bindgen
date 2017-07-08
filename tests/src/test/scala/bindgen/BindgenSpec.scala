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
        input <- inputDir.listFiles()                                        if input.getName.endsWith(".h")
        expected = util.resolve(null, null, input.toString, ".h", ".scala")  if (new File(expected)).exists
      } {
        val actual = util.resolve(outputDir.toString, null, input.toString, ".h", ".scala")
        println(s"Generating bindings file: ${actual}")

        val cmd = Array(bin, "-o", actual, input.toString)
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
