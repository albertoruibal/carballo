##
## Octave file to generate and analyze the evaluator piece mobility bonuses
##

knightMaxSquares = 4; # Only forward mobility 4 / full mobility 8
knightMobSquares = 0 : knightMaxSquares;
knightMobOpening = round(24 * log10(1 + knightMobSquares * 9 / knightMaxSquares));
knightMobEndgame = round(32 * log10(1 + knightMobSquares * 9 / knightMaxSquares));
A = [knightMobOpening; knightMobEndgame];
knightValues = A(:);

bishopMaxSquares = 7; # Only forward mobility 7 / full mobility 13
bishopMobSquares = 0 : bishopMaxSquares; 
bishopMobOpening = round(32 * log10(1 + bishopMobSquares * 9 / bishopMaxSquares));
bishopMobEndgame = round(32 * log10(1 + bishopMobSquares * 9 / bishopMaxSquares));
A = [bishopMobOpening; bishopMobEndgame];
bishopValues = A(:);

rookMaxSquares = 14;
rookMobSquares = 0 : rookMaxSquares;
rookMobOpening = round(28 * log10(1 + rookMobSquares * 9 / rookMaxSquares));
rookMobEndgame = round(42 * log10(1 + rookMobSquares * 9 / rookMaxSquares));
A = [rookMobOpening; rookMobEndgame];
rookValues = A(:);

queenMaxSquares = 27;
queenMobSquares = 0 : queenMaxSquares;
queenMobOpening = round(54 * log10(1 + queenMobSquares * 9 / queenMaxSquares));
queenMobEndgame = round(54 * log10(1 + queenMobSquares * 9 / queenMaxSquares));
A = [queenMobOpening; queenMobEndgame];
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