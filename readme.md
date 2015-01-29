Carballo Chess Engine
=====================

Carballo (the galician word for Oak, it's all about search trees) is an Open Source Java and C#
chess engine with Applet and GWT (HTML5) interfaces. It is used in the Mobialia Chess apps.

It's organized into modules:

* Core: the chess engine
* JSE: the Java Standard Edition version with the UCI interface and unit tests
* Applet: the applet code, it depends on Core and Jse
* GWT: components needed for the GWT GUI
* GWTGUI: an HTML5 interface developed by Lukas Laag, it depends on Core and GWT
* CSharp: a conversion of the Core source code to C# using Sharpen

Links:

* Source code: https://github.com/albertoruibal/carballo
* GWT interface: http://www.mobialia.com/webchessgwt
* Applet interface: http://www.mobialia.com/webchess
* UCI binary: https://github.com/albertoruibal/carballo/raw/master/carballo-uci.tgz

It is licensed under GPLv3, and you are free to use, distribute or modify the code, we ask for a mention to the original authors and/or a link to our pages.

Features
========

* Simple and clear code
* It includes a great GWT interface by Lukas Laag
* Also a Java Applet GUI
* JUnit used for testing, multiple test suites provided (Perft, BT2630, LCTII, WAC, etc.)
* Based on Bitboards
* State-of-the-art magic bitboard move generator (doubles the basic move generator speed!), also code for magic number generation
* PVS searcher
* Iterative deepening
* Aspiration window, moves only one border of the window if falls out
* Transposition Table (TT) with Zobrist Keys (two zobrist keys per board, to avoid collisions) and multiprobe/two tier
* Quiescent search with only good captures (according to SEE) and limited check generation
* Move sorting: two killer move slots, SEE, MVV/LVA and history heuristic
* Also Internal Iterative Deepening to improve sorting
* Fractional Extensions: check, pawn push and passed pawns, mate threat, recapture (2 = 1PLY)
* Reductions: Late Move Reductions (LMR)
* Pruning: Null Move Pruning, Static Null Move Pruning, Futility Pruning and Aggressive Futility Pruning
* Polyglot Opening Book support; in the code I include Fruit's Small Book
* FEN notation import/export support, also EPD support for testing
* Pluggable evaluator function, distinct functions provided: the Simplified Evaluator Function, other Complete and other Experimental
* Parameterizable evaluator (only for the complete &amp; experimental evaluators)
* Contempt factor
* UCI interface with lots of UCI options (for chess GUIs like Arena)
* The core of the chess engine was converted to C# using Sharpen

It scores 2522 ELO points at BT2630 tests in my Intel Core i7-3667U CPU @ 2.00GHz.
It also solves 290 positions of the 300 WinAtChess test (at 5 seconds each).
His real strength is about 2200 ELO points, you can check his tournament ranking at http://www.computerchess.org.uk/ccrl/

Authors
=======

* Alberto Alonso Ruibal: http://www.alonsoruibal.com
* Lukas Laag, developer of a great GWT SVG library (http://www.vectomatic.org) and the Carballo GWT interface

Building
========

Carballo uses the Gradle build system, you need to install Gradle from http://www.gradle.org

Build all the jars and install them to your local Maven repository:
```
gradle install
```
Build the UCI interface:
```
cd jse
gradle proguard
```
Build the GWT interface:
```
cd gwtgui
gradle compileGwt
```
Build the Applet interface:
```
cd applet
gradle proguard
```

History
=======

Version 0.9: Fixes evaluator bugs

* Reordered some logic in the ExperimentalEvaluator
* Fixed a bug in the detection of the 2 bishop bonus
* Fixed another bug in the detection of candidate passer pawns
* Some improvements in the HTML5 interface

Version 0.8: Build system and GWT modifications

* Project build system migrated from Maven to Gradle
* Solved some evaluator bugs
* New option to do not use Magic Bitboard Attacks: optimizes start time in HTML5
* Removed specific Bitboard attacks code from GWT, can be simulated setting BitboardAttacks.USE_MAGIC = false
* New ArrayBufferBook for GWT, it can process any opening book loaded as a JS ArrayBuffer

Version 0.7: A small leap on the engine performance and a big code clean

* Code moved to Github
* Integrated ROOT, PV and NULL nodes search routine
* Activated singular movement extensions and changed default singular extension margin
* Do null move only when the remaining depth is > 3 PLY
* No not overwrite the value in the TT if there is no room
* Converted code to C# using sharpen. At the moment only the core of the engine
* Solved a big bug getting the move from the transposition table
* Also found another bug on the search getting the last captured piece value
* And the complete evaluator had a bug calculating the attacks value

Version 0.6: Source code reorganization, GWT and PGN improvements, no changes on the engine code

* Code splitted in carballo-core, carballo-jse and carballo-applet
* Carballo-core is GWT-friendly
* Integrated SAN notation on Board class
* Improved PGN export with SAN notation
* Added a GWT interface based on the one by Lukas Laag (http://vectomatic.org) code

Version 0.5: Improves about 150 ELO points over Carballo 0.4

* PVS searcher: SearchEngine completely changed
* Futility pruning now works!
* New TT algorithm, now also uses TT to store evaluation values
* Bug with draw detection with 3-fold repetition
* Bug with time management on tournament, was using the opponent's time amount
* Bug with history table overflow

Version 0.4: First version integrated with Mobialia Chess

* Parametrizable evaluator
* Evaluator changes