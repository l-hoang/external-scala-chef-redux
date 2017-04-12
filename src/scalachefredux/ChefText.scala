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

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.immutable

/* Holds Chef lines as they are build by the line builder */
class ChefText {
  /* Stores function start line/end line */
  class FunctionInfo {
    var startLine = -1
    var endLine = -1

    /* Set the start line of a function. It's the first line that the function
     * needs to run */
    def setStartLine(s: Int) = {
      startLine = s
    }

    /* Set the end line of a function. It's the first line of the NEXT function
     * (or a line that doesn't correspond to any function if it's the
     * end of the Chef program. */
    def setEndLine(e: Int) = {
      endLine = e
    }

    /* Get the start line */
    def getStartLine = startLine

    /* Get the end line */
    def getEndLine = endLine
  }

  class LoopInfo {
    var startLine = -1
    var endLine = -1
    var ingredientToCheck = ""
    var ingredientToDecrement = ""

    /* Set loop start */
    def setStartLine(s: Int) = {
      startLine = s
    }

    /* Set loop end (i.e. line AFTER loop) */
    def setEndLine(e: Int) = {
      endLine = e
    }

    /* Set the ingredient to check */
    def setToCheck(s: String) = ingredientToCheck = s

    /* Set the ingredient to decrement */
    def setToDecrement(s: String) = ingredientToDecrement = s

    /* Get the start line */
    def getStartLine = startLine
    /* Get the end line */
    def getEndLine = endLine

    /* Get ingredient to check */
    def getToCheck = ingredientToCheck

    /* Get ingredient to decrement */
    def getToDecrement = ingredientToDecrement
  }

  // start at line 1
  var currentLine = 1 

  // current function being parsed
  var currentFunction = ""

  // Maps recipe to the ingredients they have/start with
  val recipeIngredients = new mutable.HashMap[String, 
                          mutable.HashMap[String, ChefIngredient]]

  // Maps line numbers to actual ChefLines operations
  val lines = new mutable.HashMap[Int, ChefLine]
  // Maps function names to start/end
  val functions = new mutable.HashMap[String, FunctionInfo]
  // Maps verbs to loop info
  val loops = new mutable.HashMap[String, LoopInfo]
  // stack of loops currently active
  val loopStack = new ListBuffer[String]
  // Maps verbs to a break queue
  val breakQueue = new mutable.HashMap[String, ListBuffer[Int]]

  ///////////
  // Lines //
  ///////////
  /* Functions to deal with saving Chef Lines and such */

  /* Adds a Chef Line to the program text. */
  def addLine(newLine: ChefLine) = {
    lines(currentLine) = newLine 

    newLine match {
      case LoopStart(heldVerb, ingredientToCheck, -1) =>
        val newLoop = new LoopInfo
        newLoop setStartLine currentLine
        newLoop setToCheck ingredientToCheck
        if (loops contains heldVerb) {
          throw new RuntimeException("ERROR: " + heldVerb + " already used in loop")
        }
        loops(heldVerb) = newLoop
        // add to currently open loops
        loopStack prepend heldVerb
        // intialize break stack for this loop
        breakQueue(heldVerb) = new ListBuffer
      case Break(-1) =>
        // add to current loop's break stack
        breakQueue(loopStack.head).prepend(currentLine)
      case LoopEnd(heldVerb, ingredientToDecrement, -1) =>
        val loopInfo = loops(heldVerb)

        if (ingredientToDecrement != "") {
          loopInfo setToDecrement ingredientToDecrement
        }
        loopInfo setEndLine (currentLine + 1)

        if ((loopStack remove 0) != heldVerb) {
          throw new RuntimeException("ERROR: ending different loop")
        }

        // go over break stack and update the lines accordingly
        for (lineNumber <- breakQueue(heldVerb)) {
          lines(lineNumber) = Break(currentLine + 1)
        }

        breakQueue remove heldVerb

        // update the loop start line
        lines(loopInfo.getStartLine) = LoopStart(heldVerb, loopInfo.getToCheck,
                                       currentLine + 1)

        // update the loop end line
        lines(currentLine) = LoopEnd(heldVerb, ingredientToDecrement, 
                                     loopInfo.getStartLine)
      case _ => // do nothing
    }

    //println(newLine)
    currentLine += 1
  }

  /* Get the requested line number */
  def getLine(lineNumber: Int) = lines(lineNumber)

  /* Add a starting ingredient to the current function */
  def addIngredient(newIngredient: ChefIngredient) = {
    val ingredientName = newIngredient.ingredientName
    
    if (recipeIngredients(currentFunction) contains ingredientName) {
      throw new 
      RuntimeException("ERROR: Redeclaring an already existing ingredient.")
    }

    recipeIngredients(currentFunction)(ingredientName) = newIngredient
  }

  ///////////////
  // Functions //
  ///////////////
  /* Functions that deal with the functions of a Chef Program */

  /* Given a function name, mark the current line number as the start line
   * of the function */
  def functionStart(functionName: String) = {
    if (functions contains functionName) {
      throw new RuntimeException("ERROR: Redeclaring an already existing recipe.")
    }

    // if this is not the first function being declared, save the end of the
    // last function as the current line number
    if (!(currentFunction == "")) {
      functions(currentFunction) setEndLine currentLine
    }

    functions(functionName) = new FunctionInfo
    functions(functionName) setStartLine currentLine
    currentFunction = functionName

    // initialize start ingredients for this recipe
    recipeIngredients(functionName) = new mutable.HashMap[String, ChefIngredient]
  }

  /* Called at the end of parsing. Deals with saving the end of the last 
   * function. */
  def endFunction = {
    functions(currentFunction) setEndLine currentLine
  }

  /* Get the function start line for some function */
  def getStartLine(functionName: String) = {
    functions(functionName).getStartLine
  }

  /* Get the function end line for some function */
  def getEndLine(functionName: String) = {
    functions(functionName).getEndLine
  }

  /* Return a copy of a recipe's starting ingredients */
  def getStartingIngredients(functionName: String) = 
    HelperFunctions.deepCopyIngredients(recipeIngredients(functionName))

  ///////////
  // Other //
  ///////////
  /* Things that deal with other things that should/need to be done */

  /* Make sure all of the lines/info in the text are consistent (e.g. all 
   * functions have a start and an end) */
  def consistencyCheck = {
    for (i <- functions.keys) {
      if (functions(i).getStartLine == -1 || functions(i).getEndLine == -1) {
        throw new RuntimeException("ERROR: recipe " + i + " inconsistent")
      }
    }

    if (!breakQueue.isEmpty) {
      throw new RuntimeException("ERROR: break queue not empty")
    }

    if (!loopStack.isEmpty) {
      throw new RuntimeException("ERROR: loop stack not empty")
    }
  }

  /* Print all the Chef lines contained in this object. */
  def printLines = {
    for (i <- 1 to (currentLine - 1)) {
      //println("Line " + i + ": " + lines(i))
    }
  }
}
