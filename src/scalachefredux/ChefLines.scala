package scalachefredux

/* File that contains the types of Chef lines possible in this system */

abstract sealed class ChefLine

case class ChefProgram() extends ChefLine

case class Read(ingredient: String) extends ChefLine // Take
case class Push(ingredient: String, bowlNumber: Int) extends ChefLine // Put
case class Pop(bowlNumber: Int, ingredient: String) extends ChefLine // Fold

case class Add(ingredient: String, bowlNumber: Int) extends ChefLine
case class Subtract(ingredient: String, bowlNumber: Int) extends ChefLine
case class Multiply(ingredient: String, bowlNumber: Int) extends ChefLine
case class Divide(ingredient: String, bowlNumber: Int) extends ChefLine

case class AddDry(bowlNumber: Int) extends ChefLine

case class Liquefy(ingredient: String) extends ChefLine // Liquefy
case class LiquefyContents(bowlNumber: Int) extends ChefLine // Liquefy

case class Stir(num: Int, bowlNumber: Int) extends ChefLine // Stir
case class StirIngredient(ingredient: String, bowlNumber: Int) extends ChefLine // Stir

case class Mix(bowlNumber: Int) extends ChefLine // Mix

case class ClearStack(bowlNumber: Int) extends ChefLine // Clean

case class CopyStack(bowlNumber: Int, dishNumber: Int) extends ChefLine // Pour

case class LoopStart(verb: String, ingredient: String, end: Int) extends ChefLine // Verb
case class LoopEnd(verb: String, ingredient: String, begin: Int) extends ChefLine // Verb until

case class Break(breakLine: Int) extends ChefLine // Set

case class Call(recipe: String) extends ChefLine // Serve

case class Return(num: Int) extends ChefLine

case class PrintStacks(num: Int) extends ChefLine // Serves

