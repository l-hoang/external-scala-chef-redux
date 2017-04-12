/* Original license for Scala-Chef

The MIT License (MIT)
Copyright (c) 2014-2016 Zane Urbanski, Eric Yu, Loc Hoang

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package scalachefredux


import scala.language.implicitConversions
import scala.language.dynamics

/* Extend this class to start your DSL writing */
class ScalaChefRedux {

  val lineBuilder = new LineBuilder
  val programText = new ChefText
  val programState = new ChefState

  var firstRecipeFound = false
  var firstOp = false

  ///////////
  // TITLE //
  ///////////

  /* Title parsing; Should only appear at the beginning of a program */
  object Title {
    def -(recipeTitle: String) = {
      if (firstRecipeFound) {
        finishLine
      }

      if (recipeTitle == "") {
        throw new RuntimeException("ERROR: Recipe name can't be empty")
      }

      // Tell program text to save the current line as the start of a new
      // function
      programText functionStart recipeTitle
      // go into limbo mode (next thing should be Ingredients declaration)
      lineBuilder.modeLimbo

      if (!firstRecipeFound) {
        programState setMainRecipe recipeTitle
        firstRecipeFound = true
      }
    }
  }

  /* Parses "mixing bowl" */
  object MixingGetter {
    /* mixing bowl by itself = default stack 1 */
    def mixing(b: BowlWord) = {
      lineBuilder setStackNumber1 1
      lineBuilder.setFinished
    }
  }

  /* Parses "bowl <number>" */
  object BowlGetter {
    /* bowl with number = into some particular stack */
    def bowl(bowlNumber: Int) = {
      lineBuilder setStackNumber1 bowlNumber
      lineBuilder.setFinished
    }

    /* bowl with no number = by default stack 1 */
    def bowl = {
      lineBuilder setStackNumber1 1
      lineBuilder.setFinished
    }
  }

  /* Continues parsing with "into mixing" or "into the" */
  object IntoGetter {
    /* "into mixing" leads into "bowl <number>" */
    def into(m: MixingWord) = BowlGetter
    /* "into the" leads into "mixing bowl" */
    def into(t: TheWord) = MixingGetter
  }

  /* Continues parsing with "to mixing" or "to the" */
  object ToGetter {
    /* "to mixing" leads into "bowl <number>" */
    def to(m: MixingWord) = BowlGetter
    /* "to the" leads into "mixing bowl" */
    def to(t: TheWord) = MixingGetter
  }


  /* Take the <ingredient> from the refrigerator */
  object Take {
    def the(ingredient: String) = {
      finishLine

      lineBuilder.assertMethod
      // set ingredient + op
      lineBuilder setString ingredient
      lineBuilder setOp E_TAKE

      FromGetter
    }

    def a(ingredient: String) = the(ingredient)
    def an(ingredient: String) = the(ingredient)
    def some(ingredient: String) = the(ingredient)

    object FromGetter {
      def from(t: TheWord) = RefrigeratorGetter
    }

    object RefrigeratorGetter {
      def refrigerator = lineBuilder.setFinished
    }
  }

  /* The push operation of the language. 
   * Put the <ingredient> into mixing bowl <number>; */
  object Put {
    def the(ingredient: String) = {
      finishLine

      lineBuilder.assertMethod
      // set ingredient + op
      lineBuilder setString ingredient
      lineBuilder setOp E_PUT

      // return the into object
      IntoGetter
    }

    // aliases for "the"
    def a(ingredient: String) = the(ingredient)
    def an(ingredient: String) = the(ingredient)
    def some(ingredient: String) = the(ingredient)


  }

  object Fold {
    def the(ingredient: String) = {
      finishLine

      lineBuilder.assertMethod
      // set ingredient + op
      lineBuilder setString ingredient
      lineBuilder setOp E_FOLD

      // return the into object
      IntoGetter
    }

    // aliases for "the"
    def a(ingredient: String) = the(ingredient)
    def an(ingredient: String) = the(ingredient)
    def some(ingredient: String) = the(ingredient)
  }

