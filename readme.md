Carballo Chess Engine
=====================
[![Release](https://img.shields.io/github/v/release/albertoruibal/carballo.svg)](https://github.com/albertoruibal/carballo/packages)

Carballo (the Galician word for Oak, it's all about search trees) is an Open Source Java chess engine with three interfaces:

* UCI: a text interface for chess GUIs: https://github.com/albertoruibal/carballo/raw/master/carballo-uci-1.8.tgz
* HTML5: developed with Google Web Toolkit (GWT) using the Vectomatic SVG library: http://www.mobialia.com/webchessgwt
* Desktop: a Swing application

It is organized into modules:

* Core: the chess engine
* Jse: the Java Standard Edition version with the UCI interface and JUnit tests
* Gwt: components needed for the GWT GUI
* GwtGui: an HTML5 interface developed by Lukas Laag; it depends on Core and Gwt
* Swing: the swing application code, it depends on Core and Jse

It is licensed under GPLv3 and the source code is hosted at https://github.com/albertoruibal/carballo.
You are free to use, distribute or modify the code, we ask for a mention to the original authors and/or a link to our pages.

Features
========

* UCI interface for chess GUIs like Arena or SCID
* It includes a great GWT interface by Lukas Laag and a Java Swing GUI
* Based on bitboards with a magic bitboard move generator, it also includes code for magic number generation
* Move iterator sorting moves with four killer move slots, Static Exchange Evaluation (SEE), Most Valuable Victim/Least Valuable Aggressor (MVV/LVA) and history heuristic
* PVS searcher
* Aspiration window, moves only one border of the window if it falls out
* Transposition Table (TT) with Zobrist keys (it uses two Zobrist keys per board to avoid collisions) and multiprobe
* Quiescent Search (QS) with only good or equal captures (according to SEE) and limited check generation
* Internal Iterative Deepening to improve sorting
* Extensions: Check (only with positive SEE), pawn push, mate threat and singular move
* Reductions: Late Move Reductions (LMR)
* Pruning: Null move pruning, static null move pruning, futility pruning and history pruning
* Pluggable evaluator function, distinct functions provided: the Simplified Evaluator Function, other Complete and other Experimental
* Selectable ELO level with a UCI parameter
* Supports Chess960
* Polyglot opening book support; the code includes Fruit's Small Book
* FEN notation import/export support, also EPD support for testing
* JUnit is used for testing, with multiple test suites provided (Perft, BS2830, BT2630, LCTII, WinAtChess, etc.)

Test results on an Intel Xeon CPU limited to 2.0GHz for consistency:

| Test suite       | Time per position | Version 1.8 | Version 1.7 |
| ---------------- | -----------------:| -----------:| -----------:|
| WinAtChess (New) |          1 second |     293/300 |     293/300 |
| SilentButDeadly  |          1 second |     125/134 |     125/134 |
| ECMGCP           |          1 second |     113/183 |     112/183 |
| ECMGCP           |         5 seconds |     156/183 |     154/183 |
| Arasan 19a       |        60 seconds |      52/200 |      52/200 |

Its real strength is about 2400 ELO points. You can check its tournament rankings at http://www.computerchess.org.uk/ccrl/.

Authors
=======

* Alberto Alonso Ruibal: http://www.alonsoruibal.com
* Lukas Laag, developer of a great GWT SVG library (http://www.vectomatic.org) and the Carballo GWT interface

Building
========

Carballo uses the Gradle build system and the gradle wrapper.

Build all the jars and install them to your local Maven repository:
```
./gradlew publishToMavenLocal
```

UCI Interface
-------------

Build the UCI interface (creates a carballo-${version}.jar in jse/):
```
cd jse
../gradlew proguard
```
Running the UCI interface:
```
cd jse
java -jar carballo-1.8.jar
```

HTML5 Interface
-------------

Build the GWT interface:
```
cd gwtgui
../gradlew compileGwt
```
Running the GWT interface:
```
cd gwtgui
../gradlew appRun
```
and access with your web browser to http://localhost:8080/chess/

Desktop Interface
-----------------

Building the Swing interface (creates a carballo-swing-${version}.jar in swing/):
```
cd swing
../gradlew proguard
```
Running the Swing interface:
```
cd swing
java -jar carballo-swing-1.8.jar
```

Testing
=======

Run tests:
```
./gradlew test
```
Run the "Win at Chess" test suite:
```
cd jse
../gradlew slowTest --tests=WinAtChessTest
```
Run the "Silent but Deadly" suite:
```
cd jse
../gradlew slowTest --tests=SilentButDeadlyTest
```
