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
    ((chefTitle <~ (comments.?)) <~ 
    ingredientDecl) ~
    (ingredient.* <~
    (cookingTime.? <~
    ovenTemp.? <~
    methodDecl)) ~
    chefLine.+ ~
    serves.? ^^
    { case _title ~ _ingredients ~ _lines ~ _serves => _title + _ingredients +
      _lines + _serves}
  
  /* Parses a title; drop the period at the end */
  def chefTitle: Parser[String] = 
    """[A-Za-z0-9-_. ]+[.]""".r <~ newLine ^^ 
    { x => x.substring(0, x.length - 1) }

  /* Parse comments that follow a title */
  def comments: Parser[String] = 
    """[A-Za-z0-9-_. ]*""".r <~ newLine

  /* Parses a number and returns a number */
  def number: Parser[Int] = 
    """[0-9]+""".r ^^ { _.toInt }

  /* Parses the ingredient declaration */
  def ingredientDecl: Parser[String] =
    """Ingredients.""" <~ newLine

  /* Parses an ingredient */
  def ingredient: Parser[String] = 
    number.? ~ measure.? ~ """[A-Za-z- _]+""".r <~ """[\n]+""".r ^^
    { case None ~ None ~ _name => "name only " + _name
      case None ~ Some(y) ~ _name => "no num " + "meaure " + y + " " + _name
      case Some(x) ~ None ~ _name => "num " + x + " " + _name
      case Some(x) ~ Some(y) ~ _name => x + " measure " + y + " " + _name }

  /* Parses heaped/level */
  def heapedLevel: Parser[String] =
    ("heaped" | "level") ^^ {x => println(x); x}

  /* Parses a measure, which may include an option heaped/level */
  def measure: Parser[String] = 
    heapedLevel.? ~ ("kg" | "g" | "pinches" | "pinch" | "ml" | "l" | "dashes" | 
    "dash" | "cups" | "cup" | "teaspoons" | "teaspoon" | "tablespoons" | 
    "tablespoon") ^^
    { case None ~ _measure => _measure
      case Some(h) ~ _measure => h + " " + _measure }
  
  /* Parses cooking time */
  def cookingTime: Parser[String] = 
    "Cooking time: " <~ """[0-9]+""".r <~ 
    ("hours" | "hour" | "minutes" | "minute").? <~ "." <~ newLine

  /* Parses the oven temperature */
  def ovenTemp: Parser[String] =
    "Pre-heat oven to " <~ """[0-9]+""".r <~ "degrees Celsius" <~
    gasMark.? <~ "." <~ newLine

  /* Parses the gas mark specification */
  def gasMark: Parser[Int] = 
    "(" ~> "gas mark " ~> number <~ ")"

  /* Parses the method declaration */
  def methodDecl: Parser[String] = 
    """Method.""" <~ newLine

  /* Parses a Chef statement */
  def chefLine: Parser[ChefLine] = 
    (takeLine | putLine | foldLine | 
    addDryLine | addDryLine2 |
    addLine | addLine2 | removeLine | removeLine2 |
    combineLine | combineLine2 | divideLine | divideLine2 |
    liquefyContentsLine | liquefyContentsLine2 | liquefyLine |
    stirBowlLine | stirBowlLine2 |
    stirIngredientLine | stirIngredientLine2 ) <~ """[\n]*""".r

  /* Parses a Take line */
  def takeLine: Parser[ChefLine] =
    "Take " ~> """[A-Za-z- _]+ +from +refrigerator""".r <~ "." ^^ { longString =>
      // get the ingredient name
      val fromIndex = longString lastIndexOf "from"
      val onlyIngredient = longString.substring(0, fromIndex).trim
      Read(onlyIngredient)
   }

  /* Parses a Put line */
  def putLine: Parser[ChefLine] = 
    "Put " ~> """[A-Za-z- _]+ +into +mixing +bowl""".r ~ number.? <~ "." ^^ {
      case longString ~ None =>
        Push(getIngredient(longString, "into"), 1)
      case longString ~ Some(bowl) =>
        Push(getIngredient(longString, "into"), bowl)
    }

  /* Parses a Fold line */
  def foldLine: Parser[ChefLine] = 
    "Fold " ~> """[A-Za-z- _]+ +into +mixing +bowl""".r ~ number.? <~ "." ^^ {
      case longString ~ None =>
        Pop(1, getIngredient(longString, "into"))
      case longString ~ Some(bowl) =>
        Pop(bowl, getIngredient(longString, "into"))
    }

  /* Parses an Add line */
  def addLine: Parser[ChefLine] = 
    "Add " ~> """[A-Za-z- _]+ +to +mixing +bowl""".r ~ number.? <~ "." ^^ {
      case longString ~ None =>
        Add(getIngredient(longString, "to"), 1)
      case longString ~ Some(bowl) =>
        Add(getIngredient(longString, "to"), bowl)
    }

  /* Parses an Add line variant */
  def addLine2: Parser[ChefLine] = 
    "Add " ~> """[A-Za-z- _]+""".r <~ "." ^^ { x =>
      Add(x.trim, 1)
    }

  /* Parses a Remove line */
  def removeLine: Parser[ChefLine] = 
    "Remove " ~> """[A-Za-z- _]+ +from +mixing +bowl""".r ~ number.? <~ "." ^^ {
      case longString ~ None =>
        Subtract(getIngredient(longString, "from"), 1)
      case longString ~ Some(bowl) =>
        Subtract(getIngredient(longString, "from"), bowl)
    }

  /* Parses a Remove line variant */
  def removeLine2: Parser[ChefLine] = 
    "Remove " ~> """[A-Za-z- _]+""".r <~ "." ^^ { x =>
      Subtract(x.trim, 1)
    }

  /* Parses a Combine line */
  def combineLine: Parser[ChefLine] = 
    "Combine " ~> """[A-Za-z- _]+ +into +mixing +bowl""".r ~ number.? <~ "." ^^ {
      case longString ~ None =>
        Multiply(getIngredient(longString, "into"), 1)
      case longString ~ Some(bowl) =>
        Multiply(getIngredient(longString, "into"), bowl)
    }

  /* Parses a Combine line variant */
  def combineLine2: Parser[ChefLine] = 
    "Combine " ~> """[A-Za-z- _]+""".r <~ "." ^^ { x =>
      Multiply(x.trim, 1)
    }

  /* Parses a Divide line */
  def divideLine: Parser[ChefLine] = 
    "Divide " ~> """[A-Za-z- _]+ +into +mixing +bowl""".r ~ number.? <~ "." ^^ {
      case longString ~ None =>
        Divide(getIngredient(longString, "into"), 1)
      case longString ~ Some(bowl) =>
        Divide(getIngredient(longString, "into"), bowl)
    }

  /* Parses a Divide line variant */
  def divideLine2: Parser[ChefLine] = 
    "Divide " ~> """[A-Za-z- _]+""".r <~ "." ^^ { x =>
      Divide(x.trim, 1)
    }

  /* Parse add dry line with optional mixing bowl */
  def addDryLine: Parser[ChefLine] = 
    """Add +dry +ingredients +to +mixing +bowl""".r ~> number.? <~ "." ^^ {
      case None => println("adklfj");AddDry(1)
      case Some(bowl) => AddDry(bowl)
    }

  /* Parse add dry line with no optional mixing bowl */
  def addDryLine2: Parser[ChefLine] = 
    """Add +dry +ingredients""".r <~ "." ^^ { _ => AddDry(1) }

  /* Parse liquefy line */
  def liquefyLine: Parser[ChefLine] = 
    "Liquefy " ~> """[A-Za-z- _]+""".r <~ "." ^^ {
      i => Liquefy(i.trim)
    }

  /* Parse liquefy contents line */
  def liquefyContentsLine: Parser[ChefLine] = 
    """Liquefy +contents +of +the +mixing +bowl""".r <~ "." ^^ {
      _ => LiquefyContents(1)
    }

  /* Parse liquefy contents variant line */
  def liquefyContentsLine2: Parser[ChefLine] = 
    """Liquefy +contents +of +mixing +bowl""".r ~> number <~ "." ^^ {
      bowl => LiquefyContents(bowl)
    }

  /* Parse stir bowl */
  def stirBowlLine: Parser[ChefLine] = 
    ("""Stir +mixing +bowl """.r ~> number <~ "for") ~ number <~ "minutes" <~ 
    "." ^^ {
      case bowl ~ minutes => Stir(minutes, bowl)
    }

  /* Parse stir bowl variant */
  def stirBowlLine2: Parser[ChefLine] = 
    """Stir +the +mixing +bowl +for""".r ~> number <~ "minutes" <~ "." ^^ {
      minutes => Stir(minutes, 1)
    }

  /* Parse a stir ingredient line */
  def stirIngredientLine: Parser[ChefLine] = 
    "Stir " ~> """[A-Za-z- _]+ +into +the +mixing +bowl""".r <~ "." ^^ {
      longString => StirIngredient(getIngredient(longString, "into"), 1)
    }

  /* Parse a stir ingredient line variant */
  def stirIngredientLine2: Parser[ChefLine] = 
    ("Stir " ~> """[A-Za-z- _]+ +into +mixing +bowl""".r) ~ number <~ "." ^^ {
      case longString ~ bowl => 
        StirIngredient(getIngredient(longString, "into"), bowl)
    }

  /* Parses a mix well line */
  def mixLine: Parser[ChefLine] =
    "Mix " <~ "well" <~ "." ^^ { _ => Mix(1) }

  /* Parses a mix bowl line */
  def mixBowlLine: Parser[ChefLine] =
    """Mix +the +mixing +bowl +well""".r <~ "." ^^ { _ => Mix(1) }

  /* Parses a mix bowl line */
  def mixBowlLine2: Parser[ChefLine] =
    """Mix +the +mixing +bowl +well""".r <~ "." ^^ { _ => Mix(1) }

  /* Parses the final serves statement in a recipe */
  def serves: Parser[Int] = 
    """Serves """ ~> number <~ "." <~ newLine

  /* Deal with new lines, i.e. require 1 at least */
  def newLine: Parser[String] = 
    """[\n]{2,}""".r

  /* Get ingredient given that last word after the ingredient */
  def getIngredient(uncut: String, lastWord: String) =
    uncut.substring(0, uncut lastIndexOf lastWord).trim
}

