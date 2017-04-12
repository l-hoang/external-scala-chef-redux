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

package programs

import scalachefredux._
import scala.language.postfixOps

object GuessingGame extends ScalaChefRedux {
  def main(args: Array[String]) = {
    Title - "Guessing Salad with Prompt Pudding, More, Less, and Equal"

    Ingredients;

    100 ct "random ingredients";
    1 ct "secret ingredient";
    1 ct "true";
    0 ct "false";
    1 ct "result";
    1 ct "greater";
    1 ct "less";
    1 ct "equal";

    Method; 

    // Set up the random number we need to guess.
    "Amass" the "random ingredients";
      Put some "random ingredients" into the mixing bowl;
    "Amass" the "random ingredients" until "amassed";

    Mix the bowl well;
    Fold the "secret ingredient" into the mixing bowl;
    Clean the bowl;

    "Guess" the "secret ingredient";
      Put some "false" into mixing bowl 2;
      Fold an "equal" into mixing bowl 2; // equal = 0
      Put some "false" into mixing bowl 2;
      Fold a "greater" into mixing bowl 2; // greater = 0
      Put the "true" into mixing bowl 2;
      Fold the "less" into mixing bowl 2; // less = 1

      Serve WITH "Prompt Pudding";

      Take a "guess" from the refrigerator;
      Put the "secret ingredient" into mixing bowl 2;

      Divide the "guess" into mixing bowl 2;
      // result = result of division
      Fold the "result" into mixing bowl 2;

      // guess > actual => result at least 1
      // guess = actual => result is 1
      // guess < actual => result is 0, so loop skip
      "Check" the "result";
        Put some "false" into mixing bowl 2;
        Fold the "less" into mixing bowl 2;
        Put some "true" into mixing bowl 2;
        Fold an "equal" into mixing bowl 2;
        Put the "guess" into mixing bowl 2;
        Remove the "secret ingredient" from mixing bowl 2;
        Fold the "result" into mixing bowl 2;

        "Equalize" the "result";
          Put some "false" into mixing bowl 2;
          Fold an "equal" into mixing bowl 2;
          Put the "true" into mixing bowl 2;
          Fold a "greater" into mixing bowl 2;
          Set aside;
        "Equalize" until "equalized";
        Set aside;
      "Check" until "checked";

      "Lower" the "less";
        Serve _with "More";
        Set aside;
      "Lower" until "lowered";

      "Max" the "greater";
        Serve _with "Less";
        Set aside;
      "Max" until "maxed";

      "Level" the "equal";
        Put some "false" into mixing bowl 2;
        Fold the "secret ingredient" into mixing bowl 2;
        Serve _with "Equal";
        Set aside;
      "Level" until "leveled";
    "Guess" until "guessed";

    Recipe serves 1;

    // Asks for a guess
    Title - "Prompt Pudding";

    Ingredients;

    103 g "white sugar";
    117 ct "eggs";
    101 cups "corn starch";
    115 cups "milk";
    58 ct "bananas";

    Method;
    Clean the bowl;

    Put the "bananas" into the mixing bowl;
    Put the "milk" into the mixing bowl;
    Put the "milk" into the mixing bowl;
    Put the "corn starch" into the mixing bowl;
    Put the "eggs" into the mixing bowl;
    Put the "white sugar" into the mixing bowl;
    Liquefy the contents of the mixing bowl;
    Pour the contents of the mixing bowl into the baking dish; 
    Refrigerate fr 1 hour;

    Title - "More";

    Ingredients;
    10 ct "nl";
    62 ct "up";

    Method;

    Clean the bowl;

    Put the "nl" into the mixing bowl;
    Put the "up" into the mixing bowl;
    Liquefy the contents of the mixing bowl;
    Pour the contents of the mixing bowl into the baking dish; 
    Refrigerate fr 1 hour;

    Title - "Less";

    Ingredients;
    10 ct "nl";
    60 ct "down";

    Method;

    Clean the bowl;

    Put the "nl" into the mixing bowl;
    Put the "down" into the mixing bowl;
    Liquefy the contents of the mixing bowl;
    Pour the contents of the mixing bowl into the baking dish; 
    Refrigerate fr 1 hour;

    Title - "Equal";

    Ingredients;
    10 ct "nl";
    61 ct "eq";

    Method;

    Clean the bowl;

    Put the "nl" into the mixing bowl;
    Put the "eq" into the mixing bowl;
    Liquefy the contents of the mixing bowl;
    Pour the contents of the mixing bowl into the baking dish; 
    Refrigerate fr 1 hour;

    Enjoy your meal;
  }
}
