package scalachefredux

import scala.collection.mutable.ListBuffer

/* This class is responsible for the actual running of the program */
class ChefRunner(state: ChefState, text: ChefText) {
  val programState = state
  val programText = text

  /* Run the program starting from line 1 */
  def run = {
    var currentLine = 1
    var inFunction = false

    // get the last line of the main function
    val mainLastLine = programText getEndLine programState.getMainRecipe

    // return stack for function calls
    val returnStack = new ListBuffer[Int]
    // line ends for functions
    val functionEndLineStack = new ListBuffer[Int]

    // initialize the starting ingredients
    programState initializeIngredients programText

    while (currentLine != mainLastLine || inFunction) {
      //println(currentLine)
      var nextLine = programText getLine currentLine
      var jumped = false
      var returned = false

      nextLine match {
        case Read(ingredient) => //print("read " + ingredient);
          programState.take(ingredient)

        case Push(ingredient, bowlNumber) => //println("push " + ingredient);
          programState.pushToBowl(ingredient, bowlNumber)

        case Pop(bowlNumber, ingredient) => //println("pop " + bowlNumber);
          programState.popToIngredient(bowlNumber, ingredient)
        
        case Add(ingredient, bowlNumber) => //println("add " + ingredient);
          programState.addOp(ingredient, bowlNumber)

        case Subtract(ingredient, bowlNumber) => //println("subtract " + ingredient);
          programState.subOp(ingredient, bowlNumber)

        case Multiply(ingredient, bowlNumber) => //println("multiply " + ingredient);
          programState.mulOp(ingredient, bowlNumber)

        case Divide(ingredient, bowlNumber) => //println("divide " + ingredient);
          programState.divOp(ingredient, bowlNumber)

        case AddDry(bowlNumber) => //println("add dry " + bowlNumber);
          programState.addDry(bowlNumber)

        case Liquefy(ingredient) => //println("liquefy " + ingredient);
          programState.liquefyIngredient(ingredient)

        case LiquefyContents(bowlNumber) => //println("liquefy " + bowlNumber);
          programState.liquefyBowl(bowlNumber)

        case Stir(stirNum, bowlNumber) => //println("stir " + stirNum);
          programState.stir(stirNum, bowlNumber)

        case StirIngredient(ingredient, bowlNumber) => //println("stir I" + ingredient);
          programState.stirIngredient(ingredient, bowlNumber)

        case Mix(bowlNumber) => //println("mix " + bowlNumber);
          programState.mix(bowlNumber)

        case ClearStack(bowlNumber) => //println("clear " + bowlNumber);
          programState.clearBowl(bowlNumber)

        case CopyStack(bowlNumber, dishNumber) => 
          //println("copy " + bowlNumber + dishNumber)
          programState.bowlToDish(bowlNumber, dishNumber)

        case LoopStart(verb, check, loopEnd) =>
          if (programState.ingredientIsZero(check)) {
            currentLine = loopEnd
            jumped = true
          }

        case LoopEnd(verb, decrement, loopBegin) => //println("loop end " + decrement);
          programState.decrementIngredient(decrement)
          currentLine = loopBegin
          jumped = true

        case Break(loopEnd) =>
          currentLine = loopEnd
          jumped = true

        case Call(function) => //println("call " + function);
          // change chef state to context switch to function
          programState.contextSwitch(programText, function)
          returnStack prepend (currentLine + 1)

          // "jump" to function
          currentLine = programText getStartLine function
          jumped = true

          // set up return stack + line to "finish" function at
          functionEndLineStack prepend (programText getEndLine function)


          inFunction = true
        
        case Return(-1) => 
          jumped = true
          returned = true

        case Return(num) =>
          programState.printDishes(num)
          jumped = true
          returned = true

        case PrintStacks(numToPrint) => //println("print stacks " + numToPrint);
          programState.printDishes(numToPrint)
      }

      if (!jumped) {
        currentLine += 1
      }

      // check function end line stack; if we're at the end line, jump back
      // and restore state
      if (returned || (!functionEndLineStack.isEmpty && 
          currentLine == functionEndLineStack.head)) {
        if (inFunction) {
          // restore program state
          programState.contextReturn

          functionEndLineStack remove 0
          if (functionEndLineStack.isEmpty) {
            inFunction = false
          }
          // return to line to continue execution at
          currentLine = returnStack remove 0
        } else {
          // end execution
          currentLine = mainLastLine
        }
      }
    }
  }
}
