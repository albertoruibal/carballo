##
## Octave file to generate and analyze the Piece-Square-Table values
##

# PAWN
PawnColumn = [
    [-20, -8, -2, 5],
    [-4, -6, -8, -10]
];
PawnRank = [
    [0, -3, -2, -1, 1, 2, 3, 0],
    [0, -3, -3, -2, -1, 0, 2, 0]
];
PawnDiagonal = [
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0]
];
PawnOpeningCorrection = [
    0, 0, 0, 0, 0, 0, 0, 0;
    0, 0, 0, 0, 0, 0, 0, 0;
    0, 0, 0, 10, 10, 0, 0, 0;
    0, 0, 0, 20, 20, 0, 0, 0;
    0, 0, 0, 10, 10, 0, 0, 0;
    0, 0, 0, 0, 0, 0, 0, 0;
    0, 0, 0, 0, 0, 0, 0, 0;
    0, 0, 0, 0, 0, 0, 0, 0;
];
# KNIGHT
KnightColumn = [
    [-26, -10, 1, 5],
    [-4, -1, 2, 4]
];
KnightRank = [
    [-32, -10, 6, 15, 21, 19, 10, -11],
    [-10, -5, -2, 1, 3, 5, 2, -3]
];
KnightDiagonal = [
    [0, 0, 0, 0, 0, 0, 0, 0],
    [2, 1, 0, -1, -2, -4, -7, -10]
];
# BISHOP
BishopColumn = [
    [0, 0, 0, 0],
    [0, 0, 0, 0]
];
BishopRank = [
    [-5, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0]
];
BishopDiagonal = [
    [10, 5, 1, -3, -5, -7, -8, -12],
    [3, 2, 0, 0, -2, -2, -3, -3]
];
# ROOK
RookColumn = [
    [-4, 0, 4, 8],
    [0, 0, 0, 0]
];
RookRank = [
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 1, 1, 1, -2]
];
RookDiagonal = [
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0]
];
# QUEEN
QueenColumn = [
    [-2, 0, 1, 2],
    [-2, 0, 1, 2]
];
QueenRank = [
    [-2, 0, 1, 2, 2, 1, 0, -2],
    [-2, 0, 1, 2, 2, 1, 0, -2]
];
QueenDiagonal = [
    [3, 2, 1, 0, -2, -4, -7, -10],
    [1, 0, -1, -3, -4, -6, -8, -12]
];
# KING
KingColumn = [
    [40, 45, 15, -5],
    [-15, 0, 10, 15]
];
KingRank = [
    [4, 1, -2, -5, -10, -15, -25, -35],
    [-15, 0, 10, 15, 15, 10, 0, -15]
];
KingDiagonal = [
    [0, 0, 0, 0, 0, 0, 0, 0],
    [2, 0, -2, -5, -8, -12, -20, -30]
];

function A = myDiagonal(D)
    D1 = [
        D(8), D(7), D(6), D(5);
        D(7), D(6), D(5), D(4);
        D(6), D(5), D(4), D(3);
        D(5), D(4), D(3), D(2)
    ];
    D2 = [
        D(1), D(2), D(3), D(4);
        D(2), D(1), D(2), D(3);
        D(3), D(2), D(1), D(2);
        D(4), D(3), D(2), D(1)
    ];
    A = [ (D1 + D2) fliplr(D1 + D2); flip(D1 + D2) flip(fliplr(D1 + D2))];
endfunction

function out = generatePCSQ(pieceValue,Column4, Rank, Diagonal, OpeningCorrection, plotOffset)
    O = OpeningCorrection + pieceValue(1) + repmat([Column4(1, :) flip(Column4(1, :))], 8, 1) + repmat(Rank(1, :)', 1, 8) + myDiagonal(Diagonal(1, :));
    E = pieceValue(2) + repmat([Column4(2, :) flip(Column4(2, :))], 8, 1) + repmat(Rank(2, :)', 1, 8) + myDiagonal(Diagonal(2, :));
    subplot(6, 2, plotOffset);
    surf(O);
    legend("Opening");
    subplot(6, 2, plotOffset + 1);
    surf(E);
    legend("EndGame");
        
    OE = [O(1,:) O(2,:) O(3, :) O(4,:) O(5,:) O(6,:) O(7,:) O(8,:) ; E(1,:) E(2,:) E(3,:) E(4,:) E(5,:) E(6,:) E(7,:) E(8,:)](:);
        
    out = strcat(" {\n",
    substr(sprintf('oe(%i, %i), oe(%i, %i), oe(%i, %i), oe(%i, %i), oe(%i, %i), oe(%i, %i), oe(%i, %i), oe(%i, %i),\n', OE), 1, -2), 
    "\n};\n");
endfunction

printf(strcat(
    "private static final int pawnPcsq[] =",
    generatePCSQ([100, 100], PawnColumn, PawnRank, PawnDiagonal, PawnOpeningCorrection, 1),
    "private static final int knightPcsq[] =",
    generatePCSQ([325, 325], KnightColumn, KnightRank, KnightDiagonal, 0, 3),
    "private static final int bishopPcsq[] =",
    generatePCSQ([325, 325], BishopColumn, BishopRank, BishopDiagonal, 0, 5),
    "private static final int rookPcsq[] =",
    generatePCSQ([500, 500], RookColumn, RookRank, RookDiagonal, 0, 7),
    "private static final int queenPcsq[] =",
    generatePCSQ([975, 975], QueenColumn, QueenRank, QueenDiagonal, 0, 9),
    "private static final int kingPcsq[] =",
    generatePCSQ([1000, 1000], KingColumn, KingRank, KingDiagonal, 0, 11)
    ));
