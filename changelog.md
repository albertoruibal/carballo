# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Upgrade the source and target compatibility level to Java 17
- Upgrade the Gradle wrapper to 9.6.1
- Upgrade GWT to 2.13.1 (`gwt-user` and `gwt-dev`)
- Upgrade the Gretty plugin to 5.0.2
- Upgrade ProGuard to 7.6.1 and target Java 17 in the obfuscation tasks
- Upgrade JUnit Jupiter to 5.13.4 and add the JUnit Platform launcher dependency
- Changed the artifact group to `com.carballo` and publish to GitHub Packages instead of JitPack
- Updated the Gradle build scripts to the Gradle 9 API (`base { archivesName }`, `assemble.dependsOn`)

### Removed
- Removed the `jitpack.yml` configuration file

## [1.8] - 2022-11-13

### Changed
- Change the source code level to Java 11
- Use JitPack for publishing the artifacts
- Changed artifact group to com.github.albertoruibal.carballo
- Use the gradle wrapper 7.5.1
- Update the tests to JUnit 5.9.1
- HTML5 app updated to GWT 2.10
- Converted the old Applet to a Swing application
- Implemented the standard "UCI_LimitStrength" and "UCI_Elo" UCI options instead of the old "Elo" option
- Implemented "searchmoves" and "go depth" in the UCI interface
- UCI Interface: Now "isready" may be called after "position"
- Add pawn blockade also to the CompleteEvaluator

### Fixed
- More small bug fixes

## [1.7] - 2019-09-21

### Changed
- New LMR formula with a progressive reduction based on the node eval diff and the move history
- Reduce the LMR tables size saving memory
- Do history pruning before LMR
- Prune moves with negative SSEs taking reductions into account
- Penalty for pawns in [D,E] in the initial square blocked by our own pieces
- Remove the pawn push extension
- A new PGN parser supporting variations, comments, NAGs, etc.

## [1.6]

### Changed
- Set the CompleteEvaluator as the new default evaluator
- Add history pruning
- Make the singular move margin depth dependent
- Don't do razoring in positions with known wins
- Add pawn push extension
- Fixes in the Opening-Endgame values to two-shorts-in-one-int logic that allows to separate the piece values from the square tables
- Add logic to detect draws in KQKQ, KRKR, KRPKR, KQKP, KBPKB, KBPKN and KRPPKP endgames
- Reduce the pawn value in the opening
- Add space evaluation
- Add rook trapped logic and improve bishop trapped evaluating pawn guard
- Change passer pawn logic adding a king distance bonus, remove unstoppable passer logic
- Improve the king shield and pawn storm logic
- Do not eval only forward mobility in bishops and knights
- Scale the contempt factor to 0 in the endgame

### Fixed
- Fix a bug returning from the excluded search for the singular move extension
- Add KBKB draw recognition (with same color bishops)
- Start to scale from midgame to endgame with 6 minor pieces (previously it was with 4 pieces)
- Fix some concurrence problems adding thread locks in the SearchEngine and SearchEngineThreaded class
- Fix seldepth reporting in the UCI interface (sometimes seldepth was lower than depth): if a TT move is returned, add the depth analyzed in the TT

## [1.5]

### Changed
- Prune non capture moves with SEE < 0 when depthRemaining < 3
- Change the outside passer detection, now only if the pawn is in the files a, b, c  or f, g, h and there are opposite pawns
- Increase the passer pawn bonuses to match the "Two connected passers on 6th are better than a rook"
- Increase the attack bonuses
- Revise the absolute/relative pin logic, adding the bonus one time by each pinned piece
- Increase the bishop trapped penalty
- Increase the pawn center bonuses in the opening

### Fixed
- Fix a bug in the pawnCanAttack bitboards

### Added
- Added GNU's Octave scripts to generate the mobility/attack/passer bonuses and the Piece-Square tables under scripts/octave
- Added a GCJ compilation script to generate native binaries for Linux

## [1.4]

### Changed
- Better understanding of pinned pieces, generating attacks only from legal moves
- New logarithmic piece mobility bonuses
- New set of pawn bonuses and changes in passer pawn evaluation adding an unstoppable passer bonus
- Remove evaluator section Config & UCI parameters speeding up things
- Changes in king safety evaluation taking into account three more squares in front of the king and modifying bonuses
- Improve pawn shield logic and add pawn storm evaluation
- Avoid negative values in the Opening-Endgame (O-E) arithmetic
- Make the bishop pair and the tempo bonuses O-E
- Reduce the tempo bonus in the endgame
- Merge piece values in the piece-square tables
- Simplify the rook on 5th, 6th, 7th rank logic replacing it by a bonus for each pawn attacked by the rook
- Remove the queen on 7th rank logic
- Use the PV value in the TT as the search starting score in each depth iteration
- Change time management to use more time
- New futility and razoring margins by depthRemaining, extend futility to more PLYs
- In quiescence search (QS), do futility also for PV nodes and generate checks at depth 0 also for non-PV nodes
- As fractional extensions are no longer used, now PLY is 1
- Use unicode figurines in the text board and in the GWT Gui SAN notation

### Fixed
- Fix engine crash analyzing positions already mate

## [1.3]

