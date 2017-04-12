package programs

import scalachefredux._
import scala.language.postfixOps

object Tester4 extends ScalaChefRedux {
  def main(args: Array[String]) = {
    Title - "func1";

    // add some ingredients
    Ingredients;

    0 ct "fillera";
    1 ct "tofu";
    72 g "haricot beans" ; // dry
    101 ct "eggs";
    108 g "lard"; // dry
    111 cups "oil";
    32 ct "zucchinis";
    119 ml "water";
    114 g "red salmon"; // dry
    100 g "dijon mustard"; // dry
    33 ct "potatoes";
    3 ct "threes";

    10 ct "wabbit";
    3 ct "bleh";

    Method;

    Serve _with "func2";
    Pour the contents of the mixing bowl into the baking dish; 
    Recipe serves 1;

    //Put the "oil" into the mixing bowl;

    Title - "func2";

    Ingredients;

    0 ct "filler";
    1 ct "one";
    2 ct "two";
    3 ct "three";

    Method;

    Put the "filler" into the mixing bowl;
    Put the "one" into the mixing bowl;
    Pour the contents of the mixing bowl into the baking dish; 
    //Refrigerate _for 1 hour;
    Refrigerate now;
    Put the "two" into the mixing bowl;
    Put the "three" into the mixing bowl;

    Enjoy your meal;
  }
}
