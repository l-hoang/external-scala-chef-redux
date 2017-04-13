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

/* This file holds helper classes that should be globally visible. They aid
 * in various functions. */

package scalachefredux

import scala.collection.mutable
import scala.collection.mutable.HashMap
import java.util.ArrayDeque

/* Holds the results of the parse of a single Chef recipe */
class ParseResult {

}

class ChefIngredient(name: String, interpretation: IState, initValue: Int = -1, 
                     copy: Boolean = false, cInitialized: Boolean = false) {
/* Class that represents a Chef ingredient. Has the ingredient quantity as well
 * as its interpretation. */

  def ingredientName = name
  var currentInterpretation = interpretation

  var initialized = false
  var quantity = 0

  if (initValue != -1) {
    initialized = true
    quantity = initValue
  }

  /* if this ingredient was made from a copy, then take its passed in 
   * values */
  if (copy) {
    quantity = initValue
    initialized = cInitialized
  }

  /* Return a new copy of the ingredient */
  def deepCopy = {
    new ChefIngredient(ingredientName, currentInterpretation, quantity, true,
                       initialized)
  }

  /////////////
  // Setters //
  /////////////

  def setValue(newValue: Int) = {
  /* Set the quantity of this ingredient */
    quantity = newValue
    initialized = true
  }

  def setInterpretation(newInterpretation: IState) = {
  /* Change the intrepretation of this ingredient */
    currentInterpretation = newInterpretation
  }

  /////////////
  // Getters //
  /////////////

  /* Return the current interpretation of this ingredient */
  def getInterpretation = currentInterpretation

  def asNumber = {
  /* Return the value of this ingredient as a number */
    assertInitialized
    quantity
  }

  def asChar = {
  /* Return the value of this ingredient as a Unicode char */
    assertInitialized
    quantity.toChar
  }

  ////////////////
  // Assertions //
  ////////////////

  def assertInitialized = {
  /* Assert that this ingredient has been assigned a value */
    if (!initialized) {
      throw new RuntimeException("ERROR: ingredient not initialized")
    }
  }
}

/* A wrapper class for a Java deque that is used to represent a Chef stack 
 * (dish or bowl) */
class ChefStack {
  val javaDeque = new ArrayDeque[ChefIngredient]

  /* put at front of deque */
  def push(i: ChefIngredient) = javaDeque push i
  /* pop from the front of the deque */
  def pop = javaDeque.pop

  /* Change the interpretation of all ingredients in this stack to liquid */
  def liquefy = {
    val thisIterator = javaDeque.iterator

    while (thisIterator.hasNext) {
      (thisIterator.next) setInterpretation I_LIQUID
    }
  }

  /* Add to top element of this stack */
  def add(toAdd: Int) = {
    if (javaDeque.isEmpty) {
      throw new RuntimeException("ERROR: can't add with an empty bowl")
    }

    val ingredient = javaDeque.peek
    ingredient setValue (ingredient.asNumber + toAdd)
  }

  /* Subtract from top element of this stack */
  def subtract(toSubtract: Int) = {
    if (javaDeque.isEmpty) {
      throw new RuntimeException("ERROR: can't subtract with an empty bowl")
    }

    val ingredient = javaDeque.peek
    ingredient setValue (ingredient.asNumber - toSubtract)
  }

  /* Multiply with top element of this stack */
  def multiply(toMultiply: Int) = {
    if (javaDeque.isEmpty) {
      throw new RuntimeException("ERROR: can't multiply with an empty bowl")
    }

    val ingredient = javaDeque.peek
    ingredient setValue (ingredient.asNumber * toMultiply)
  }

  /* Divide into top element of this stack */
  def divide(toDivide: Int) = {
    if (javaDeque.isEmpty) {
      throw new RuntimeException("ERROR: can't divide with an empty bowl")
    }

    val ingredient = javaDeque.peek
    // TODO make it the other way around?
    ingredient setValue (toDivide / ingredient.asNumber)
  }

  /* Add all dry ingredients in this stack and push as a new ingredient */
  //def addDry = {
  //  var total = 0

  //  val dequeIter = javaDeque.iterator

  //  while (dequeIter.hasNext) {
  //    val i = dequeIter.next
  //    
  //    total += (if (i.getInterpretation == I_DRY) i.asNumber else 0)
  //  }

  //  javaDeque push (new ChefIngredient("dry", I_DRY, total))
  //}

  /* Move an ingredient stirNum places down the stack */
  def stir(stirNum: Int) = {
    if (javaDeque.size <= 1 || stirNum <= 0) {
      // do nothing
    } else if (stirNum >= (javaDeque.size - 1)) {
      // move top to bottom
      javaDeque add javaDeque.pop
    } else {
      val originalSize = javaDeque.size
      val topI = javaDeque.pop

      // number of things left in the stack
      val numI = javaDeque.size
      val numAfterStir = numI - stirNum

      for (i <- 1 to stirNum) {
        javaDeque add javaDeque.pop
      }

      javaDeque add topI

      for (i <- 1 to numAfterStir) {
        javaDeque add javaDeque.pop
      }

      if (originalSize != javaDeque.size) {
        throw new RuntimeException("ERROR: stir error")
      }
    }
  }

  /* Randomize the order of the stack */
  def mix = {
    if (javaDeque.size > 1) {
      val buffer = new mutable.ArrayBuffer[ChefIngredient]

      while (!javaDeque.isEmpty) {
        buffer append javaDeque.pop
      }

      val shuffled = (new scala.util.Random) shuffle buffer

      for (i <- shuffled) {
        javaDeque add i
      }
    }
    // do nothing otherwise since mixing a stack with 1 or 0 is pointless
  }

  /* Empty the stack */
  def clean = {
    javaDeque.clear
  }

  /* Make this ChefStack a deep copy of the stack passed in */
  def deepCopyInto(otherStack: ChefStack) = {
    if(!javaDeque.isEmpty) {
      throw new RuntimeException("ERROR: can only copy into this stack if empty")
    }

    val otherIterator = otherStack.iterator

    while (otherIterator.hasNext) {
      javaDeque add (otherIterator.next.deepCopy)
    }
  }

  /* Return an iterator to the Java deque (order is same as popping it) */
  def iterator = javaDeque.iterator
  /* Return an backward iterator to the Java deque (pop from bottom) */
  def descendingIterator = javaDeque.descendingIterator
  /* Tell whether or not this stack is empty */
  def empty = javaDeque.isEmpty
}

/* Contains helper functions that are used throughout Scala-Chef redux */
object HelperFunctions {
  /* Returns a deep copy of a mutable.HashMap holding ingredients */
  def deepCopyIngredients(toCopy: mutable.HashMap[String, ChefIngredient]) = {
    val mapIterator = toCopy.keys
  
    val copy = new mutable.HashMap[String, ChefIngredient] 
  
    for (key <- mapIterator) {
      copy(key) = toCopy(key).deepCopy
    }
  
    copy
  }

  /* Returns a deep copy of a HashMap holding stacks */
  def deepCopyStacks(toCopy: HashMap[Int, ChefStack]) = {
    val copy = new HashMap[Int, ChefStack]

    for (key <- toCopy.keys) {
      val stackCopy = new ChefStack
      stackCopy.deepCopyInto(toCopy(key))
      copy(key) = stackCopy
    }

    copy
  }
}
