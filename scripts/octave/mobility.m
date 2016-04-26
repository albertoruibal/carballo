##
## Octave file to generate and analyze the evaluator piece mobility bonuses
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