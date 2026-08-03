# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
- Search: declare `searching`, `stop`, `thinkToTime`, `thinkToNodes` and `thinkToDepth` as
  `volatile` so the search thread reliably observes updates from the UCI/caller thread
  (prevents lost visibility of `stop`, torn 64-bit reads on `thinkToTime`, and infinite
  spins in `SearchEngineThreaded.stop()`).
- Search: fix null-move verification logic, which was inverted by the `||` short-circuit
  and skipped verification on deep nodes (where it is most useful) instead applying it
  on shallow nodes.
- Search: use the re-probed (non-exclusion) TT entry for `refineEval` when running an
  excluded search for singular extensions; previously the exclusion entry's score was
  incorrectly used to refine the static evaluation.
- Search: run the singular-extension excluded search as a `NODE_NULL` null-window
  search instead of inheriting the parent node type, avoiding spurious IID and singular
  recursion in PV nodes.
- Threaded search: `SearchEngineThreaded.stop()` no longer holds `startStopSearchLock`
  while sleeping, and now `join()`s the worker thread so the `bestmove` UCI reply is
  guaranteed to be emitted before `stop()` returns. The search thread is now named and
  is a daemon so it never blocks JVM shutdown.
- Evaluation: fix integer overflow in the tapered midgame/endgame blend of
  `CompleteEvaluator` and `ExperimentalEvaluator`. With an endgame eval of a few
  thousand centipawns and the default scale factor, the intermediate
  `(1000 - gamePhase) * e(oe) * scaleFactor` exceeded `Integer.MAX_VALUE` before the
  final division, silently corrupting the returned value in routine endgame positions.
  The blend now uses `long` arithmetic.
- Evaluation: fix integer overflow in the king-safety term. `kingSafety[us]` is an
  OE-encoded int whose opening component can reach ~80,000,000 for a strong attack;
  multiplying it by `KING_SAFETY_PONDER[count]` (up to 64) overflowed a 32-bit int
  before `oeShr` could extract the parts. The king-safety term is now computed in
  opening units (its endgame component is always 0).
- Evaluation: recognize KBBK (black with two bishops) as a winning KXK endgame. The
  black-dominant branch of the KXK dispatch tested `whiteBishops >= 2` (impossible when
  white has no material) instead of `blackBishops >= 2`, so KBBK fell through to the
  general evaluator without the corner-drive / king-proximity heuristic.
- Evaluation: remove the double-counting of pinned pieces in `ExperimentalEvaluator`.
  `AttacksInfo.pinnedPieces` already detects every absolute pin (bishop- and rook-type),
  and `evalAttacks` scores them, so the per-piece x-ray scan in the bishop and rook loops
  was scoring the same pinned rooks/queens a second time.
- Evaluation: make the `closerSquares` king-proximity table monotonic. The bonus for an
  adjacent enemy king (distance 1) was 0 while distance 2 was 100, an obvious
  non-monotonic hole that scored the strongest opposition configurations as worthless.
- Board: do not increment the 50-move counter on a null move. A null move is not a real
  chess move; bumping `fiftyMovesRule` for it could turn a genuine 99-halfmove position
  into a false 100-halfmove draw during null-move search. `moveNumber` still advances so
  the save/undo history slots stay aligned.
- Board: emit Shredder/XFEN castling-rights file letters (e.g. `HBhb`) from `getFen()`
  when `chess960` is enabled, instead of always emitting `KQkq`. The previous output was
  not spec-compliant and ambiguous for Chess960 positions with two rooks on the same
  side of the king.
- Board: fix the SEE (Static Exchange Evaluation) SWAP loop so a non-capturing move's
  piece is not erroneously added to the attacker set: use `attacks &= ~fromSquare`
  instead of `^=`, which could let the mover be re-selected and corrupt the score.
- Move: change the castling-type detection in `Move.getFromString` from two independent
  `if`s to `if`/`else if`, so a kingside match cannot be overwritten by a queenside match
  in unusual Chess960 setups where both conditions could fire for the same target.
- Move generator: fix `LegalMoveGenerator.generateMoves` to iterate the freshly
  generated pseudo-legal moves `[index, lastIndex)` instead of starting from 0. The
  previous loop re-ran `doMove` on stale slots below `index` and never processed the new
  moves correctly when called with a non-zero `index`.
- AttacksInfo: guard `kingIndex` against a missing king. `Long.numberOfTrailingZeros(0)`
  returns 64, which would index out of bounds in `getBishopAttacks`/`getRookAttacks`;
  normal positions always have both kings, but constructed/editor positions can be
  kingless and previously crashed `AttacksInfo.build`.
- Transposition table: use modular indexing in the probe and store loops so a key near
  the end of the table still gets its full `MAX_PROBES` slots. The previous loop was
  truncated when `startIndex + MAX_PROBES > size`, biasing replacement and losing entries
  near the table tail.
- Transposition table: clamp the mate-adjusted score to `±MATE` before storing. With a
  large `distanceToInitialPly` the adjustment could push a MATE score past `±MATE`,
  breaking the 16-bit round-trip and producing a negative "mate in N" in UCI output.
- Transposition table: `clear()` now zeroes `infos` and `evals` in addition to `keys`,
  so stale eval/score data cannot be read back through a slot whose key happens to be 0.
- Transposition table: validate the requested size and round it up to a power of two
  instead of silently producing a too-small (or zero-length) table for non-power-of-two
  or zero `sizeMb` values.
- UCI: parse `setoption name <name with spaces> value ...` correctly by joining the
  name tokens with spaces. The previous code concatenated them without separators, so
  `Contempt Factor` became `ContemptFactor` and only matched the switch case by accident;
  the switch now matches the advertised `"Contempt Factor"`.
