package com.alonsoruibal.chess.book;

import com.alonsoruibal.chess.Board;
import com.alonsoruibal.chess.Move;
import com.alonsoruibal.chess.log.Logger;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Polyglot opening book support
 *
 * @author rui
 */
public class FileBook implements Book {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger("FileBook");

	private String bookName;

	List<Integer> moves = new ArrayList<>();
	List<Integer> weights = new ArrayList<>();
	long totalWeight;

	private final Random random = new Random();

	public FileBook(String fileName) {
		bookName = fileName;
		logger.debug("Using opening book " + bookName);
	}

	/**
	 * "move" is a bit field with the following meaning (bit 0 is the least significant bit)
	 * <p/>
	 * bits                meaning
	 * ===================================
	 * 0,1,2               to file
	 * 3,4,5               to row
	 * 6,7,8               from file
	 * 9,10,11             from row
	 * 12,13,14            promotion piece
	 * "promotion piece" is encoded as follows
	 * none       0
	 * knight     1
	 * bishop     2
	 * rook       3
	 * queen      4
	 *
	 * @param move
	 * @return
	 */
	private String int2MoveString(int move) {
		StringBuilder sb = new StringBuilder();
		sb.append((char) ('a' + ((move >> 6) & 0x7)));
		sb.append(((move >> 9) & 0x7) + 1);
		sb.append((char) ('a' + (move & 0x7)));
		sb.append(((move >> 3) & 0x7) + 1);
		if (((move >> 12) & 0x7) != 0) sb.append("nbrq".charAt(((move >> 12) & 0x7) - 1));
		return sb.toString();
	}

	public void generateMoves(Board board) {
		totalWeight = 0;
		moves.clear();
		weights.clear();

		long key2Find = board.getKey();

		InputStream bookIs = getClass().getResourceAsStream(bookName);
		if (bookIs == null) {
			// Book resource not found: silently return no moves (the engine will
			// fall back to search). Previously this threw a NullPointerException
			// that was swallowed by the catch below, making the failure invisible.
			return;
		}
		// Use try-with-resources so the stream is always closed: previously the
		// DataInputStream was never closed, leaking a file handle on every book
		// move lookup and relying on the EOFException to exit the loop.
		try (DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(bookIs))) {

			long key;
			int moveInt;
			int weight;

			while (true) {
				key = dataInputStream.readLong();
				if (key == key2Find) {
					moveInt = dataInputStream.readShort();
					weight = dataInputStream.readShort();
					dataInputStream.readInt(); // Unused learn field

					int move = Move.getFromString(board, int2MoveString(moveInt), true);
					// Add only if it is legal
					if (board.getLegalMove(move) != Move.NONE) {
						moves.add(move);
						weights.add(weight);
						totalWeight += weight;
					}
				} else {
					// Skip the remaining 8 bytes of a non-matching entry. skipBytes may
					// return fewer bytes than requested (per its contract); read the
					// leftover bytes individually so a short skip cannot desynchronize
					// the whole scan and match wrong keys.
					int toSkip = 8;
					while (toSkip > 0) {
						int skipped = dataInputStream.skipBytes(toSkip);
						if (skipped <= 0) {
							// No progress: fall back to a single-byte read to advance.
							if (dataInputStream.readByte() == -1) {
								break;
							}
							toSkip--;
						} else {
							toSkip -= skipped;
						}
					}
				}
			}
		} catch (Exception ignored) {
			// EOFException is the normal termination of the scan; other I/O errors
			// leave the book unusable, so returning whatever was collected so far is fine.
		}
	}

	/**
	 * Gets a random move from the book taking care of weights
	 */
	public int getMove(Board board) {
		generateMoves(board);
		if (moves.isEmpty() || totalWeight <= 0) {
			// No legal book moves or all weights zero: return NONE instead of
			// arbitrarily returning moves.get(0) (which the old <= 0 check did).
			return Move.NONE;
		}
		// Use double precision for the random weight: the previous
		// Float.valueOf(random.nextFloat() * totalWeight).longValue() lost precision
		// for totalWeight above 2^24 (float mantissa width), systematically under-
		// selecting small-weight moves.
		long randomWeight = (long) (random.nextDouble() * totalWeight);
		for (int i = 0; i < moves.size(); i++) {
			randomWeight -= weights.get(i);
			if (randomWeight <= 0) {
				return moves.get(i);
			}
		}
		return moves.get(moves.size() - 1);
	}
}