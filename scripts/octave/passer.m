##
## Octave file to generate the passer pawn bonuses
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