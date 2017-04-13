package scalachefredux

import scala.language.implicitConversions

object ScalaChefRedux {
  def main(args: Array[String]) {
    val parser = new ChefParser
    val chefText = new ChefText
    val chefState = new ChefState

    // read a file
    // http://stackoverflow.com/questions/1284423/read-entire-file-in-scala
    val chef_text = io.Source.fromFile(args(0)).mkString

    val result = parser.parse(parser.chefProgram, chef_text)

    println(result)

  }
}
