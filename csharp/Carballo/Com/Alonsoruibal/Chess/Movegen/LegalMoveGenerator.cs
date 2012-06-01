/*
 * Code converted with Sharpen
 * 
 */using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.Movegen;
using Sharpen;

namespace Com.Alonsoruibal.Chess.Movegen
{
	public class LegalMoveGenerator : MagicMoveGenerator
	{
		/// <summary>get only LEGAL Operations by testing with domove</summary>
		public override int GenerateMoves(Board board, int[] moves, int index)
		{
			int lastIndex = base.GenerateMoves(board, moves, index);
			int j = index;
			for (int i = 0; i < lastIndex; i++)
			{
				if (board.DoMove(moves[i], false))
				{
					moves[j++] = moves[i];
					board.UndoMove();
				}
			}
			return j;
		}
	}
}
