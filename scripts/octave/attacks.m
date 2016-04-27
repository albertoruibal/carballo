##
## Octave file to generate and analyze the attack bonuses
##

# Pawn, Minor, Major
pieceTypeBonus = [25, 15, 7];
AttackFactors = [0.3, 0.6, 0.63, 0.9, 1];

pawnAttackBase =  pieceTypeBonus(1) * attackFactors;
pawnAttackBase(1) = 0;
pawnAttackOpening = round(pawnAttackBase * 3 / 4);
pawnAttackEndgame = round(pawnAttackBase);
pawnAttacks = [pawnAttackOpening; pawnAttackEndgame](:);

minorAttackBase = pieceTypeBonus(2) * attackFactors;
minorAttackOpening = round(minorAttackBase * 3 / 4);
minorAttackEndgame = round(minorAttackBase);
minorAttacks = [minorAttackOpening; minorAttackEndgame](:);

majorAttackBase = pieceTypeBonus(3) * attackFactors;
majorAttackOpening = round(majorAttackBase * 3 / 4);
majorAttackEndgame = round(majorAttackBase);
majorAttacks = [majorAttackOpening; majorAttackEndgame](:);

# Create a graph with attack values
pieces = [1, 2, 3, 4, 5];
plot(pieces, pawnAttackOpening, pieces, pawnAttackEndgame, pieces, minorAttackOpening, pieces, minorAttackEndgame, pieces, majorAttackOpening, pieces, majorAttackEndgame);
legend("Pawn Opening", "Pawn Endgame", "Minor Opening", "Minor Endgame", "Major Opening", "Major Endgame");

# Print values for the Java evaluator
printf(strrep(strcat(
    "private static final int[] PAWN_ATTACKS = {0, ", substr(sprintf('oe(%i, %i), ', pawnAttacks), 1, -2), ", 0};\n",
    "private static final int[] MINOR_ATTACKS = {0, ", substr(sprintf('oe(%i, %i), ', minorAttacks), 1, -2), ", 0}; // Minor piece attacks to pawn undefended pieces\n",
    "private static final int[] MAJOR_ATTACKS = {0, ", substr(sprintf('oe(%i, %i), ', majorAttacks), 1, -2), ", 0}; // Major piece attacks to pawn undefended pieces\n"
    ), "oe(0, 0)", "0"));