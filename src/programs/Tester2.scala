/* Fixed version of the Fibonacci program from the Chef website taken from the 
 * original Scala Chef */
package programs

import scalachefredux._
import scala.language.postfixOps

object Tester2 extends ScalaChefRedux {
  def main(args: Array[String]) = {
    Title - "Recipe 1";

    // add some ingredients
    Ingredients;
    6 g "flour";
    250 g "butter";
    44 ct "eggs";
    Method;

    Liquefy the "eggs";
    "Sift" the "flour";
    Put the "flour" into the mixing bowl;
    Serve wth "Recipe 2";
    Stir fr 1 minutes;
    Fold the "butter" into the mixing bowl;
    Put the "eggs" into the mixing bowl;
    "Rub" the "flour" until "sifted";
    Fold the "butter" into the mixing bowl;
    Pour the contents of the mixing bowl into the baking dish; 

    Recipe serves 1;

    // hello world
    Title - "Recipe 2";

    Ingredients;
    1 cup "white sugar";
    1 cup "brown sugar";
    1 ct "vanilla bean";
    1 cup "dummies";

    Method;
    Fold the "white sugar" into the mixing bowl;
    Put the "white sugar" into the mixing bowl;
    Fold the "brown sugar" into the mixing bowl;
    Clean the bowl;
    Put the "white sugar" into the mixing bowl;
    Remove the "vanilla bean";
    Fold the "white sugar" into the mixing bowl;
    "Melt" the "white sugar";

    Put the "white sugar" into the mixing bowl;
    Remove the "vanilla bean" from the mixing bowl;
    Fold the "white sugar" into the mixing bowl;

    // != 2
    "Caramelise" the "white sugar";
    Put the "white sugar" into the mixing bowl;
    Serve wth "Recipe 2";
    Fold the "brown sugar" into the mixing bowl;
    Fold the "dummies" into the mixing bowl;

    Put the "white sugar" into the mixing bowl;

    Add the "vanilla bean";

    Serve wth "Recipe 2";

    Add the "brown sugar";

    Fold the "white sugar" into the mixing bowl;

    Fold the "dummies" into the mixing bowl;

    Put the "white sugar" into the mixing bowl;

    Refrigerate now;

    "Heat" the "white sugar" until "caramelised";

    Put the "vanilla bean" into the mixing bowl;

    Refrigerate now;

    "Heat" the "white sugar" until "melted";

    Put the "vanilla bean" into the mixing bowl;

    Refrigerate now;

    Enjoy your meal;
  }

}
