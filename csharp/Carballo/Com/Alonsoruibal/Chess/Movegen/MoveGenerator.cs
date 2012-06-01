/*
 * Code converted with Sharpen
 * 
 */using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.Movegen;
using Sharpen;

namespace Com.Alonsoruibal.Chess.Movegen
{
	/// <author>rui</author>
	public interface MoveGenerator
	{
		int GenerateMoves(Board board, int[] moves, int index);
	}
}
