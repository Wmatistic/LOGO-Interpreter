# LOGO Interpreter

A LOGO interpreter built with ANTLR4 that produces SVG output. Programs written in LOGO are parsed and executed by a turtle that draws to a scalable vector graphics canvas.

## Features

- **Drawing commands** — `fd`, `bk`, `rt`, `lt`, `pu`, `pd`, `hm`
- **Variables** — `make "x 100`, `:x`
- **Arithmetic** — `+`, `-`, `*`, `/` with full operator precedence and parentheses
- **Floating point** — decimal literals supported in all expressions
- **Comparisons** — `>`, `<`, `>=`, `<=`, `==`, `!=`
- **Conditionals** — `if expr [ stmts ] else [ stmts ]`
- **Loops** — `while expr [ stmts ]`, `rp n [ stmts ]`
- **Procedures** — named procedures with parameters and recursion
- **Color** — indexed color via `sc`, RGB color via `pc r g b`
- **Stroke width** — `sw n`
- **Absolute positioning** — `setx`, `sety`
- **Fill** — `bf` / `ef` (begin fill / end fill) for filled polygon shapes
- **Comments** — `;` single-line comments

## Language Reference

```
fd 100            ; forward
bk 50             ; backward
rt 90             ; turn right (degrees)
lt 45             ; turn left (degrees)
pu                ; pen up
pd                ; pen down
hm                ; return home

make "x 100       ; assign variable
fd :x + 50        ; use variable in expression

sc 2              ; set color by index (0–15)
pc 255 100 0      ; set color by RGB
sw 3              ; set stroke width

setx 350          ; jump to absolute x
sety 200          ; jump to absolute y

rp 4 [ fd 100 rt 90 ]
if :x > 0 [ fd :x ] else [ bk :x ]
while :x > 0 [ fd :x make "x :x - 1 ]

to square :size
    rp 4 [ fd :size rt 90 ]
end
square 100

bf                ; begin fill
rp 3 [ fd 100 lt 120 ]
ef                ; end fill — emits filled polygon
```

## Dependencies

- Java JDK 8 or later
- ANTLR 4

## Build

```bash
CP=$(grep '^CLASSPATH=' "$(which antlr4)" | cut -d= -f2-):.
antlr4 -no-listener -visitor logo.g4
javac -cp $CP *.java
```

## Run

```bash
java -cp $CP Driver < samples/sample1_variables.logo > out.svg
open out.svg
```

An optional first argument sets the canvas size in pixels (default 700):

```bash
java -cp $CP Driver 900 < samples/sample8_sierpinski.logo > out.svg
```

## Samples

| File | Description |
|------|-------------|
| `sample1_variables.logo` | Variables and arithmetic |
| `sample2_conditionals.logo` | `if` / `else` |
| `sample3_while.logo` | `while` loop spiral |
| `sample4_procedures.logo` | Named procedures with parameters |
| `sample5_combined.logo` | All basic features together |
| `sample6_recursive_tree.logo` | Recursive binary tree with variable stroke width |
| `sample7_koch_snowflake.logo` | Koch snowflake fractal (depth 3) |
| `sample8_sierpinski.logo` | Sierpinski triangle fractal (depth 4) |
| `sample9_new_commands.logo` | RGB color, stroke width, absolute positioning |
| `sample10_fill.logo` | Filled shapes with `bf` / `ef` |

## Architecture

| File | Role |
|------|------|
| `logo.g4` | ANTLR4 grammar defining the language |
| `Engine.java` | Abstract LOGO machine (turtle state, fill logic) |
| `SVGEngine.java` | Concrete engine that emits SVG output |
| `SymbolTable.java` | Variable scoping and procedure registry |
| `Visitor.java` | Parse tree visitor that interprets the program |
| `Driver.java` | Entry point — wires lexer, parser, and interpreter |
