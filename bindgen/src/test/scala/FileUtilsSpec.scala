package bindgen

import utest._

object Test extends TestSuite {
  val util = new Object with FileUtils
  val tests = this {
    "ability to determine output file name" - {

      "when output goes to STDOUT"-{
        "well formed chdir and default"-{
          val chdir = "target/bindgen/tests"
          val out   = "-"
          val default  = "test.h"
          val expected = "-"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed chdir"-{
          val chdir = null
          val out   = "-"
          val default  = "test.h"
          val expected = "-"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed default"-{
          val chdir = "target/bindgen/tests"
          val out   = "-"
          val default  = null
          val expected = "-"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed chdir and default"-{
          val chdir = null
          val out   = "-"
          val default  = null
          val expected = "-"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
      }


      "when output assumes defaults"-{
        "well formed chdir and default, output is null"-{
          val chdir = "target/bindgen/tests"
          val out   = null // assuming defaults means that this can be either null or ""
          val default  = "test.h"
          val expected = "target/bindgen/tests/test.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "well formed chdir and default, output is empty"-{
          val chdir = "target/bindgen/tests"
          val out   = null // assuming defaults means that this can be either null or ""
          val default  = "test.h"
          val expected = "target/bindgen/tests/test.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed chdir"-{
          val chdir = null
          val out   = null // assuming defaults means that this can be either null or ""
          val default  = "test.h"
          val expected = "./test.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed default"-{
          val chdir = "target/bindgen/tests"
          val out   = null // assuming defaults means that this can be either null or ""
          val default  = null
          intercept[IllegalArgumentException] {
            util.resolve(chdir, out, default, ".h", ".scala")
          }
        }
        "badly formed chdir and default"-{
          val chdir = null
          val out   = null // assuming defaults means that this can be either null or ""
          val default  = null
          intercept[IllegalArgumentException] {
            util.resolve(chdir:String, out, default, ".h", ".scala")
          }
        }
      }


      "when output specifies a simple file name"-{
        "well formed chdir and default, output is null"-{
          val chdir = "target/bindgen/tests"
          val out   = "unified.scala"
          val default  = "test.h"
          val expected = "target/bindgen/tests/unified.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed chdir"-{
          val chdir = null
          val out   = "unified.scala"
          val default  = "test.h"
          val expected = "./unified.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed default"-{
          val chdir = "target/bindgen/tests"
          val out   = "unified.scala"
          val default  = null
          val expected = "target/bindgen/tests/unified.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed chdir and default"-{
          val chdir = null
          val out   = "unified.scala"
          val default  = null
          val expected = "./unified.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
      }


      "when output specifies a relative file name"-{
        "well formed chdir and default, output is null"-{
          val chdir = "target/bindgen/tests"
          val out   = "another/unified.scala"
          val default  = "test.h"
          val expected = "target/bindgen/tests/another/unified.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed chdir"-{
          val chdir = null
          val out   = "another/unified.scala"
          val default  = "test.h"
          val expected = "./another/unified.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed default"-{
          val chdir = "target/bindgen/tests"
          val out   = "another/unified.scala"
          val default  = null
          val expected = "target/bindgen/tests/another/unified.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed chdir and default"-{
          val chdir = null
          val out   = "another/unified.scala"
          val default  = null
          val expected = "./another/unified.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
      }


      "when output specifies an absolute file name"-{
        "well formed chdir and default, output is null"-{
          val chdir = "target/bindgen/tests"
          val out   = "/tmp/unified.scala"
          val default  = "test.h"
          val expected = "/tmp/unified.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed chdir"-{
          val chdir = null
          val out   = "/tmp/unified.scala"
          val default  = "test.h"
          val expected = "/tmp/unified.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed default"-{
          val chdir = "target/bindgen/tests"
          val out   = "/tmp/unified.scala"
          val default  = null
          val expected = "/tmp/unified.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
        "badly formed chdir and default"-{
          val chdir = null
          val out   = "/tmp/unified.scala"
          val default  = null
          val expected = "/tmp/unified.scala"
          val actual   = util.resolve(chdir, out, default, ".h", ".scala")
          assert(expected == actual)
        }
      }

    }
  }
}
