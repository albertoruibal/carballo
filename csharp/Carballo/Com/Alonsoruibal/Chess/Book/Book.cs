/*
 * Code converted with Sharpen
 * 
 */using Com.Alonsoruibal.Chess;
using Sharpen;

namespace Com.Alonsoruibal.Chess.Book
{
	/// <summary>Opening book support</summary>
	/// <author>rui</author>
	public interface Book
	{
		/// <summary>Gets a random move from the book taking care of weights</summary>
		int GetMove(Board board);
	}
}
