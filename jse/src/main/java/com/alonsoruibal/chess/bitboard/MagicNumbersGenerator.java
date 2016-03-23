package com.alonsoruibal.chess.bitboard;

import java.util.Random;

/**
 * Generates magic numbers without postmask!!!! by try and error
 *
 * @author rui
 */
public class MagicNumbersGenerator {

	public final static Random random = new Random(System.currentTimeMillis());

	/**
	 * Generates a randon number with few bits
	 *
	 * @return
	 */
	private long randomFewbits() {
		return random.nextLong() & random.nextLong() & random.nextLong() & random.nextLong() & random.nextLong();
	}


	/**
	 * Fills pieces from a mask. Neccesary for magic generation variable bits is
	 * the mask bytes number index goes from 0 to 2^bits
	 */
	private long generatePieces(int index, int bits, long mask) {
		int i;
		long lsb;
		long result = 0L;
		for (i = 0; i < bits; i++) {
			lsb = mask & (-mask);
			mask ^= lsb; // Deactivates lsb bit of the mask to get next bit next time
			if ((index & (1 << i)) != 0)
				result |= lsb; // if bit is set to 1
		}
		return result;
	}

	/**
	 * Routine to generate our own magic bits
	 *
	 * @param index  of the square
	 * @param m      bits needed
	 * @param bishop is a bishop or rook?
	 * @return
	 */
	long findMagics(byte index, byte m, boolean bishop) {
		long mask, magic;
		long attack[] = new long[4096];
		long block[] = new long[4096];
		long magicAttack[] = new long[4096];
		int i, j, k, n;
		boolean fail;

		BitboardAttacksMagic attacks = (BitboardAttacksMagic) BitboardAttacks.getInstance();

		mask = (bishop ? attacks.bishopMask[index] : attacks.rookMask[index]);
		// System.out.println("mask:\n" + BitboardUtils.toString(mask));

		// Fill Attack tables, those are very big!
		n = BitboardUtils.popCount(mask);
		for (i = 0; i < (1 << n); i++) {
			block[i] = generatePieces(i, n, mask);
			// System.out.println("b:\n" + BitboardUtils.toString(block[i]));
			attack[i] = bishop ? attacks.getBishopShiftAttacks(BitboardUtils.index2Square(index), block[i]) : attacks.getRookShiftAttacks(
					BitboardUtils.index2Square(index), block[i]);
			// System.out.println("attack:\n" +
			// BitboardUtils.toString(attack[i]));
		}

		for (k = 0; k < 1000000000; k++) {
			// test new magic
			magic = randomFewbits();
			if (BitboardUtils.popCount((mask * magic) & 0xFF00000000000000L) < 6)
				continue;

			// First, empty magic attack table
			for (i = 0; i < 4096; i++)
				magicAttack[i] = 0L;

			for (i = 0, fail = false; !fail && (i < (1 << n)); i++) {
				j = BitboardAttacksMagic.magicTransform(block[i], magic, m);
				if (magicAttack[j] == 0L) {
					magicAttack[j] = attack[i];
				} else if (magicAttack[j] != attack[i]) {
					fail = true;
				}

				// Verifies that using a postmask is not necessary
				if (attack[i] != (attack[i] & (bishop ? attacks.bishop[index] : attacks.rook[index]))) {
					fail = true;
				}
			}

			if (!fail)
				return magic;
		}
		System.out.println("Error!\n");
		return 0L;
	}

	public static void main(String args[]) {
		byte index;
		MagicNumbersGenerator magic = new MagicNumbersGenerator();

		System.out.print("public static final long rookMagicNumber[] = {");
		for (index = 0; index < 64; index++) {
			System.out.print("0x" + Long.toHexString(magic.findMagics(index, BitboardAttacksMagic.rookShiftBits[index], false)) + "L"
					+ (index != 63 ? ", " : ""));
		}
		System.out.println("};");

		System.out.print("public static final long bishopMagicNumber[] = {");
		for (index = 0; index < 64; index++) {
			System.out.print("0x" + Long.toHexString(magic.findMagics(index, BitboardAttacksMagic.bishopShiftBits[index], true)) + "L"
					+ (index != 63 ? ", " : ""));
		}
		System.out.println("};");
	}
}
