package scalachefredux

import scala.language.implicitConversions

object ScalaChefRedux {
  def main(args: Array[String]) {
    val parser = new ChefParser

    // read a file
    // http://stackoverflow.com/questions/1284423/read-entire-file-in-scala
    val chef_text = io.Source.fromFile(args(0)).mkString

    println(parser.parse(parser.chefProgram, chef_text))
  }
}
