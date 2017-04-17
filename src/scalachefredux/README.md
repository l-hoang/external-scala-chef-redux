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

#### Example 1

The following code parses "do" and returns it as the value you can access.

```
def doParser: Parser[String] = 
  "do"
```

Note that no transformation is applied. Since "do" itself is a String, 
the compiler will not complain since I specified the return type to be String.

#### Example 2

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

#### Example 3

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

## Parser Combinator Construction: Advanced

The above example combinators are limited in what they can parse: you can only
parse a simple string.

You can add more power to your combinator by using `~`, `~>`, `<~` and other
operators. Additionally, you can use parser combinators within other parser
combinators to form more powerful combinators.

### `~`: Chaining Parse Strings

`~` allows you to chain together different things that you want to parse.

```
def testParser: Parser[Start] = 
  "do" ~ "this"
```

The parser above will chain "do" and "this", and if you have whitespace ignoring
on (see the beginning of my `ChefParser` for details), 
it will be able to parse them even with spaces separating them.

Using `~` makes transformations a bit more complex: we now have 2 (or more) results
that need to be passed into the transformation function if we use one. In this
case, you use case syntax to differentiate between the 2 parse results:

```
def testParser: Parser[Start] = 
  "do" ~ "this" ^^ {
    case d ~ t => 
      d + " " + t
```

`case d ~ t` follows the same construction as the parse on the left: `d` will
correspond to the first result, i.e. "do", and `t`, which is separated from
`d` by the `~`, will correspond to the second result, i.e. `t`.

You can extend this to multiple results:

```
def testParser: Parser[Start] = 
  "do" ~ "this" ~ "task" ~ "right" ^^ {
    case d ~ t ~ _ ~ r => 
      d + " " + t + " " + r
```

Note that not all arguments need to be used: regardless, for each argument
that is passed into the transformation, the case statement must have a "slot"
for it.

### `~>` and `<~`: Parse and Ignore

In some cases, you want to parse something, but you do not want to have it
passed into output. This is where `~>` and `<~` are useful:

```
def testParser: Parser[Start] = 
  "do" ~> "this"
```

This will still parse "do" and "this". However, the final output result is 
only going to be "this" as the result of parsing "do" is ignored due
to `~>`, which says that you ignore the result on the left and take the one
on the right.

Similarly, `<~` ignores the result on the right and takes the one of the left.
The below parser will parse the same thing as the above, except it only 
returns "do".

```
def testParser: Parser[Start] = 
  "do" <~ "this"
```

You can use this in conjuction with `~`.

```
def testParser: Parser[Start] = 
  ("do" ~> "this") ~ ("task" <~ "right") ^^ {
    case t ~ ta => t + " " + ta
```

**You should parenthesize your "main" results along with the ignored
results**. Here, the ignored "do" corresponds with "this", which **isn't**
ignored, and the ignored "right" is grouped with "task". Separating the
"this" and "task" group is the ~. We only have 2 results, then, as reflected
in the case statement: **you should not refer to the ignored results in the
case statement**.

Below is an example of something you do not want to do. It may potentially confuse
the parser and cause errors. **Always parenthesis main results/results separated
by the ~.**

```
def testParser: Parser[Start] = 
  "do" ~> "this" ~ "task" <~ "right" ^^ {
    case t ~ ta => t + " " + ta
```

### Combinators in Combinators

Instead of parsing only strings, you can also use other combinators in
your combinator definition:

```
def doParser: Parser[String] = 
  "do" ^^ { 
    x => x.toUpperCase
  }

def thisParser: Parser[String] =
  "this"

def doThisParser: Parser[String] = 
  doParser ~ thisParser ^^ {
    case x ~ y => x + y
  }
```

The `doThisParser` uses `doParser`, which parses "do" and returns "DO", and the
`thisParser`, which parses/returns "this", to parse both words and return 
"DOthis". The result of the other parse combinators will be passed
into the trasformation function (i.e. "DO" and "this").

You can exert more control over the parser combinators used in your definitions
using particular operators.

#### `?`: Optionality 