### Changed
- Now AttacksInfo holds the attacks information by piece type
- This allows to improve mobility and king safety evaluation detecting squares attacked by less valuable pieces
- Removed the king defense bonuses from the Experimental evaluator
- New MOBILITY array holding mobility bonuses by piece type and number of destiny squares
- Simplified the Bishop's Capablanca rule
- Removed some rook in 7th rank logic
- Improved the pawnCanAttack squares detection removing squares that cannot be reached due to opposite pawns
- Modify the "rook attacks backward pawn logic" to detect real backward pawns
- Evaluation refactoring using the "W" and "B" constants and the "us" and "them" variables
- The Attacks evaluation is now done in a separated evalAttacks() method, unifying the attack bonuses in the PAWN_ATTACKS, MINOR_ATTACKS and MAJOR_ATTACKS arrays
- Piece value constants moved to the Config class and removed the PIECE_VALUES array
- Better midgame-to-endgame evaluation scaling with the new NON_PAWN_MATERIAL_MIDGAME_MAX and NON_PAWN_MATERIAL_ENDGAME_MIN constants
- Remove some UCI parameters and change them by constants for a better running optimization

### Fixed
- Fixed the unsupported pawn penalty

## [1.2]

### Added
- Added support for Chess960 (Fischer Random Chess)
- Implemented UCI Ponder
- Four killer move slots

### Changed
- New MoveIterator that generates only legal moves with a check flag set
- New replace strategy for the TT, taking into account the entry depth and the generation difference
- In the TT in QS, store the entries with checks generated with depth 1 and entries without checks with depth 0
- Set default razoring margin to 400
- Remove the null move margin and improve the null move reduction calculation
- Remove the recapture extension
- New time management strategy adding a "panic time" when the search in the root node fails low by a margin of 100
- Do not penalize pinned pawns in the evaluators, multiply the hung pieces bonus by the number of hung pieces, and pawn center opening corrections
- Generate piece-square values in different classes
- Now it uses 1" for the WinAtChess tests

### Fixed
- Fixed a bug multiplying the opening/ending values in the evaluators by negative factors
- Fixed an important bug in the SWAP algorithm for the SSE evaluation

## [1.1]

### Changed
- Do not allocate memory inside ExperimentalEvaluator.evaluate()
- Added the new pawn and endgame logic to the CompleteEvaluator (ExperimentalEvaluator continues as the default)

### Fixed
- The UCI interface was ignoring all the UCI options

## [1.0]

### Added
- New Transposition Table with a separated slot for the eval values
- Implemented UCI seldepth, lowerbound, upperbound and hashfull
- Implemented the depth and node limit for the search
- Enabled the endgame knowledge
- Tests migrated to the JUnit 4 format with annotations, and created a "fastTest" gradle task to run only the fast tests

### Changed
- Now uses the TT in quiescence search
- In the search, assume that pawn pushes are to the 6th, 7th or 8th rank
- Extend only checks with positive SSE
- Disable by default the recapture and pawn extensions, set the mate threat extension to one full PLY
- C# code separated in another GitHub project

### Fixed
- Fix mate values before inserting them in the TT, now the mate problems are solved with the right distance to the mate
- Fixes to futility pruning in quiescence search
- Fix recapture extension (now disabled by default)

## [0.9]

### Fixed
- Reordered some logic in the ExperimentalEvaluator
- Fixed a bug in the detection of the 2 bishop bonus
- Fixed another bug in the detection of candidate passer pawns
- Bug in the detection of the adjacent columns found by Yonathan Maalo

### Changed
- Some improvements in the HTML5 interface

## [0.8]

### Changed
- Project build system migrated from Maven to Gradle
- Solved some evaluator bugs
- New option to do not use Magic Bitboard Attacks: optimizes start time in HTML5
- Removed specific Bitboard attacks code from GWT, can be simulated setting BitboardAttacks.USE_MAGIC = false

### Added
- New ArrayBufferBook for GWT, it can process any opening book loaded as a JS ArrayBuffer

## [0.7]

### Changed
- Code moved to Github
- Integrated ROOT, PV and NULL nodes search routine
- Activated singular movement extensions and changed default singular extension margin
- Do null move only when the remaining depth is > 3 PLY
- No not overwrite the value in the TT if there is no room
- Converted code to C# using sharpen. At the moment only the core of the engine

### Fixed
- Solved a big bug getting the move from the transposition table
- Also found another bug on the search getting the last captured piece value
- And the complete evaluator had a bug calculating the attacks value

## [0.6]

### Changed
- Code splitted in carballo-core, carballo-jse and carballo-applet
- Carballo-core is GWT-friendly
- Integrated SAN notation on Board class
- Improved PGN export with SAN notation

### Added
- Added a GWT interface based on the one by Lukas Laag (http://vectomatic.org) code

## [0.5]

### Changed
- PVS searcher: SearchEngine completely changed
- Futility pruning now works!
- New TT algorithm, now also uses TT to store evaluation values

### Fixed
- Bug with draw detection with 3-fold repetition
- Bug with time management on tournament, was using the opponent's time amount
- Bug with history table overflow

## [0.4]

### Added
- Parameterizable evaluator

### Changed
- Evaluator changes