package bindgen

import scala.io.Source
import java.io.File
import org.scalatest._
import scala.sys.process._

class BindgenSpec extends FunSpec {

  describe("Bindgen.main") {
    val inputDirectory = new File("src/test/resources/samples")

    val outputDir = new File("target/bindgen-samples")
    Option(outputDir.listFiles()).foreach(_.foreach(_.delete()))
    outputDir.mkdirs()

    def contentOf(file: File) =
      Source.fromFile(file).getLines.mkString("\n")

    for {
      input <- inputDirectory.listFiles()
      if input.getName.endsWith(".h")
      expected = new File(inputDirectory, input.getName.replace(".h", ".scala"))
      if expected.exists()
    } {
      it(s"should generate bindings for ${input.getName}") {
        val output = new File(outputDir, expected.getName)
        val bin = sys.props.get("native.bin").getOrElse {
          sys.error("native.bin is not set")
        }
        val cmd = Array(bin, "-o", output.getAbsolutePath, input.getAbsolutePath)

        assert(Process(cmd).! == 0)
        assert(output.exists())
        assert(contentOf(output) == contentOf(expected))
      }
    }
  }

}