The `?` operator will make the parser combinator optional: it can either succeed,
or it can fail and not cause any issues.

```
def doParser: Parser[String] = 
  "do" ^^ { 
    x => x.toUpperCase
  }

def thisParser: Parser[String] =
  "this"

def doThisParser: Parser[String] = 
  doParser ~ thisParser.? ^^ {
    case x ~ None => x 
    case x ~ Some(y) => x + y
  }
```

`doThisParser` can now parse just "do", or "do" and "this". Depending on how the
parse goes, the second result may or may not exist. This is reflected in the
2 case statemnts that now exist in the trasformation function. The first case
occurs if "this" was not parsed: a None object will exist as the second
result. In the case that it was parsed, then the result will be wrapped in a
Some class: to access the actual result, you pattern match with the Some class
as shown above.


#### `*`: Any Number

`*` tells the parser that it can parse nothing or any number of the particular
combinator that it is used on.

```
def doParser: Parser[String] = 
  "do" ^^ { 
    x => x.toUpperCase
  }

def thisParser: Parser[String] =
  "this"

def doThisParser: Parser[String] = 
  doParser ~ thisParser.* ^^ {
    case x ~ listOfThis => 
      val final = x

      for (t <- listOfThis) {
        final = final + " " + t
      }

      final
  }
```

This is able to parse "do", followed by any number of "this"s that may exist.
Note that `thisParser.*` now returns a `List` of Strings as its result. The
list can be empty if no "this" is parsed. Otherwise it can have any number
of "this"s.

#### `+`: At Least One

`+` says that you need to have the parser combinator succeed at least once.


```
def doParser: Parser[String] = 
  "do" ^^ { 
    x => x.toUpperCase
  }

def thisParser: Parser[String] =
  "this"

def doThisParser: Parser[String] = 
  doParser ~ thisParser.+ ^^ {
    case x ~ listOfThis => 
      val final = x

      for (t <- listOfThis) {
        final = final + " " + t
      }

      final
  }
```

This will parse "do" followed by at least one "this". Like `*`, the result
of `thisParser.+` will be a list, except in this case it will have at least
one element due to `+` requiring one exist for the parser to succeed.


## Parser Combinator Construction: Regex

You can use regex in your parser combinators if you extended the RegexParser
class. Simply add a `.r` to your string to convert into a regex expression.

```
def alphaParse: Parser[String] =
  """[A-Za-z]""".r
```

If you are familiar with regex, you will know that this says to parse any
single alpha character. Note the use of `"""`: I don't know how
to explain it well, but it has something to do with formatting in Scala,
and most examples I've seen use it for regex. The important thing to know
is that it's still a String.

Once you are in "regex mode" you can basically do anything that you can 
do in regex. For details, I recommend looking up basic regex tutorials and
the such.

Note `?`, `*`, and `+` exist in regex as well, and they work the same as explained 
above, except you do not need to prepend them with `.`. Additionally, it will 
only apply to the token that directly preceeds it. For example, if I did
`""" +""".r`, it would parse at least 1 space character.

See the ChefParser code from some examples of regex.

## Parser Combinator Construction: Other Things

### Whitespace Specification

You will find this near the beginning of the ChefParser code:

```
  override def skipWhitespace = true
  override val whiteSpace = "[ \t\r\f]+".r
```

This tells to parser to skip white space between parser combinators but not
while within a parser combinator itself. The ``whitespace`` val specifies
what the parser should consider as whitespace to skip.

### Returning Things Other Than Strings

My examples so far return Strings. You can return things other than strings.
Here is an example from ChefParser:

```
def cleanLineT: Parser[ChefLine] = 
  """Clean +the +mixing +bowl""".r <~ "." ^^ {
    _ => ClearStack(1)
  }
```

The return type here is ``ChefLine``. In most cases if you return
something that isn't a string, then you need to have your transformation
function return something that is the type you want to return since by default
most parse results will be Strings. Here, my transformation function
returns ``ClearStack(1)`` which is of type ChefLine.

## Using The Parser

TODO
