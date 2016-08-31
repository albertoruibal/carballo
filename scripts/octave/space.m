##
## Octave file to generate and analyze the Space values
##

spaceMaxSquares = 4 * 3;
spaceSquares = 0 : spaceMaxSquares;
spaceEval = round(30 * log10(1 + spaceSquares * 9 / spaceMaxSquares));

plot(spaceSquares, spaceEval);
legend("Space Squares");

# Print values for the Java evaluator
printf(strcat(
    "private static final int[] SPACE = {",
    substr(sprintf('oe(%i, 0), ', spaceEval), 1, -2),
    "};\n"
    ));