###
### Octave file to generate and analyze some evaluator values
###

##
## Piece mobility values
##

knightMobSquares=0:4; # only forward mobility
knightMobOpening=round(24 * log10(1 + knightMobSquares * 9 / 4));
knightMobEndgame=round(32 * log10(1 + knightMobSquares * 9 / 4));
A = [knightMobOpening;knightMobEndgame];
knightValues = A(:); 

bishopMobSquares=0:7; # only forward mobility
bishopMobOpening=round(32 * log10(1 + bishopMobSquares * 9 / 7));
bishopMobEndgame=round(32 * log10(1 + bishopMobSquares * 9 / 7));
A = [bishopMobOpening;bishopMobEndgame];
bishopValues = A(:); 

rookMobSquares=0:14;
rookMobOpening=round(28 * log10(1 + rookMobSquares * 9 / 14));
rookMobEndgame=round(42 * log10(1 + rookMobSquares * 9 / 14));
A = [rookMobOpening;rookMobEndgame];
rookValues = A(:); 

queenMobSquares=0:27;
queenMobOpening=round(54 * log10(1 + queenMobSquares * 9 / 27));
queenMobEndgame=round(54 * log10(1 + queenMobSquares * 9 / 27));
A = [queenMobOpening;queenMobEndgame];
queenValues = A(:); 

# Create a graph with mobility values
plot(knightMobSquares, knightMobOpening, knightMobSquares, knightMobEndgame, bishopMobSquares, bishopMobOpening, rookMobSquares, rookMobOpening, rookMobSquares, rookMobEndgame, queenMobSquares, queenMobOpening, queenMobSquares, queenMobEndgame);
legend("Knight Opening", "Knight Endgame", "Bishop", "Rook Opening", "Rook EndGame", "Queen Opening", "Queen Endgame");

# Print values for the Java evaluator
printf(strcat(
  "private static final int[][] MOBILITY = {\n",
	"    {}, {},\n",
  "    {", substr(sprintf('oe(%i, %i), ', knightValues), 1, -2), "},\n",
  "    {", substr(sprintf('oe(%i, %i), ', bishopValues), 1, -2), "},\n",
  "    {", substr(sprintf('oe(%i, %i), ', rookValues), 1, -2), "},\n",
  "    {", substr(sprintf('oe(%i, %i), ', queenValues), 1, -2), "}\n",
  "};\n"
  ));

##
## Passer pawns bonuses
##
ranks = 0:7;

possibleSquares = [0, 1, 1, 1, 1, 1, 1, 0];
possibleSquares066 = possibleSquares * 2 / 3;
possibleSquaresAux = [possibleSquares066 ; possibleSquares];
possibleSquaresOE = possibleSquaresAux(:);

bonuses=[0, 0, 0, 0.1, 0.3, 0.6, 1, 0];
bonuses066 = bonuses * 2 / 3;
bonusesAux = [bonuses066 ; bonuses];
bonusesOE = bonusesAux(:);

passerValues = 25 * possibleSquaresOE + 155 * bonusesOE;
candidateValues = round(passerValues * 0.5);
outsideValues = round(passerValues * 0.2);

connectedValues = round(bonusesOE * 70);
supportedValues = round(bonusesOE * 80);
mobileValues = round(bonusesOE * 45);
runnerValues = round(bonusesOE * 60);

passerValues = round(passerValues);

# Print values for the Java evaluator
printf(strrep(strcat(
  "private static final int[] PAWN_CANDIDATE = {",
  substr(sprintf('oe(%i, %i), ', candidateValues), 1, -2),
  "};\n",
  "private static final int[] PAWN_PASSER = {",
  substr(sprintf('oe(%i, %i), ', passerValues), 1, -2),
  "};\n",
  "private static final int[] PAWN_PASSER_OUTSIDE = {",
  substr(sprintf('oe(%i, %i), ', outsideValues), 1, -2),
  "};\n",
  "private static final int[] PAWN_PASSER_CONNECTED = {",
  substr(sprintf('oe(%i, %i), ', connectedValues), 1, -2),
  "};\n",
  "private static final int[] PAWN_PASSER_SUPPORTED = {",
  substr(sprintf('oe(%i, %i), ', supportedValues), 1, -2),
  "};\n",
  "private static final int[] PAWN_PASSER_MOBILE = {",
  substr(sprintf('oe(%i, %i), ', mobileValues), 1, -2),
  "};\n",
  "private static final int[] PAWN_PASSER_RUNNER = {",
  substr(sprintf('oe(%i, %i), ', runnerValues), 1, -2),
  "};\n",
  ""
), "oe(0, 0)", "0"));