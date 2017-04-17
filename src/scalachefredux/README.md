# Parsing Combinator Use/Explanation

The purpose of this is to explain how the parser combinators function both
in general as well as in the way I used it in my own implementation.


## How to Start

The parser is an object that extends one of Scala's existing parser combinator
classes. To begin, add this import to the beginning of your file:

```
import scala.util.parsing.combinator._
```

Then, define your parser object (or class if you plan on making more than on
instance of the parser)

```
object ExampleParser extends RegexParser {
  // Insert parser combinators + other code here
}
```

There are other kinds of parsers besides the RegexParser that you can use. I 
use the RegexParser as it allows me to use regex when doing parsing.

At this point, you can begin to define your parser combinators as functions
in your object/class.

## Parser Combinator Construction: Basics

A parser combinator has the following structure to it:

```
def exampleCombinator: Parser[<Return Type>] = {
    <insert thing to parse here> ^^ 
    <insert trasformation on parsed value here>
}
```

You give the parser combinator a name, and you have it return a Parser
with some type in the square brackets. The type in the square brackets is the
final return type of the thing you parse. What is actually returned is a 
ParseResult object, but within the Result object is a value of the type
you declared.

Within the function, you write what you want the combinator to parse as a
string or as a regex expression. How you specify which will be detailed
later.

Finally, the code after the "^^" is transformation code. It takes the
parse result as input and applies some kind of transformation to it. The
type of the result of this transformation must be the return type specified
in the function signature. The transformation is **optional**.

### Example 1

The following code parses "do" and returns it as the value you can access.

```
def doParser: Parser[String] = 
    "do"
```

Note that no transformation is applied. Since "do" itself is a String, 
the compiler will not complain since I specified the return type to be String.

### Example 2

The following code parses "do" and capitalizes it.

```
def doParser: Parser[String] = 
    "do" ^^ { _.toUpperCase }
```

Here, we parse "do". If parsing it is successful, then we will pass in the 
result of the parse to the specified transformation function in order to
apply some kind of transformation to it. In this case, I want to make "do"
into "DO", so I apply `toUpperCase` to it. The `_` refers to the
pass in result, i.e. "do"; since Strings have the `toUpperCase` method, I can
call it. **This syntax only works if you only have a single parsing result, and 
I believe it will only work if you have a simple transformation such as the
one above.**.
I will get into multiple parsing results later.

Alternatively, you can use this syntax as well:

```
def doParser: Parser[String] = 
    "do" ^^ { x => x.toUpperCase }
```

In this case, you give the passed in result an explicit name `x` that you can
refer to. This will also allow you to make your transformation more complex 
(see below).

### Example 3

The transformation can be more complex than the ones above. 

```
def doParser: Parser[String] = 
    "do" ^^ { 
      x => 
        val test = x + "hello"
        test + "1"
    }
```

Here, I add "hello" and "1" to "do" and return "dohello1". I believe that
you **cannot** use `_` to refer to "do" in this example, so you have
to use this other anonymous function syntax.


## Using The Parser


TODO
