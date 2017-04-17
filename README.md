# Scala-Chef-Redux

Scala-Chef-Redux as an external DSL. See 
https://github.com/l-hoang/scala-chef-redux
for original Scala-Chef-Redux.

## Important Notes

* Comments are currently **not optional**.
* The comments must not have any new lines in them (i.e. one paragraph only).
* Method statements **cannot** have more than one new line between them.
* Instead of specifying a mixing bowl with an ordinal identifier, you specify
it by number, e.g. "mixing bowl 1" instead of "the first mixing bowl".
* To be safe, put at least 1 blank line (i.e. 2 new lines) between each "section"
of a recipe (e.g. ingredients, methods, titles, etc.).

## How to Compile/Use

The following assumes your working directly is the `src` directory, and a `bin`
directory exists in the directory above `src`.

There are dependences in the files, so you will have to compile them in an
order that satisfies the dependences. Here is one such order.

```
scalac -d ../bin/ -cp ../bin scalachefredux/Enums.scala
scalac -d ../bin/ -cp ../bin scalachefredux/ChefLines.scala
scalac -d ../bin/ -cp ../bin scalachefredux/ChefHelpers.scala
scalac -d ../bin/ -cp ../bin scalachefredux/ChefText.scala
scalac -d ../bin/ -cp ../bin scalachefredux/ChefState.scala
scalac -d ../bin/ -cp ../bin scalachefredux/LineBuilder.scala
scalac -d ../bin/ -cp ../bin scalachefredux/ChefRunner.scala
scalac -d ../bin/ -cp ../bin scalachefredux/ChefParser.scala
scalac -d ../bin/ -cp ../bin scalachefredux/ScalaChefRedux.scala
```

## Writing/Running an ScalaChefRedux Program (Externally)

The external DSL nature of this implementation allows you to write the
Chef program in a separate file.

The syntax of the program should basically follow the original Chef spec found 
on the website aside from the changes mentioned above in the 
**Important Notes** section.

Once you have written a Chef program, you can run it with the following:

`scala -cp <path to bin> scalachefredux.ScalaChefRedux <chef program here>`

The error messages you will get on failure are admittedly not very helpful,
so if you have an error, please recheck the syntax changes above as well as 
the original Chef syntax.

## Files Overview

### ChefHelpers.scala

Contains the helper classes `ChefIngredient` and `ChefStack`, which
represent a Chef Ingredient and stack for holding ingredients respectively.
There are also helper functions for copying state.

For the external DSL implementation, it also contains the `ChefResult` class
which is used by the parser when returning parse results.

### ChefLines.scala

Contains case class definitions for the different Chef lines (corresponding
to different Chef operations).

### ChefRunner.scala

Contains the ChefRunner class which given a program state and program
text will be responsible for running the program by going through
the text and updating the program state.

### ChefState.scala

Contains the ChefState class which holds all of the "runtime information"
and state for a running Chef program.

### ChefText.scala

Contains the ChefText class which holds the lines that are parsed in order
to be run later.

### Enums.scala

Contains various enumerations (case objects) for the code. Notably contains
the objects for certain words so that they can be parsed.

### LineBuilder.scala

Contains the LineBuilder class which is what builds ChefLines by being
fed information by the main parser.

### ChefParser.scala

Contains the parser for the Chef programs you write. Parses and creates
ChefLines.

### ScalaChefRedux.scala

The main class that puts everything together to run the Chef program.
Uses the ChefParser to parse the lines in order to construct the
inner representation of the Chef program and runs the Chef program.

## How It Works 

The parsing occurs using Scala's parsing combinators. A parsing
combinators is responsible for parsing some kind of token and returning
whatever result you specify it to return, whether it be the parsed
value or a transformation applied to the parsed value. They are particularly
powerful as you can combine parsing combinators together to form a new
parsing combinator that is capable of parsing what its parts are capable of
parsing.

Here are some helpful sources on these parsing combinators and how to use them.

https://wiki.scala-lang.org/display/SW/Parser+Combinators--Getting+Started

https://enear.github.io/2016/03/31/parser-combinators/

https://kerflyn.wordpress.com/2012/08/25/playing-with-scala-parser-combinator/

http://www.scala-lang.org/files/archive/api/current/scala-parser-combinators/scala/util/parsing/combinator/Parsers.html

## Syntax

The syntax is basically the original Chef syntax except for the changes
mentioned in the **Important Notes** section above. This is possible due
to not being limited by internal DSL conventions.