  // Add dry ingredients
  // Add dry ingredients (to the) (mixing bowl)
  // Add dry ingredients (to mixing) (bowl <number>)
  object Add {
    def the(ingredient: String) = {
      finishLine

      lineBuilder.assertMethod
      // set ingredient + op
      lineBuilder setString ingredient
      lineBuilder setOp E_ADD
      lineBuilder setStackNumber1 1
      // can end here, so mark finished; there is optional part after it
      lineBuilder.setFinished

      // Optional part can be parsed
      ToGetter
    }
    // aliases for "the"
    def a(ingredient: String) = the(ingredient)
    def an(ingredient: String) = the(ingredient)
    def some(ingredient: String) = the(ingredient)

    def dry(i: IngredientsWord) = {
      finishLine

      lineBuilder.assertMethod
      // set ingredient + op
      lineBuilder setOp E_ADDDRY
      lineBuilder setStackNumber1 1
      // can end here, so mark finished; there is optional part after it
      lineBuilder.setFinished

      ToGetter
    }
  }

  // Remove the <ingredient>
  // Remove the <ingredient> (from mixing) (bowl <num>)
  // Remove the <ingredient> (from the) (mixing bowl)
  object Remove {
    def the(ingredient: String) = {
      finishLine

      lineBuilder.assertMethod
      // set ingredient + op
      lineBuilder setString ingredient
      lineBuilder setOp E_REMOVE
      lineBuilder setStackNumber1 1
      // can end here, so mark finished; there is optional part after it
      lineBuilder.setFinished

      // Optional part can be parsed
      FromGetter
    }

    // aliases for "the"
    def a(ingredient: String) = the(ingredient)
    def an(ingredient: String) = the(ingredient)
    def some(ingredient: String) = the(ingredient)

    object FromGetter {
      /* "from mixing" leads into "bowl <number>" */
      def from(m: MixingWord) = BowlGetter
      /* "from the" leads into "mixing bowl" */
      def from(t: TheWord) = MixingGetter
    }
  }

  /* Combine the <ing> into the mixing bowl
   * Combine the <ing> into mixing bowl <#> */
  object Combine {
    def the(ingredient: String) = {
      finishLine

      lineBuilder.assertMethod
      // set ingredient + op
      lineBuilder setString ingredient
      lineBuilder setOp E_COMBINE
      lineBuilder setStackNumber1 1
      // can end here, so mark finished; there is optional part after it
      lineBuilder.setFinished

      // Optional part can be parsed
      IntoGetter
    }

    // aliases for "the"
    def a(ingredient: String) = the(ingredient)
    def an(ingredient: String) = the(ingredient)
    def some(ingredient: String) = the(ingredient)
  }

  /* Divide the <ing> into the mixing bowl
   * Divide the <ing> into mixing bowl <number> */
  object Divide {
    def the(ingredient: String) = {
      finishLine

      lineBuilder.assertMethod
      // set ingredient + op
      lineBuilder setString ingredient
      lineBuilder setOp E_DIVIDE
      lineBuilder setStackNumber1 1
      // can end here, so mark finished; there is optional part after it
      lineBuilder.setFinished

      // Optional part can be parsed
      IntoGetter
    }

    // aliases for "the"
    def a(ingredient: String) = the(ingredient)
    def an(ingredient: String) = the(ingredient)
    def some(ingredient: String) = the(ingredient)
  }


  /* (Liquefy the contents) (of the) (mixing bowl)
   * (Liquefy the contents) (of mixing) (bowl <number>) */
  object Liquefy {
    def the(ingredient: String) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_LIQUEFY
      lineBuilder setString ingredient

      lineBuilder.setFinished
    }

    def the(c: ContentsWord) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_LIQUEFY_CONTENTS
    
