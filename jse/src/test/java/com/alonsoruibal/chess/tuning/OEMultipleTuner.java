package com.alonsoruibal.chess.tuning;

import com.alonsoruibal.chess.evaluation.TunedEvaluator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public class OEMultipleTuner extends Tuner {

	String[] FIELD_NAMES;
	int[] parameters;

	int[] getParameters(TunedEvaluator evaluator) {
		System.out.println("************************************************************************************");
		System.out.println("FIELD_NAMES = " + Arrays.toString(FIELD_NAMES));

		try {
			ArrayList<Integer> parameterList = new ArrayList<>();
			for (String fieldName : FIELD_NAMES) {
				Field field = TunedEvaluator.class.getField(fieldName);
				String typeName = field.getType().getSimpleName();
				if ("int".equals(typeName)) {
					int fieldValue = (int) field.get(evaluator);
					parameterList.add(TunedEvaluator.o(fieldValue));
					parameterList.add(TunedEvaluator.e(fieldValue));
				} else if ("int[]".equals(typeName)) {
					int[] fieldValue = (int[]) field.get(evaluator);
					for (int k : fieldValue) {
						parameterList.add(TunedEvaluator.o(k));
						parameterList.add(TunedEvaluator.e(k));
					}
				}
			}

			parameters = new int[parameterList.size()];
			for (int i = 0; i < parameterList.size(); i++) {
				parameters[i] = parameterList.get(i);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return parameters;
	}

	void paramsToEval(TunedEvaluator evaluator) {
		try {
			int parameterIndex = 0;
			for (String fieldName : FIELD_NAMES) {
				Field field = TunedEvaluator.class.getField(fieldName);
				String typeName = field.getType().getSimpleName();
				if ("int".equals(typeName)) {
					field.set(evaluator, TunedEvaluator.oe(parameters[parameterIndex++], parameters[parameterIndex++]));
				} else if ("int[]".equals(typeName)) {
					int[] fieldValue = (int[]) field.get(evaluator);
					for (int i = 0; i < fieldValue.length; i++) {
						fieldValue[i] = TunedEvaluator.oe(parameters[parameterIndex++], parameters[parameterIndex++]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	@Tag("slow")
	void tunePawns() {
		FIELD_NAMES = new String[]{
				"PAWN_BACKWARDS_PENALTY", "PAWN_ISOLATED_PENALTY", "PAWN_DOUBLED_PENALTY", "PAWN_UNSUPPORTED_PENALTY",
				"PAWN_CANDIDATE", "PAWN_PASSER", "PAWN_PASSER_OUTSIDE", "PAWN_PASSER_CONNECTED", "PAWN_PASSER_SUPPORTED",
				"PAWN_PASSER_MOBILE", "PAWN_PASSER_RUNNER",
				"PAWN_PASSER_OTHER_KING_DISTANCE", "PAWN_PASSER_MY_KING_DISTANCE",
				"PAWN_SHIELD_CENTER", "PAWN_SHIELD",
				"PAWN_STORM_CENTER", "PAWN_STORM",
				"PAWN_PCSQ"
		};
		doTuning(this::printParameters);
	}

	@Test
	@Tag("slow")
	void tuneMinor() {
		FIELD_NAMES = new String[]{
				"KNIGHT_OUTPOST", "KNIGHT_MOBILITY", "KNIGHT_PCSQ",
				"BISHOP_OUTPOST", "BISHOP_MY_PAWNS_IN_COLOR_PENALTY",
				"BISHOP_TRAPPED_BY_OPPOSITE_PAWN_PENALTY", "BISHOP_MOBILITY", "BISHOP_PCSQ",
		};
		doTuning(this::printParameters);
	}

	@Test
	@Tag("slow")
	void tuneMajor() {
		FIELD_NAMES = new String[]{
				"ROOK_OUTPOST", "ROOK_FILE",
				"ROOK_7", "ROOK_TRAPPED_BY_OWN_KING_PENALTY", "ROOK_MOBILITY", "ROOK_PCSQ",
				"QUEEN_MOBILITY", "QUEEN_PCSQ", "KING_PCSQ"
		};
		doTuning(this::printParameters);
	}

	@Test
	@Tag("slow")
	void tuneAttacks() {
		FIELD_NAMES = new String[]{
				"PAWN_ATTACKS", "MINOR_ATTACKS", "MAJOR_ATTACKS",
				"HUNG_PIECES", "PINNED_PIECE",
				"PIECE_ATTACKS_KING"
		};
		doTuning(this::printParameters);
	}

	@Test
	@Tag("slow")
	void tuneOther() {
		FIELD_NAMES = new String[]{
				"SPACE", "TEMPO"
		};
		doTuning(this::printParameters);
	}

	@Test
	@Tag("slow")
	void tuneAllParameters() {
		FIELD_NAMES = new String[]{
				"PAWN_BACKWARDS_PENALTY", "PAWN_ISOLATED_PENALTY", "PAWN_DOUBLED_PENALTY", "PAWN_UNSUPPORTED_PENALTY",
				"PAWN_CANDIDATE", "PAWN_PASSER", "PAWN_PASSER_OUTSIDE", "PAWN_PASSER_CONNECTED", "PAWN_PASSER_SUPPORTED",
				"PAWN_PASSER_MOBILE", "PAWN_PASSER_RUNNER",
				"PAWN_PASSER_OTHER_KING_DISTANCE", "PAWN_PASSER_MY_KING_DISTANCE",
				"PAWN_SHIELD_CENTER", "PAWN_SHIELD",
				"PAWN_STORM_CENTER", "PAWN_STORM",
				"PAWN_PCSQ",

				"KNIGHT_OUTPOST", "KNIGHT_MOBILITY", "KNIGHT_PCSQ",
				"BISHOP_OUTPOST", "BISHOP_MY_PAWNS_IN_COLOR_PENALTY",
				"BISHOP_TRAPPED_BY_OPPOSITE_PAWN_PENALTY", "BISHOP_MOBILITY", "BISHOP_PCSQ",

				"ROOK_OUTPOST", "ROOK_FILE",
				"ROOK_7", "ROOK_TRAPPED_BY_OWN_KING_PENALTY", "ROOK_MOBILITY", "ROOK_PCSQ",
				"QUEEN_MOBILITY", "QUEEN_PCSQ", "KING_PCSQ",

				"PAWN_ATTACKS", "MINOR_ATTACKS", "MAJOR_ATTACKS",
				"HUNG_PIECES", "PINNED_PIECE",
				"PIECE_ATTACKS_KING",

				"SPACE", "TEMPO"
		};
		doTuning(this::printParameters);
	}

	void printParameters(int[] parameters) {
		try {
			TunedEvaluator evaluator = new TunedEvaluator();
			int parameterIndex = 0;
			for (String fieldName : FIELD_NAMES) {
				Field field = TunedEvaluator.class.getField(fieldName);

				String typeName = field.getType().getSimpleName();
				if ("int".equals(typeName)) {
					System.out.println("public int " + fieldName + " = " +oe(parameters, parameterIndex)+";");
					parameterIndex += 2;
				} else if ("int[]".equals(typeName)) {
					int[] fieldValue = (int[]) field.get(evaluator);

					if (fieldName.contains("PCSQ")) {
						System.out.println("public final int[] " + fieldName + " = {");
						for (int i = 0; i < fieldValue.length; i++) {
							System.out.print(oe(parameters, parameterIndex) + (i != fieldValue.length - 1 ? ", " : ""));
							parameterIndex += 2;
							if (i % 8 == 7) {
								System.out.println();
							}
						}
					} else {
						System.out.print("public final int[] " + fieldName + " = {");
						for (int i = 0; i < fieldValue.length; i++) {
							System.out.print(oe(parameters, parameterIndex) + (i != fieldValue.length - 1 ? ", " : ""));
							parameterIndex += 2;
						}
					}
					System.out.println("};");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String oe(int[] parameters, int parameterIndex) {
		return (parameters[parameterIndex] == 0 && parameters[parameterIndex + 1] == 0) ? "0" :
				"oe(" + parameters[parameterIndex] + ", " + parameters[parameterIndex + 1] + ")";
	}

}