- UCI: bounds-check every `tokens[index++]` access (e.g. `wtime`, `btime`, `movestogo`,
  `depth`, `nodes`, `movetime`, `mate`) and parse numbers defensively so a malformed
  `go` or `setoption` command no longer crashes the UCI loop with
  `ArrayIndexOutOfBoundsException`/`NumberFormatException`. The whole command body is
  wrapped in a `RuntimeException` catch that reports the error via `info string` instead
  of killing the engine.
- UCI: null-guard `engine` in `stop` and `ucinewgame` so sending them before `isready`
  no longer throws a `NullPointerException`.
- UCI: handle `position moves ...` (without `startpos`/`fen`) as `startpos`, trim the
  trailing space from `position fen` FEN strings, and validate each move in
  `position ... moves`: an illegal/unparseable move now reports `info string illegal
  move` and stops processing instead of silently desynchronizing the board.
- UCI: terminate the loop on EOF (`readLine() == null`) so the engine exits cleanly
  when the GUI closes the input stream.
- UCI: guard a malformed `bestmove` line with no move token in `UciEngine` so it no
  longer throws `ArrayIndexOutOfBoundsException`.
- UCI (`UciEngine`): make `uciOk`/`readyOk`/`bestMove`/`died` volatile so the waiting
  thread reliably sees updates from the reader thread; add a `died` flag set when the
  reader exits so `waitUciOk`/`waitReadyOk`/`waitBestMove` no longer hang forever if the
  subprocess dies; bound those waits with timeouts; and `close()` now closes the
  scanner, writer and process streams (previously leaked file descriptors across
  repeated open/close cycles). The reader thread is now named and is a daemon.
- Search parameters: when `ponderhit` arrives without any time data
  (`timeAvailable <= 0` and no increment), keep the infinite deadline instead of
  returning `startTime + 0`, which aborted the search immediately and returned a
  barely-searched move.
- Opening book (`FileBook`): close the book stream after each lookup. The previous
  code never closed the `DataInputStream`, leaking a file handle on every book move
  until GC finalized it. Now uses try-with-resources.
- Opening book: detect a missing book resource and return no moves instead of throwing
  a `NullPointerException` that was swallowed by the catch block, making the failure
  invisible.
- Opening book: tolerate `DataInputStream.skipBytes` returning fewer bytes than requested
  (per its contract) by retrying/falling back to a single-byte read, so a short skip
  can no longer desynchronize the scan and match wrong keys.
- Opening book: use `double` precision for the weighted random selection. The previous
  `Float.valueOf(random.nextFloat() * totalWeight).longValue()` lost precision for
  `totalWeight` above 2^24 (float mantissa width), systematically under-selecting
  small-weight moves.
- Opening book: return `Move.NONE` instead of arbitrarily returning `moves.get(0)` when
  the book has no legal moves or all weights are zero.
- PGN parser: guard header parsing against lines without quotes (e.g. `[Event]` or
  `[Event foo]`). The previous `line.substring(1, line.indexOf("\""))` threw
  `StringIndexOutOfBoundsException` when `indexOf` returned -1, and the outer catch
  silently dropped the whole game.
- PGN parser: parse castling written as `0-0`/`0-0-0` (the PGN spec allows both `O-O`
  and `0-0`) by including `'0'` in `isAlphaNumeric`; previously `0-0` was routed to the
  glyph branch and never added as a move.
- PGN parser: parse Elo/FIDE-id headers defensively, returning null on a non-numeric
  value (e.g. `"?"`, `""`, or a malformed token) instead of throwing
  `NumberFormatException` and dropping the whole game.
- PGN parser: do not treat `"="` as a result (it is a NAG shorthand for "equal
  chances", not a game result); only `1/2-1/2`/`½-½` normalize to `½-½`.
- PGN (`PgnImportExport.setBoard`): validate each move before applying it; an
  unparseable or illegal move now stops the replay instead of leaving the board
  unadvanced and desynchronizing every subsequent move.
- PGN (`Game` setters): only clear event/site/round/date/eventDate when the whole value
  is the PGN "unknown" marker, not via substring `replace("?", "")` which mangled
  legitimate data containing `?` (e.g. `"Who? Open"` or the date `"2020.??.??"` that
  became `"2020."` with a dangling dot).
- Logger: route `error()` to `System.err` even in UCI mode (`noLog = true`) so real
  errors remain visible for debugging without corrupting the UCI stdout protocol
  stream. Also make `noLog` volatile so the UCI thread's update is visible across
  threads, and null-guard the message in all log methods.
- Config: add the correctly-spelled `DEFAULT_BOOK_KNOWLEDGE` constant (the old
  `DEFAULT_BOOK_KNOWGLEDGE` typo is kept `@Deprecated` for source compatibility).

## [1.9] - 2026-07-27

### Changed
- Upgrade the source and target compatibility level to Java 17
- Upgrade the Gradle wrapper to 9.6.1
- Upgrade GWT to 2.13.1 (`gwt-user` and `gwt-dev`)
- Upgrade the Gretty plugin to 5.0.2
- Upgrade ProGuard to 7.9.1 and target Java 17 in the obfuscation tasks
- Upgrade JUnit Jupiter to 5.13.4 and add the JUnit Platform launcher dependency
- Changed the artifact group to `com.carballo` and publish to GitHub Packages instead of JitPack
- Updated the Gradle build scripts to the Gradle 9 API (`base { archivesName }`, `assemble.dependsOn`)
- Make `ChessApp` a `JPanel` inside a `JFrame` instead of an AWT `Panel`
  inside a `Frame`, so the Swing board is double-buffered and no longer
  flickers on computer moves

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