      OfGetter
    }

    object OfGetter {
      def of(t: TheWord) = MixingGetter
      def of(m: MixingWord) = BowlGetter
    }
  }

  // (Stir for <number>) minutes
  // (Stir bowl <number>) (for <number>) minutes
  // (Stir the bowl) (for <number>) minutes
  // (stir <word> ingredient) into mixing bowl 1
  // (stir <word> ingredient) into the mixing bowl
  object Stir {
    def bowl(bowlNumber: Int) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_STIR
      lineBuilder setStackNumber1 bowlNumber

      ForGetter
    }

    def the(b: BowlWord) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_STIR
      lineBuilder setStackNumber1 1
      
      ForGetter
    }

    // for doens't work, unfortunately, so have to use _for
    def _for(num: Int) =  {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_STIR
      lineBuilder setNumber num
      lineBuilder setStackNumber1 1
    
      MinutesGetter
    }

    // aliases for for
    def FOR(num: Int) = _for(num)
    def For(num: Int) = _for(num)
    def fOr(num: Int) = _for(num)
    def f0r(num: Int) = _for(num)
    def fr(num: Int) = _for(num)

    /* Grab the for in the case when a mixing bowl is included in the 
     * instruction */
    object ForGetter {
      def _for(num: Int) = {
        lineBuilder setNumber num
        MinutesGetter
      }

      // aliases
      def FOR(num: Int) = _for(num)
      def For(num: Int) = _for(num)
      def fOr(num: Int) = _for(num)
      def f0r(num: Int) = _for(num)
      def fr(num: Int) = _for(num)
    }

    /* grab minutes */
    object MinutesGetter {
      def minutes = lineBuilder.setFinished
    }

    def the(ingredient: String) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_STIR_I
      lineBuilder setString ingredient

      IntoGetter
    }

    // the aliases
    def a(ingredient: String) = the(ingredient)
    def an(ingredient: String) = the(ingredient)
    def some(ingredient: String) = the(ingredient)
  }



  /* Mix bowl <number> well 
   * Mix the bowl well 
   * Mix well */
  object Mix {
    def the(b: BowlWord) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_MIX;
      lineBuilder setStackNumber1 1

      WellGetter
    }

    def bowl(bowlNumber: Int) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_MIX;
      lineBuilder setStackNumber1 bowlNumber

      WellGetter
    }

    def well = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_MIX;
      lineBuilder setStackNumber1 1
      lineBuilder.setFinished
    }

    object WellGetter {
      def well = lineBuilder.setFinished
    }
  }

  /* Clean up the mixing bowl
   * Clean up mixing bowl <number> */
  object Clean {
    def the(b: BowlWord) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_CLEAN;
      lineBuilder setStackNumber1 1
      lineBuilder.setFinished
    }

    def bowl(bowlNumber: Int) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_CLEAN;
      lineBuilder setStackNumber1 bowlNumber
      lineBuilder.setFinished
    }

    // TODO remove this syntax pattern?
    def up(t: TheWord) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_CLEAN;

      MixingGetter
    }

    def up(m: MixingWord) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_CLEAN

      BowlGetter
    }
  }


  /* Pour the contents (of mixing) (bowl <number>) (into the) (baking dish)
   * Pour the contents (of mixing) (bowl <number>) (into baking) (dish <number>)
   * Pour the contents (of the) (mixing bowl) (into baking) (dish <number>) 
   * Pour the contents (of the) (mixing bowl) (into the) (baking dish) */
  object Pour {
    def the(c: ContentsWord) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_POUR

      PourOfGetter
    }

    object PourOfGetter {
      def of(t: TheWord) = PourMixingGetter
      def of(m: MixingWord) = PourBowlGetter
    }

    object PourMixingGetter {
      def mixing(b: BowlWord) = {
        lineBuilder setStackNumber1 1
        PourIntoGetter

      }
    }

    object PourBowlGetter {
      def bowl(bowlNumber: Int) = {
        lineBuilder setStackNumber1 bowlNumber
        PourIntoGetter
      }
    }

    object PourIntoGetter {
      def into(b: BakingWord) = PourDishGetter
      def into(t: TheWord) = PourBakingGetter
    }

    object PourDishGetter {
      def dish(dishNumber: Int) = {
        lineBuilder setStackNumber2 dishNumber
        lineBuilder.setFinished
      }
    }

    object PourBakingGetter {
      def baking(d: DishWord) = {
        lineBuilder setStackNumber2 1
        lineBuilder.setFinished
      }
    }
  }

  /* convert strings to loop builders */
  implicit def string2Loop(s: String) = new LoopBuilder(s.toLowerCase)

  class LoopBuilder(s: String) {
    val verb = if (s endsWith "e") s + "d" else s + "ed"
    
    def the(ingredient: String) = {
      // until getter => verb end, else verb beginning
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_LOOP;
      lineBuilder setVerb verb
      lineBuilder setString ingredient
      lineBuilder.setFinished

      UntilGetter
    }

    // the aliases
    def a(ingredient: String) = the(ingredient)
    def an(ingredient: String) = the(ingredient)
    def some(ingredient: String) = the(ingredient)


    def until(lVerb: String) = {
      finishLine

      // original verb can be ignored, important one is lVerb
      val finalVerb = lVerb.toLowerCase

      lineBuilder.assertMethod
      lineBuilder setOp E_LOOP_END
      lineBuilder setVerb finalVerb
      lineBuilder.setFinished
    }

    object UntilGetter {
      def until(lVerb: String) = {
        val finalVerb = lVerb.toLowerCase

        lineBuilder setOp E_LOOP_END
        lineBuilder setVerb finalVerb
        lineBuilder.setFinished
      }
    }
  }

  /* Set aside */
  object Set {
    def aside = {
      finishLine
      lineBuilder setOp E_SET
      lineBuilder.setFinished
    }

  }

  /* Serve _with <recipe> */
  object Serve {
    // with is a keyword, so I have to use something else
    def _with(recipe: String) = {
      finishLine

      lineBuilder setOp E_SERVE
      lineBuilder setString recipe
      lineBuilder.setFinished
    }

    // Random aliases you can use
    def wth(recipe: String) = _with(recipe)
    def wit(recipe: String) = _with(recipe)
    def WITH(recipe: String) = _with(recipe)
    def With(recipe: String) = _with(recipe)
    def recipe(r: String) = _with(r)
    def dish(r: String) = _with(r)
    def along(r: String) = _with(r)
    def plus(r: String) = _with(r)
  }

  /* Refrigerate now
   * Refrigerate for <number> hours */
  object Refrigerate {
    def now = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_REFRIGERATE
      lineBuilder setNumber -1
      lineBuilder.setFinished
    }

    def _for(num: Int) = {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_REFRIGERATE
      lineBuilder setNumber num
      
      if (num <= 0) {
        throw new RuntimeException("ERROR: hours can't be negative/0")
      }

      new HourGetter(num)
    }


    class HourGetter(h: Int) {
      def hour = {
        if (h > 1) {
          throw new RuntimeException("ERROR: hour used for more than 1 hour")
        }

        lineBuilder.setFinished
      }

      def hours = {
        if (h == 1) {
          throw new RuntimeException("ERROR: hours used for 1 hour")
        }

        lineBuilder.setFinished
      }
    }

    // aliases
    def FOR(num: Int) = _for(num)
    def For(num: Int) = _for(num)
    def fOr(num: Int) = _for(num)
    def f0r(num: Int) = _for(num)
    def fr(num: Int) = _for(num)
  }


  object Recipe {
    def serves(numberOfDishes: Int) {
      finishLine

      lineBuilder.assertMethod
      lineBuilder setOp E_SERVES
      lineBuilder setNumber numberOfDishes
      lineBuilder.setFinished
    }
  }

  /* Integers will be converted into this class, and the class will then
   * grab ingredient strings. */
  class IngredientGetter(num: Int) extends Dynamic {
    if (num < 0) {
      throw new 
      RuntimeException("ERROR: Ingredients in a recipe declaration cannot be negative.")
    }

    /* Ingredients classified as dry */

    def g(ingredient: String) = {
      lineBuilder.assertIngredient
      programText addIngredient (new ChefIngredient(ingredient, I_DRY, num))
    }

    def kg(ingredient: String) = {
      lineBuilder.assertIngredient
      programText addIngredient (new ChefIngredient(ingredient, I_DRY, num))
    }

    def pinch(ingredient: String) = {
      lineBuilder.assertIngredient

      if (num == 1) 
        programText addIngredient (new ChefIngredient(ingredient, I_DRY, num))
      else
        throw new RuntimeException("ERROR: A pinch means a value of 1")
    }

    def pinches(ingredient: String) = {
      lineBuilder.assertIngredient

      if (num > 1) 
        programText addIngredient (new ChefIngredient(ingredient, I_DRY, num))
      else
        throw new RuntimeException("ERROR: Pinches means a value greater than 1")
    }

    /* Ingredients classified as liquid */

    def ml(ingredient: String) = {
      lineBuilder.assertIngredient
      programText addIngredient (new ChefIngredient(ingredient, I_LIQUID, num))
    } 

    def l(ingredient: String) = {
      lineBuilder.assertIngredient
      programText addIngredient (new ChefIngredient(ingredient, I_LIQUID, num))
    }

    def dash(ingredient: String) = {
      lineBuilder.assertIngredient

      if (num == 1) 
        programText addIngredient (new ChefIngredient(ingredient, I_LIQUID, num))
      else
        throw new RuntimeException("ERROR: A dash means a value of 1")
    }

    def dashes(ingredient: String) = {
      lineBuilder.assertIngredient

      if (num > 1) 
        programText addIngredient (new ChefIngredient(ingredient, I_LIQUID, num))
      else
        throw new RuntimeException("ERROR: Dashes means a value greater than 1")
    }

    /* Ingredients classified as either */

    def cup(ingredient: String) = {
      lineBuilder.assertIngredient

      if (num == 1) 
        programText addIngredient (new ChefIngredient(ingredient, I_EITHER, num))
      else
        throw new RuntimeException("ERROR: Cup means a value of 1")
    }

    def cups(ingredient: String) = {
      lineBuilder.assertIngredient

      if (num > 1) 
        programText addIngredient (new ChefIngredient(ingredient, I_EITHER, num))
      else
        throw new RuntimeException("ERROR: Cups means a value greater than 1")
    }


    def teaspoon(ingredient: String) = {
        lineBuilder.assertIngredient

      if (num == 1) 
        programText addIngredient (new ChefIngredient(ingredient, I_EITHER, num))
      else
        throw new RuntimeException("ERROR: Teaspoon means a value of 1")
    }

    def teaspoons(ingredient: String) = {
      lineBuilder.assertIngredient

      if (num > 1) 
        programText addIngredient (new ChefIngredient(ingredient, I_EITHER, num))
      else
        throw new RuntimeException("ERROR: Teaspoons means a value greater than 1")
    }

    def tablespoon(ingredient: String) = {
      lineBuilder.assertIngredient

      if (num == 1) 
        programText addIngredient (new ChefIngredient(ingredient, I_EITHER, num))
      else
        throw new RuntimeException("ERROR: Tablespoon means a value of 1")
    }

    def tablespoons(ingredient: String) = {
      lineBuilder.assertIngredient

      if (num > 1) 
        programText addIngredient (new ChefIngredient(ingredient, I_EITHER, num))
      else
        throw new RuntimeException("ERROR: Tablespoons means a value greater than 1")
    }

    // stands for "count"
    def ct(ingredient: String) = {
      lineBuilder.assertIngredient
      programText addIngredient (new ChefIngredient(ingredient, I_EITHER, num))
    }
  }

  implicit def int2IngredientGetter(i: Int) = new IngredientGetter(i)

  /* Debug function that will print the lines of the program text */
  def printLines = programText.printLines

  /* "Enjoy your meal;" is the line that tells you to begin running the 
   * program. */
  object Enjoy {
    def your(m: MealWord) = {
      // finish the last line
      finishLine

      // disable the line builder for good
      lineBuilder.modeDone

      // finish the last function
      programText.endFunction
      // do a consistency check 
      programText.consistencyCheck

      // run the program
      val runner = new ChefRunner(programState, programText)
      runner.run
    }
  }


  /* Called to finish a line */
  def finishLine = {
    if (!firstOp) {
      programText addLine lineBuilder.finishLine
    } else {
      firstOp = false
    }
  }

  ///////////////////////
  // Mode change lines //
  ///////////////////////
  /* The purpose of the following is to signify a change in the current "mode"
   * a Chef program is in: mainly Title, Ingredient, or Method */
  /* Mode change to ingredient */
  def Ingredients {
    lineBuilder.modeIngredient
  }

  /* Mode change to method */
  def Method {
    lineBuilder.modeMethod
    firstOp = true
  }
}
