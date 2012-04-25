package mutabletest

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import javax.management.remote.rmi._RMIConnection_Stub
import scopt.immutable.OptionParser._

/**
 * Tests the use of the options parser
 */

case class Config(var foo: Int = -1,
  var bar: String = null,
  var bop: Boolean = false,
  var xyz: Boolean = false,
  var libname: String = null,
  var libfile: String = null,
  var maxlibname: String = null,
  var maxcount: Int = -1,
  var whatnot: String = null,
  var files: List[String] = Nil,
  var packageDir: Boolean = false)

@RunWith(classOf[JUnitRunner])
class OptionsTest extends FunSuite {
  var config: Config = _

  val parser1 = new scopt.OptionParser("scopt") {
    intOpt("f", "foo", "foo is an integer property", {v: Int => config.foo = v})
    stringOpt("o", "output", "<file>", "output is a string property", {v: String => config.bar = v})
    booleanOpt("xyz", "xyz is a boolean property", {v: Boolean => config.xyz = v})
    keyValueOpt("l", "lib", "<libname>", "<filename>", "load library <libname>",
      {(key: String, value: String) => { config.libname = key; config.libfile = value } })
    keyIntValueOpt(None, "max", "<libname>", "<max>", "maximum count for <libname>",
      {(key: String, value: Int) => { config.maxlibname = key; config.maxcount = value } })
    opt("package-dir", "generates package directories",
      { _ => config.packageDir = true })
    arg("<file>", "some argument", {v: String => config.whatnot = v})
    help("h","help","Print out this help string")
  }
  
  test("valid arguments are parsed correctly") {
    validArguments(parser1, Config(whatnot = "blah"), "blah")
    validArguments(parser1, Config(foo = 35, whatnot = "abc"), "-f", "35", "abc")
    validArguments(parser1, Config(foo = 22, bar = "beer", whatnot = "drink"), "-o", "beer", "-f", "22", "drink")
    validArguments(parser1, Config(foo = 22, bar = "beer", whatnot = "drink"), "-f", "22", "--output", "beer", "drink")
    validArguments(parser1, Config(libname = "key", libfile = "value", whatnot = "drink"), "--lib:key=value", "drink")
    validArguments(parser1, Config(maxlibname = "key", maxcount = 5, whatnot = "drink"), "--max:key=5", "drink")
    validArguments(parser1, Config(xyz = true, whatnot = "drink"), "--xyz", "true", "drink")
    validArguments(parser1, Config(whatnot = "blah", packageDir = true), "blah", "--package-dir")
  }

  test("invalid arguments fail") {
    invalidArguments(parser1)
    invalidArguments(parser1, "-z", "blah")
    invalidArguments(parser1, "blah", "blah")
    invalidArguments(parser1, "-z", "abc", "blah")
    invalidArguments(parser1, "-f", "22", "-z", "abc", "blah")
    invalidArguments(parser1, "--xyz")
  }

  test("bad numbers fail to parse nicely") {
    invalidArguments(parser1, "-f", "shouldBeNumber", "blah")
  }

  test("bad booleans fail to parse nicely") {
    invalidArguments(parser1, "--xyz", "shouldBeBoolean", "blah")
  }

  val parser2 = new scopt.OptionParser("scopt") {
    arglist("<file>...", "some argument", {v: String => config.files = (v :: config.files).reverse })
  }
  
  test("valid argument list is parsed correctly") {
    validArguments(parser2, Config(files = List("foo", "bar")), "foo", "bar")
  }
  
  val parser3 = new scopt.OptionParser("scopt") {
    argOpt("<file>...", "some argument", {v: String => config.files = (v :: config.files).reverse })
  }
  
  test("empty optional argument is parsed correctly") {
    validArguments(parser3, Config(files = List()))
  }  
  
  test("valid optional argument is parsed correctly") {
    validArguments(parser3, Config(files = List("foo")), "foo")
  }

  test("help should simply print out message and exit") {
    invalidArguments(parser1, "-h")
  }
    
  def validArguments(parser: scopt.OptionParser,
      expectedConfig: Config, args: String*) {
    config = new Config()
    expect(true) {
      parser.parse(args)
    }

    expect(expectedConfig) {
      config
    }
  }

  def invalidArguments(parser: scopt.OptionParser,
      args: String*) {
    config = new Config()
    expect(false) {
      parser.parse(args)
    }
  }
}