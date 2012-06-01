package com.alonsoruibal.chess.bitboard;

import java.util.Random;

/**
 * Generates magic numbers without postmask!!!! by try and error
 * @author rui
 */
public class MagicNumbersGenerator {

	public final static Random random = new Random(System.currentTimeMillis());
	 
	/**
	 * Generates a randon number with few bits
	 * @return
	 */
	private long randomFewbits() {
		  return random.nextLong() & random.nextLong() & random.nextLong() & random.nextLong() & random.nextLong();
	}
	 
	/**
	 * Routine to generate our own magic bits
	 * @param index of the square
	 * @param m bits needed
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
	 
	  mask = (bishop? BitboardAttacks.bishopMask[index] : BitboardAttacks.rookMask[index]);
	  //System.out.println("mask:\n" + BitboardUtils.toString(mask));
	  
	  // Fill Attack tables, those are very big!
	  n = BitboardUtils.popCount(mask);
	  for (i = 0; i < (1 << n); i++) {
	    block[i] = BitboardAttacks.generatePieces(i, n, mask);
		//System.out.println("b:\n" + BitboardUtils.toString(block[i]));
	    attack[i] = bishop ? BitboardAttacks.getBishopShiftAttacks(BitboardUtils.index2Square(index), block[i]) :
	    BitboardAttacks.getRookShiftAttacks(BitboardUtils.index2Square(index), block[i]);
		//System.out.println("attack:\n" + BitboardUtils.toString(attack[i]));
	  }
	  
	  for (k = 0; k < 1000000000; k++) {
	    // test new magic
		magic = randomFewbits();
	    if (BitboardUtils.popCount((mask * magic) & 0xFF00000000000000L) < 6) continue;
	    
	    // First, empty magic attack table
	    for (i = 0; i < 4096; i++) magicAttack[i] = 0L;
	    
	    for (i = 0, fail = false; !fail && (i < (1 << n)); i++) {
	      j = BitboardAttacks.magicTransform(block[i], magic, m);
	      if (magicAttack[j] == 0L) magicAttack[j] = attack[i];
	      else if (magicAttack[j] != attack[i]) fail = true;
	      
	      // Verifies that using a postmask is not necessary
	      if (attack[i] != (attack[i] & (bishop? BitboardAttacks.bishop[index] : BitboardAttacks.rook[index]))) fail = true;
	    }
	    
	    if (!fail) return magic;
	  }
	  System.out.println("Error!\n");
	  return 0L;
	}
	 
	public static void main(String args[]) {
	  byte index;
	  MagicNumbersGenerator magic = new MagicNumbersGenerator();
	  
	  System.out.print("public final static long rookMagicNumber[] = {");
	  for (index = 0; index < 64; index++)
		  System.out.print("0x" + Long.toHexString(magic.findMagics(index, BitboardAttacks.rookShiftBits[index], false)) + "L" + (index != 63 ? ", " : ""));
	  System.out.println("};");
	 
	  System.out.print("public final static long bishopMagicNumber[] = {");
	  for (index = 0; index < 64; index++)
		  System.out.print("0x" + Long.toHexString(magic.findMagics(index, BitboardAttacks.bishopShiftBits[index], true)) + "L" + (index != 63 ? ", " : ""));
	  System.out.println("};");
	}	
}
