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

  def chefProgram: Parser[List[ChefResult]] =
    chefRecipe ~ (newLine ~> chefRecipe).* ^^ {
      case r ~ l => r :: l
    }

  /* Parses a single recipe */
  def chefRecipe: Parser[ChefResult] = 
    (chefTitle <~ (comments)) ~ 
    (ingredientList.? <~ (cookingTime.? <~ ovenTemp.? <~ methodDecl)) ~
    chefLine.+ ~
    serves.? ^^ { 
      case _title ~ None ~ _lines ~ None => 
        new ChefResult(_title, List(), _lines)
      case _title ~ Some(_ingredients) ~ _lines ~ None => 
        new ChefResult(_title, _ingredients, _lines)
      case _title ~ None ~ _lines ~ Some(x) => 
        new ChefResult(_title, List(), _lines :+ x)
      case _title ~ Some(_ingredients) ~ _lines ~ Some(x) => 
        new ChefResult(_title, _ingredients, _lines :+ x)
    }
  
  /* Parses a title; drop the period at the end */
  def chefTitle: Parser[String] = 
    """[A-Za-z0-9-_,\. ]+\.""".r <~ newLine ^^ 
    { x => x.substring(0, x.length - 1) }

  /* Parse comments that follow a title */
  def comments: Parser[String] = 
    """[A-Za-z0-9-_ \.?!\"]*""".r <~ newLine

  /* Parses a number and returns a number */
  def number: Parser[Int] = 
    """[0-9]+""".r ^^ { _.toInt }

  /* Parses ingredient declaration + list */
  def ingredientList: Parser[List[ChefIngredient]] =
    ingredientDecl ~> ingredient.+

  /* Parses the ingredient declaration */
  def ingredientDecl: Parser[String] =
    "Ingredients." <~ newLine

  /* Parses an ingredient */
  def ingredient: Parser[ChefIngredient] = 
    number.? ~ measure.? ~ """[A-Za-z- _]+""".r <~ """[\n]+""".r ^^
    { case None ~ None ~ _name => 
        new ChefIngredient(_name, I_EITHER)
      case None ~ Some(y) ~ _name => 
        if ((y startsWith "heaped") || (y startsWith "level")) {
          // always dry
          new ChefIngredient(_name, I_DRY)
        } else {
          // otherwise, the string is just the measure: 
          // check for other things...
          y match {
            case "g" | "kg" | "pinch" | "pinches" =>
              new ChefIngredient(_name, I_DRY)
            case "ml" | "l" | "dash" | "dashes" =>
              new ChefIngredient(_name, I_LIQUID)
            case "cup" | "cups" | "teaspoon" | "teaspoons" | "tablespoons" |
             "tablespoon" =>
              new ChefIngredient(_name, I_EITHER)
            case _ => throw new RuntimeException("bad measure")
          }
        }
      case Some(x) ~ None ~ _name => 
        new ChefIngredient(_name, I_EITHER, x)
      case Some(x) ~ Some(y) ~ _name => 
        if ((y startsWith "heaped") || (y startsWith "level")) {
          // always dry
          new ChefIngredient(_name, I_DRY, x)
        } else {
          // otherwise, the string is just the measure: 
          // check for other things...
          y match {
            case "g" | "kg" | "pinch" | "pinches" =>
              new ChefIngredient(_name, I_DRY, x)
            case "ml" | "l" | "dash" | "dashes" =>
              new ChefIngredient(_name, I_LIQUID, x)
            case "cup" | "cups" | "teaspoon" | "teaspoons" | "tablespoons" |
             "tablespoon" =>
              new ChefIngredient(_name, I_EITHER, x)
            case _ => throw new RuntimeException("bad measure")
          }
        }
    }

  /* Parses heaped/level */
  def heapedLevel: Parser[String] =
    ("heaped" | "level")

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
    "Method." <~ newLine

  /* Parses a Chef statement */
  def chefLine: Parser[ChefLine] = 
    (takeLine | putLine | putLine2 | foldLine | foldLine2 | 
    addDryLine | addDryLine2 |
    addLine | addLine2 | removeLine | removeLine2 |
    combineLine | combineLine2 | divideLine | divideLine2 |
    liquefyContentsLine | liquefyContentsLine2 | liquefyLine |
    stirBowlLine | stirBowlLine2 |
    stirIngredientLine | stirIngredientLine2 |
    mixLine | mixBowlLine | mixBowlLine2 | cleanLine |
    pourLine | pourLine2 | pourLine3 | pourLine4 |
    verbEndLine2 | verbEndLine | verbLine |
    setLine | serveLine | refrigerateLine | refrigerateLine2 ) <~ """[\n]*""".r

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
  /* Parses a Put line variant */
  def putLine2: Parser[ChefLine] = 
    "Put " ~> """[A-Za-z- _]+ +into +the +mixing +bowl""".r <~ "." ^^ {
      longString => Push(getIngredient(longString, "into"), 1)
    }

  /* Parses a Fold line */
  def foldLine: Parser[ChefLine] = 
    "Fold " ~> """[A-Za-z- _]+ +into +mixing +bowl""".r ~ number.? <~ "." ^^ {
      case longString ~ None =>
        Pop(1, getIngredient(longString, "into"))
      case longString ~ Some(bowl) =>
        Pop(bowl, getIngredient(longString, "into"))
    }

  /* Parses a fold line variant */
  def foldLine2: Parser[ChefLine] = 
    "Fold " ~> """[A-Za-z- _]+ +into +the +mixing +bowl""".r <~ "." ^^ {
      longString => Pop(1, getIngredient(longString, "into"))
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
      case None => AddDry(1)
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
    """Mix +mixing +bowl""".r  ~> number <~ "well".r <~ "." ^^ { 
      bowl => Mix(bowl) 
    }

  /* Parse a clean line */
  def cleanLine: Parser[ChefLine] = 
    """Clean +mixing +bowl""".r ~> (number.? <~ ".") ^^ {
      case None => ClearStack(1)
      case Some(bowl) => ClearStack(bowl)
    }

  /* 4 different variants of pour line based on which optional things
   * appear */
  def pourLine: Parser[ChefLine] =
    """Pour +contents +of +the +mixing +bowl +into +the +baking +dish""".r <~ 
    "." ^^ { _ => CopyStack(1, 1) }
  def pourLine2: Parser[ChefLine] =
    """Pour +contents +of +mixing +bowl""".r ~> number <~ 
    """into +the +baking +dish""".r <~ "." ^^ { bowl => CopyStack(bowl, 1) }
  def pourLine3: Parser[ChefLine] =
    """Pour +contents +of +the +mixing +bowl +into +baking +dish""".r ~>
    number <~ "." ^^ { dish => CopyStack(1, dish) }
  def pourLine4: Parser[ChefLine] =
    ("""Pour +contents +of +mixing +bowl""".r ~> 
    number) ~ ("""into +baking +dish""".r ~> number <~ ".") ^^ { 
      case bowl ~ dish => CopyStack(bowl, dish) 
    }

  /* Parses a verb/loop beginning */
  def verbLine: Parser[ChefLine] =
    ("""[A-Za-z-_]+ """.r <~ "the") ~ """[A-Za-z- _]+""".r <~ "." ^^ {
      case v ~ ing => 
        val verb = v.trim.toLowerCase
        LoopStart(if (verb endsWith "e") verb + "d" else verb + "ed", 
                  ing.trim, -1)
    }

  /* Parses a verb/loop end */
  def verbEndLine: Parser[ChefLine] =
    ("""[A-Za-z-_]+ """.r ~> "the") ~> ("""[A-Za-z-_ ]+ +until""".r) ~
    ("""[A-Za-z-_]+""".r <~ ".") ^^ {
      case i ~ v =>
        LoopEnd(v.toLowerCase, getIngredient(i, "until"), -1)
    }

  /* Parses verb/loop end variant */
  def verbEndLine2: Parser[ChefLine] =
    """[A-Za-z-_]+ +until""".r ~> ("""[A-Za-z-_]+""".r <~ ".") ^^ {
      v => LoopEnd(v.toLowerCase, "", -1)
    }

  /* Parse a set aside line */
  def setLine: Parser[ChefLine] =
    """Set +aside *\.""".r ^^ { _ => Break(-1) }

  /* Parse a Serve line */
  def serveLine: Parser[ChefLine] = 
    """Serve +with """.r ~> """[A-Za-z0-9-_. ]+ *\.""".r ^^ {
      r_name => 
        println(r_name)
        val x = r_name.substring(0, r_name.length - 1).trim
        println(x)
        Call(x)
    }

  /* Parse a refrigerate line */
  def refrigerateLine: Parser[ChefLine] =
    """Refrigerate *\.""".r ^^ { _ => Return(-1) }

  /* Parse refrigerate with an hour specification */
  def refrigerateLine2: Parser[ChefLine] =
    """Refrigerate +for""".r ~> number <~ "hours" <~ "." ^^ { 
      hour => Return(hour) 
    }

  /* Parses the final serves statement in a recipe */
  def serves: Parser[ChefLine] = 
    """Serves """ ~> number <~ "." ^^ { num => PrintStacks(num) }

  /* Deal with new lines, i.e. require 1 at least */
  def newLine: Parser[String] = 
    """[\n]{2,}""".r

  /* Get ingredient given that last word after the ingredient */
  def getIngredient(uncut: String, lastWord: String) =
    uncut.substring(0, uncut lastIndexOf lastWord).trim
}

