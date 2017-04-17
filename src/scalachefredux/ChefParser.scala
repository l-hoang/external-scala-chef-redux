package scalachefredux

import scala.util.parsing.combinator._
import scala.language.implicitConversions

/* Parser of Chef files using Scala parsing combinators 
 * Good source here:
 * https://enear.github.io/2016/03/31/parser-combinators/
 **/
object ChefParser extends RegexParsers {
  // This tells the parser to ignore whitespace when MOVING BETWEEN PARSER
  // COMBINATORS. Whitespace will NOT be ignored when actually in the parsing
  // rule.
  override def skipWhitespace = true

  // This specifies which tokens need to be treated as whitespace by the 
  // parser.
  // I need to be able to track new lines, so the new line token is NOT
  // included here.
  override val whiteSpace = "[ \t\r\f]+".r

  /* This is the top level parser which will parse Chef recipes. */
  def chefProgram: Parser[List[ChefResult]] =
    // First, parse a regular chefRecipe.
    // Then, parse a new line and possibly more Chef recipes.
    chefRecipe ~ (newLine ~> chefRecipe).* ^^ {
      // The result will be a chefRecipe, contained in r, and a list of
      // chefRecipes contained in l. Merge the 2 things together to return
      // a list of Chef recipes represented in ChefResult objects.
      case r ~ l => r :: l
    }

