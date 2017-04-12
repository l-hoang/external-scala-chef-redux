package programs

import scalachefredux._
import scala.language.postfixOps

object Tester3 extends ScalaChefRedux {
  def main(args: Array[String]) = {
    Title - "stir";

    // add some ingredients
    Ingredients;

    0 ct "filler";
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
    // put hello into the bowl
    Put some "oil" into the mixing bowl;
    Put some "lard" into the mixing bowl;
    Put some "lard" into the mixing bowl;
    Put the "eggs" into the mixing bowl;
    Put the "haricot beans" into the mixing bowl;

    Put some "oil" into mixing bowl 2;
    Put some "lard" into mixing bowl 2;
    Put some "lard" into mixing bowl 2;
    Put the "eggs" into mixing bowl 2;
    Put the "haricot beans" into mixing bowl 2;

    // make it all text
    Liquefy the contents of the mixing bowl;
    Liquefy the contents of mixing bowl 2;

    // should result in ellHo
    Stir bowl 2 fr 3 minutes;
    Stir the "threes" into mixing bowl 2;

    Pour the contents of the mixing bowl into the baking dish; 
    Pour the contents of mixing bowl 2 into the baking dish; 

    Clean the bowl;

    Put some "oil" into the mixing bowl;
    Put some "lard" into the mixing bowl;
    Put some "lard" into the mixing bowl;
    Put the "eggs" into the mixing bowl;
    Put the "haricot beans" into the mixing bowl;

    Mix the bowl well;
    Liquefy the contents of mixing bowl 1;
    Pour the contents of the mixing bowl into the baking dish; 

    Clean the bowl;

    "Kill" the "wabbit";
      Put the "threes" into mixing bowl 3;
      Fold the "bleh" into mixing bowl 3;

      "Fight" the "bleh";
        Put some "threes" into mixing bowl 1;
        Set aside;
      "yay" the "bleh" until "fighted"
      
      Put some "tofu" into the mixing bowl;
    "Beat" the "wabbit" until "killed";

    Pour the contents of the mixing bowl into baking dish 2;

    Recipe serves 2;

    Enjoy your meal;
  }
}
