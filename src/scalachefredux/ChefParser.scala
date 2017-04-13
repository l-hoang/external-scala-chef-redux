package scalachefredux

import scala.util.parsing.combinator._
import scala.language.implicitConversions

/* Parser of Chef files using Scala parsing combinators 
 * Good source here:
 * https://enear.github.io/2016/03/31/parser-combinators/
 **/
class ChefParser extends RegexParsers {
  override def skipWhitespace = true
  // need to be able to track new lines, so everything not a new line is
  // considered whitespace
  override val whiteSpace = "[ \t\r\f]+".r

  def chefProgram: Parser[String] =
    chefRecipe ~ chefRecipe ^^ {
    case x ~ y => x + y}

  /* Parses a single recipe */
  def chefRecipe: Parser[String] = 
    (((chefTitle <~ newLine) <~ (comments.?)) <~ 
    (ingredientDecl <~ newLine)) ~
    ingredient.* <~
    (methodDecl <~ newLine) ^^
    { case _title ~ _ingredients => _title + _ingredients }
  
  def test: Parser[String] = 
    chefTitle ~ chefTitle.? ~ chefTitle ~ chefTitle ^^
    {case a ~ b ~ c ~ d  => "hello"}

  /* Parses a title; drop the period at the end */
  def chefTitle: Parser[String] = 
    """[A-Za-z0-9-_. ]+[.]""".r ^^ 
    { x => x.substring(0, x.length - 1) }

  /* Parse comments that follow a title */
  def comments: Parser[String] = 
    """[A-Za-z0-9-_. ]*[\n]+""".r

  def number: Parser[String] = 
    """[0-9]+""".r

  /* Parses the ingredient declaration */
  def ingredientDecl: Parser[String] =
    """Ingredients.""" ^^ { x => x.substring(0, x.length - 1) }

  /* Parses an ingredient */
  def ingredient: Parser[String] = 
    number.? ~ measure.? ~ """[A-Za-z- _]+""".r <~ newLine ^^
    { case None ~ None ~ _name => "name only " + _name
      case None ~ Some(y) ~ _name => "no num " + "meaure " + y + " " + _name
      case Some(x) ~ None ~ _name => "num " + x.toInt + " " + _name
      case Some(x) ~ Some(y) ~ _name => x.toInt + " measure " + y + " " + _name }

  /* Parses heaped/level */
  def heapedLevel: Parser[String] =
    ("heaped" | "level") ^^ {x => println(x); x}

  /* Parses a measure, which may include an option heaped/level */
  def measure: Parser[String] = 
    heapedLevel.? ~ ("g" | "kg" | "pinch" | "pinches" | "ml" | "l" | "dash" | 
    "dashes" | "cup" | "cups" | "teaspoon" | "teaspoons" | "tablespoon" | 
    "tablespoons") ^^
    { case None ~ _measure => _measure
      case Some(h) ~ _measure => h + " " + _measure }
  
  /* Parses the method declaration */
  def methodDecl: Parser[String] = 
    """Method."""

  /* Deal with new lines, i.e. require 1 at least */
  def newLine: Parser[String] = 
    """[\n]+""".r
}