  /* Parses a single Chef recipe */
  def chefRecipe: Parser[ChefResult] = 
    // First, parse the title of the recipe as well as comments after the title
    (chefTitle <~ (comments)) ~ 
    // Then, potentially parse the ingredient list as well as the optional
    // cooking time and oven temperature. Finally, parse the (not optional)
    // method declaration line.
    (ingredientList.? <~ (cookingTime.? <~ ovenTemp.? <~ methodDecl)) ~
    // Parse at least 1 chefLine
    chefLine.+ ~
    // (Optionally) parse the serves line that appears at the end of a recipe.
    serves.? ^^ { 
      // There are 4 things that matter in this parse: the title,
      // the ingredient list, the lines, and the serves statement. Some of 
      // these things may not have been parsed due to being optional. 
      // Therefore, cases are used to differentiate among the possible
      // parsing results.
      // Regardless, all cases need to return a ChefResult which will
      // contain the result of parsing 1 Chef recipe.
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
    // Parse the title at the beginning of the line, which is composed of
    // alpha-num characters as well as some symbols. It must end in a
    // period.
    // Also, parse a newLine, which is a blank line.
    """^([A-Za-z0-9-_,\. ]+\.)""".r <~ newLine ^^ 
    // x is the parsed string: I trim the period that must be parsed to return
    // the title.
    { x => x.substring(0, x.length - 1) }

  /* Parse comments that follow a title */
  def comments: Parser[String] = 
    // Comments are a bunch of alpha numeric characters with some allowed
    // symbols. Note that new lines cannot appear in comments. Comments
    // end with a blank line.

    // Note that no transformation of the parse string occurs as I do not
    // care for the result of the comments since they have no bearing
    // on program execution.
    """[A-Za-z0-9-_ \.?!\"]*""".r <~ newLine

  /* Parses a number and returns a number */
  def number: Parser[Int] = 
    // Simply parse a series of numbers.
    // The transformation is _.toInt: the _ in this case represents
    // the parsed value (i.e. the string of numbers), and I call toInt
    // on the string to convert it to an Int to be returned.
    """[0-9]+""".r ^^ { _.toInt }

  /* Parses ingredient declaration + list */
  def ingredientList: Parser[List[ChefIngredient]] =
    // Parse the ingredient declaration (the output for it is ignored
    // via ~>), and parse at least 1 ingredient.
    ingredientDecl ~> ingredient.+

  /* Parses the ingredient declaration */
  def ingredientDecl: Parser[String] =
    // Parse the string "Ingredients." at the beginning of a line, then
    // parse at least one new line character.

    // No transformation applied as I do not care what is returned by
    // this parser.
    """^(Ingredients\.)""".r <~ """[\n]+""".r

  /* Parses an ingredient */
  def ingredient: Parser[ChefIngredient] = 
    // Parse an optional number.
    number.? ~ 
    // Parse an optional measure.
    measure.? ~ 
    // Parse the ingredient name, which consists of alpha-numberic 
    // characters and possibly spaces/symbols.
    """[A-Za-z- _]+""".r  <~
    // The use of <~ means the new line characters parsed by this will
    // not appear in the output.
    """[\n]+""".r ^^ { 
      // There are different cases depending on the success of parsing
      // the optional number/measure

      // Failed to parse number/measure: just return the ingredient
      // with its name
      case None ~ None ~ _name => 
        new ChefIngredient(_name, I_EITHER)
      // Parsed a measure: I have to do some analysis on that parsed string
      // to determine which ingredient interpretation to use.
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
      // Parsed an initial value (the number): return the ingredient
      // with that initial value.
      case Some(x) ~ None ~ _name => 
        new ChefIngredient(_name, I_EITHER, x)
      // Parsed both a number and the measure spec: do analysis on the
      // measure string, and return an ingredient based on that analysis
      // as well as the parsed number.
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
    // Parsed either "heaped" or "level". The use of \b on the ends
    // makes it so heaped must be its own word (otherwise it will
    // parse something like "levelup" or "heapedasdf".

    // Note no transformation of the parsed string is necessary as it
    // will be "heaped" or "level", which is what I want to return.
    ("""\bheaped\b""".r | """\blevel\b""".r)

  /* Parses a measure, which may include an option heaped/level */
  def measure: Parser[String] = 
    // Parse the optional heaped/level.
    heapedLevel.? ~ 
    // then parse one of the measure specs possible: the use of \b on the ends
    // makes it parse the measures only if they appear as their own word.
    ("""\bkg\b""".r | """\bg\b""".r | """\bpinches\b""".r | 
    """\bpinch\b""".r | """\bml\b""".r | """\bl\b""".r | """\bdashes\b""".r | 
    """\bdash\b""".r | """\bcups\b""".r | """\bcup\b""".r | 
    """\bteaspoons\b""".r | """\bteaspoon\b""".r | """\btablespoons\b""".r | 
    """\btablespoon\b""".r) ^^ { 
      // If the heaped/level doesn't appear, then just return the measure
      case None ~ _measure => _measure
      // If the heaped/level appears, then return it prepended to the 
      // parsed measure with a space between them.
      case Some(h) ~ _measure => h + " " + _measure 
    }
  
  /* Parses cooking time */
  def cookingTime: Parser[String] = 
    // Parse the cooking time string.
    "Cooking time: " <~ 
    // parse the number
    """[0-9]+""".r <~ 
    // Parse the (optional) time specification. \b used to make sure it parses
    // the spec by itself as a word and not as part of another word.
    ("""\bhours\b""".r | """\bhour\b""".r | """\bminutes\b""".r | 
    """\bminute\b""".r).? <~ 
    // Parse a period, then a newLine (i.e. blank line)
    "." <~ newLine
    // No xform applied as I don't care for the result of this parse.


  /* Parses the oven temperature */
  def ovenTemp: Parser[String] =
    // Parse the specified text, a number, then more specified text
    "Pre-heat oven to " <~ """[0-9]+""".r <~ "degrees Celsius" <~
    // Parse the optional gasmark, a period, then a blank line
    gasMark.? <~ "." <~ newLine
    // No xform applied as I don't care for the result of this parse.

  /* Parses the gas mark specification */
  def gasMark: Parser[Int] = 
    // Parse open parens, "gas mark ", a number, the close parens.

    // No xform applied as I don't care for the result of this parse.
    "(" ~> "gas mark " ~> number <~ ")"

  /* Parses the method declaration */
  def methodDecl: Parser[String] = 
    // Parse Method. at the beginning of some line and at least 1 new line
    // character.
    // No xform applied as I don't care for the result of this parse.
    """^(Method\.)""".r <~ """[\n]+""".r

  /* Parses a Chef statement */
  def chefLine: Parser[ChefLine] = 
    // A chef line is any one of these things below. Note that the order
    // of these things actually matters as it will return the first match
    // on these parser combinators (matters for lines that start with the
    // same token, such as Add)
    (takeLine | takeLineT | putLine | putLine2 | foldLine | foldLine2 | 
    addDryLine | addDryLineT | addDryLine2 |
    addLine | addLineT | addLine2 | 
    removeLine | removeLineT | removeLine2 |
    combineLine | combineLineT | combineLine2 | 
    divideLine | divideLineT | divideLine2 |
    liquefyContentsLine | liquefyContentsLine2 | liquefyLine |
    stirBowlLine | stirBowlLine2 | stirBowlLine3 |
    stirIngredientLine | stirIngredientLine2 |
    mixLine | mixBowlLine | mixBowlLine2 | cleanLine | cleanLineT |
    pourLine | pourLine2 | pourLine3 | pourLine4 |
    verbEndLine2 | verbEndLine | verbLine |
    setLine | serveLine | 
    refrigerateLine | refrigerateLine2 | refrigerateLine3 ) <~ 
    // parse optional new line characters: note the use of <~ means that the
    // new lines will be ignored in the output
    """[\n]?""".r
    // Note I do not need to apply an xform to the parsed result: the
    // lines above return a ChefLine, and sinc I used <~ to ignore parsed new
    // lines, all that will be returned is the ChefLine returned by the
    // line parsers

  /* Parses a Take line */
  def takeLine: Parser[ChefLine] =
    // Parse take; ignored in output due to ~>
    "Take " ~> 
    // Parse an ingredient name, then the following text. The + in front
    // of the words applies to the " " character and not the word itself:
    // it means arbitrary spaces can appear.
    """[A-Za-z- _]+ +from +refrigerator""".r <~ "." ^^ { longString =>
      // get the ingredient name from the long parse since it contains
      // from refrigerator as well
      val fromIndex = longString lastIndexOf "from"
      val onlyIngredient = longString.substring(0, fromIndex).trim
      // return the read line with the ingredient name only
      Read(onlyIngredient)
   }

  /* Parses a Take line, the */
  def takeLineT: Parser[ChefLine] =
    // parse/ignore take
    "Take " ~> 
    // parse ingredient name, then rest of the string with arbitrary whitespace
    // between words
    """[A-Za-z- _]+ +from +the +refrigerator""".r <~ "." ^^ { longString =>
      // get the ingredient name from the long string parsed (contains
      // from the refrigerator
      val fromIndex = longString lastIndexOf "from"
      val onlyIngredient = longString.substring(0, fromIndex).trim
      // return read line
      Read(onlyIngredient)
   }

  /* Parses a Put line */
  def putLine: Parser[ChefLine] = 
    // parse/ignore put, parse ingredient name along with the rest of the line
    "Put " ~> """[A-Za-z- _]+ +into +mixing +bowl""".r ~ number.? <~ "." ^^ {
      // split into cases depending on the parsing of a number
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

  /* Parses an Add line, the */
  def addLineT: Parser[ChefLine] = 
    "Add " ~> """[A-Za-z- _]+ +to +the +mixing +bowl""".r <~ "." ^^ {
      longString =>
        Add(getIngredient(longString, "to"), 1)
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

  /* Parses a Remove line, the */
  def removeLineT: Parser[ChefLine] = 
    "Remove " ~> """[A-Za-z- _]+ +from +the +mixing +bowl""".r <~ "." ^^ {
      longString =>
        Subtract(getIngredient(longString, "from"), 1)
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

  /* Parses a Combine line, the */
  def combineLineT: Parser[ChefLine] = 
    "Combine " ~> """[A-Za-z- _]+ +into +the +mixing +bowl""".r <~ "." ^^ {
      longString =>
        Multiply(getIngredient(longString, "into"), 1)
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

  /* Parses a Divide line */
  def divideLineT: Parser[ChefLine] = 
    "Divide " ~> """[A-Za-z- _]+ +into +the +mixing +bowl""".r <~ "." ^^ {
      longString =>
        Divide(getIngredient(longString, "into"), 1)
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

  /* Parse add dry line, the */
  def addDryLineT: Parser[ChefLine] = 
    """Add +dry +ingredients +to +the +mixing +bowl""".r <~ "." ^^ {
      _ => AddDry(1)
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

  /* Parse stir without bowl */
  def stirBowlLine3: Parser[ChefLine] = 
    """Stir +for""".r ~> number <~ "minutes" <~ "." ^^ {
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

  /* Parse a clean line, the */
  def cleanLineT: Parser[ChefLine] = 
    """Clean +the +mixing +bowl""".r <~ "." ^^ {
      _ => ClearStack(1)
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
        //println(r_name)
        val x = r_name.substring(0, r_name.length - 1).trim
        //println(x)
        Call(x)
    }

  /* Parse a refrigerate line */
  def refrigerateLine: Parser[ChefLine] =
    """Refrigerate *\.""".r ^^ { _ => Return(-1) }

  /* Parse refrigerate with an hour specification */
  def refrigerateLine2: Parser[ChefLine] =
    """Refrigerate +for""".r ~> number <~ "hours" <~ "." ^^ { 
      hour => 
        if (hour <= 1) {
          throw new RuntimeException("hours need to be more than 1")
        }
        Return(hour) 
    }

  /* Parse refrigerate with an hour specification */
  def refrigerateLine3: Parser[ChefLine] =
    """Refrigerate +for""".r ~> number <~ "hour" <~ "." ^^ { 
      hour => 
        if (hour != 1) {
          throw new RuntimeException("hour needs to be 1")
        }

        Return(hour) 
    }

  /* Parses the final serves statement in a recipe */
  def serves: Parser[ChefLine] = 
    """Serves """ ~> number <~ "." ^^ { num => PrintStacks(num) }

  /* Deal with new lines, i.e. require 1 at least */
  def newLine: Parser[String] = 
    // Note this says to parse at least 2 new line characters (which 
    // corresponds to a blank line in text)
    """[\n]{2,}""".r


  /* Helper function for string manipulation.
   * Get ingredient given that last word after the ingredient */
  def getIngredient(uncut: String, lastWord: String) =
    uncut.substring(0, uncut lastIndexOf lastWord).trim
}

