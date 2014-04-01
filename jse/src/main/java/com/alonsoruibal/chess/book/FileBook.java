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

	List<Integer> moves = new ArrayList<Integer>();
	List<Integer> weights = new ArrayList<Integer>();
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

		try {
			InputStream bookIs = getClass().getResourceAsStream(bookName);
			DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(bookIs));

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
					if (board.isMoveLegal(move)) {
						moves.add(move);
						weights.add(weight);
						totalWeight += weight;
					}
				} else {
					dataInputStream.skipBytes(8);
				}
			}
		} catch (Exception ignored) {
		}
	}

	/**
	 * Gets a random move from the book taking care of weights
	 */
	public int getMove(Board board) {
		generateMoves(board);
		long randomWeight = (new Float(random.nextFloat() * totalWeight)).longValue();
		for (int i = 0; i < moves.size(); i++) {
			randomWeight -= weights.get(i);
			if (randomWeight <= 0) {
				return moves.get(i);
			}
		}
		return 0;
	}
}