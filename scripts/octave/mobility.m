##
## Octave file to generate and analyze the evaluator piece mobility bonuses
##

knightMaxSquares = 8; # Only forward mobility 4 / full mobility 8
knightMobSquares = 0 : knightMaxSquares;
knightMobOpening = round(24 * log10(0.1 + knightMobSquares * 9.9 / knightMaxSquares) / 2);
knightMobEndgame = round(32 * log10(0.1 + knightMobSquares * 9.9 / knightMaxSquares) / 2);
A = [knightMobOpening; knightMobEndgame];
knightValues = A(:);

bishopMaxSquares = 13; # Only forward mobility 7 / full mobility 13
bishopMobSquares = 0 : bishopMaxSquares; 
bishopMobOpening = round(32 * log10(0.1 + bishopMobSquares * 9.9 / bishopMaxSquares) / 2);
bishopMobEndgame = round(32 * log10(0.1 + bishopMobSquares * 9.9 / bishopMaxSquares) / 2);
A = [bishopMobOpening; bishopMobEndgame];
bishopValues = A(:);

rookMaxSquares = 14;
rookMobSquares = 0 : rookMaxSquares;
rookMobOpening = round(28 * log10(0.1 + rookMobSquares * 9.9 / rookMaxSquares) / 2);
rookMobEndgame = round(42 * log10(0.1 + rookMobSquares * 9.9 / rookMaxSquares) / 2);
A = [rookMobOpening; rookMobEndgame];
rookValues = A(:);

queenMaxSquares = 27;
queenMobSquares = 0 : queenMaxSquares;
queenMobOpening = round(54 * log10(0.1 + queenMobSquares * 9.9 / queenMaxSquares) / 2);
queenMobEndgame = round(54 * log10(0.1 + queenMobSquares * 9.9 / queenMaxSquares) / 2);
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