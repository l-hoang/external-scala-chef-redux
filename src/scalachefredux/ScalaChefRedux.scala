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

    println("Parse result: " + result);

    var first = false

    //println(result.get)

    for (recipeInfo <- result.get) {
      // start a new function in metadata
      val recipeName = recipeInfo.recipeName
      chefText functionStart recipeName

      //println("NAME: " + recipeName)
      
      if (!first) {
        chefState setMainRecipe recipeName
        first = true
      }

      // save ingredients
      for (i <- recipeInfo.ingredients) {
        //println("Ingredient: "i.ingredientName)
        chefText addIngredient i
      }

      for (line <- recipeInfo.lines) {
        //println("Line: " +line)
        chefText addLine line
      }
    }

    //println("out of loop, going to begin execution")

    chefText.endFunction
    chefText.consistencyCheck

    val runner = new ChefRunner(chefState, chefText)
    runner.run
  }
}
