# Scala-Chef-Redux

Redux of Eric, Zane, and my CS345H's Scala-Chef implementation.
https://github.com/l-hoang/scala-chef

Original Chef spec
http://www.dangermouse.net/esoteric/chef.html

## Main Differences from ScalaChef

* Arguably better organized code
* DSL cleanup instead of trying to strictly follow Chef
* Different syntax
* Different interpreation of how division is done (relative to the stack)
* Apparently better memory usage 

## Main Differences from Chef

* No heaped or level commands
* No cooking time specification
* `<num> <ingredient>` doesn't work; need to do `<num> ct <ingredient>`
* Syntax differences from actual Chef to get around Scala code conventions
and syntax (e.g. "with" and "for" not working due to being keywords)
* Requiring "Enjoy your meal" at the end to start execution

TODO?

## How to Compile

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
scalac -d ../bin/ -cp ../bin scalachefredux/ScalaChefRedux.scala
```

## Writing a ScalaChefRedux Program

The file you write your program in should include the following. The class
path should point to the `bin` directory so that you have access to the
`scalachefredux` class.

```
import scalachefredux._
import scala.language.postfixOps
```

Have your main method extend the ScalaChefRedux class, as in the example
below.

```
object Tester extends ScalaChefRedux {
```

You have access to ScalaChef syntax in this object due to this extension.
See the example programs and the original Chef spec in order to get an idea for
the syntax/how it works.

## Files Overview

Explanation of files located in `src/scalachefredux`.


### ChefHelpers.scala

Contains the helper classes `ChefIngredient` and `ChefStack`, which
represent a Chef Ingredient and stack for holding ingredients respectively.
There are also helper functions for copying state.

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

### ScalaChefRedux.scala

The main parser: extending the ScalaChefRedux class will allow you to write
programs in ScalaChefRedux. Works in conjunction with the LineBuilder
to parse and build lines to provide to the program text.

## How It Works (Syntactically)

The DSL takes advantage of Scala syntactic sugar.

The beginning of lines are represented by Scala objects, and the continuation 
of the line (i.e. the next word) is represented as a function call from the
object. For example `Do this` in Scala translates into `Do.this`. 

Note that if there are more than 3 words in a line, Scala will treat it like
an object calling a function with an argument. For example `A B C` is `A.B(C)`.
Working around this restriction means that you may have to add extra tokens
to some lines to make things come out "evenly". Additionally, this restriction
forces things that you want to be arguments into certain positions in the line.

For lines longer than 3 words, the following structure applies:

`A B C D E F G` = `((A.B(C)).D(E)).F(G)`

Therefore, Scala will attempt to call the function `D` with argument `E` from
the object that is returned by `A.B(C)`. Due to this restriction of sorts,
then, arguments can only be passed in through the 3rd, 5th, 7th, 9th, ... slots
of a line.

Finally, for lines that start with an integer or a string, I use implicit
conversion to change it into a class. At that point, the same theory behind
how the calls work out as explained above applies.

## How It Works (Semantically)

This is going to be a high level overview of how parsing/evaluation works
while trying to avoid the specifics of ScalaChefRedux (as to be useful to
the DSL builders at large and not justp for understanding ScalaChefRedux).

The ScalaChefRedux class has defined keywords of the language as objects
and/or functions. When Scala runs into one of these keywords, it is
interpreted as an object that calls some method with some argument (see
above). The definition of the methods themselves are just hooks into another
class that is responsible for building the lines based on the information
that the parser gives it. The lines are saved with the necessary
information needed to evaluate them.

When parsing is complete, the program accesses the saved lines from
the beginning (or wherever the start point happens to be) and begins
execution based on the current line being accessed. Execution can
update the program state.

## Syntax

Syntax is similar, but not exactly the same as Chef. The operations are the 
ones specified in the Chef spec, so I will not go over them here. I will simply
go over the syntax.

If you see ", that means that they need to be included in the code you write.

http://www.dangermouse.net/esoteric/chef.html

All lines must be followed by a semi-colon for maximum safety (otherwise, 
I do not guarantee the code will function).

`Title - "<insert title here>"`

`Ingredients`

`Method`

`Take the/a/an/some  "<ingredient>" from the refrigerator`


`Put the/a/an/some "<ingredient>" into mixing bowl <number>`

`Put the/a/an/some "<ingredient>" into the mixing bowl`


`Fold the/a/an/some "<ingredient>" into the mixing bowl`

`Fold the/a/an/some "<ingredient>" into mixing bowl <number>`


`Add the/a/an/some "<ingredient>" [to the mixing bowl]`

`Add the/a/an/some "<ingredient>" [to mixing bowl <number>]`


`Remove the/a/an/some "<ingredient>" [from the mixing bowl]`

`Remove the/a/an/some "<ingredient>" [from mixing bowl <number>]`


`Combine the/a/an/some "<ingredient>" [into the mixing bowl]`

`Combine the/a/an/some "<ingredient>" [into mixing bowl <number>]`


`Divide the/a/an/some "<ingredient>" [into the mixing bowl]`

`Divide the/a/an/some "<ingredient>" [into mixing bowl <number>]` 


`Add dry ingredients to the mixing bowl`

`Add dry ingredients to mixing bowl <number>`


`Liquefy the "<ingredient>"`

`Liquefy the contents of the mixing bowl`

`Liquefy the contents of mixing bowl <number>`


`Stir for <number> minutes`

`Stir bowl <number> _for <number> minutes`

`Stir the bowl _for <number> minutes`

`Stir the/a/an/some "<ingredient>" into the mixing bowl`

`Stir the/a/an/some "<ingredient>" into mixing bowl <number>`


`Mix the bowl well` 

`Mix bowl <number> well`


`Clean the bowl`

`Clean bowl <number>` 


`Pour the contents of the mixing bowl into the baking dish`

`Pour the contents of mixing bowl <number> into the baking dish`

`Pour the contents of the mixing bowl into baking dish <number>`

`Pour the contents of mixing bowl <number> into baking dish <number>`


`"<verb>" the/a/an/some "<ingredient>"`

`"<verb>" the/a/an/some "<ingredient>" until "<verbed>"`

`"<verb>" until "<verbed>"`


`Set aside`


`Serve _with "<auxiliary-recipe>"`


`Refrigerate now`

`Refrigerate for <number> hours`


`Enjoy your meal` (should appear at the end to start the program execution)

## Desugared Syntax

This section gives you an idea of how these calls are actually changed
together.

If you check the actual code, you will find that I am returning an object
with the next method defined. For exaple, for `(Take.the(ingredient)).from(the)`,
you will find that I return an object that has a `from` method defined.

To parse the tokens of the line (e.g. the), I define an object that corresponds
to said token and just have the argument of the function that takes the token
be the type of that token.

Note that there are cases where I return an object to continue parsing
optional things in a line. In order to prevent Scala from trying to grab
the first token in the next line as the argument (which it will do if
you don't put a blank line between 2 lines), I recommend above that all
lines end with a semi-colon since it will shut down the syntactic sugar
in some sense.


`Title.-(<insert title here>)`

`Ingredients` (It's a function)

`Method` (It's a function)


`((Take.the(<ingredient>)).from(the)).refrigerator`


`((Put.the(<ingredient>)).into(mixing)).bowl(<number>)`

`((Put.the(<ingredient>)).into(the)).mixing(bowl)`


`((Fold.the(<ingredient>)).into(the)).mixing(bowl)`

`((Fold.the(<ingredient>)).into(mixing)).bowl(<number>)`


`((Add.the(<ingredient>)).to(the)).mixing(bowl)`

`((Add.the(<ingredient>)).to(mixing)).bowl(<number>)`


`((Remove.the(<ingredient>)).from(the)).mixing(bowl)`

`((Remove.the(<ingredient>)).from(mixing)).bowl(<number>)`


`((Combine.the(<ingredient>)).into(the)).mixing(bowl)`

`((Combine.the(<ingredient>)).into(mixing)).bowl(<number>)`


`((Divide.the(<ingredient>)).into(the)).mixing(bowl)`

`((Divide.the(<ingredient>)).into(mixing)).bowl(<number>)` 


`((Add.dry(ingredients)).to(the)).mixing(bowl)`

`((Add.dry(ingredients)).to(mixing)).bowl(<number>)`


`Liquefy.the(<ingredient>)`

`((Liquefy.the(contents)).of(the)).mixing(bowl)`

`((Liquefy.the(contents)).of(mixing)).bowl(<number>)`


`(Stir.for(<number>)).minutes`

`((Stir.bowl(<number>))._for(<number>)).minutes`

`((Stir.the(bowl))._for(<number>)).minutes`

`((Stir.the(<ingredient>)).into(the)).mixing(bowl)`

`((Stir.the(<ingredient>)).into(mixing)).bowl(<number>)`


`(Mix.the(bowl)).well` 

`(Mix.bowl(<number>)).well`


`Clean.the(bowl)`

`Clean.bowl(<number>)`


`((((Pour.the(contents)).of(the)).mixing(bowl)).into(the)).baking(dish)`

`((((Pour.the(contents)).of(mixing)).bowl(<number>)).into(the)).baking(dish)`

`((((Pour.the(contents)).of(the)).mixing(bowl)).into(baking)).dish(<number>)`

`((((Pour.the(contents)).of(mixing)).bowl(<number>)).into(baking)).dish(<number>)`


An implicit converts the string (verb) into a class with the appropriate
methods.

`<verb>.the(<ingredient>)`

`(<verb>.the(<ingredient>)).until(<verbed>)"`

`<verb>.until(<verbed>)`


`Set.aside`


`Serve._with(<auxiliary-recipe>)"`


`Refrigerate.now`

`(Refrigerate.for(<number>).hours)`


`Enjoy.your(meal)`

