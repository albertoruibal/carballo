/*
 * Code converted with Sharpen
 * 
 */using System.Text;
using Com.Alonsoruibal.Chess.Evaluation;
using Com.Alonsoruibal.Chess.Search;
using Sharpen;

namespace Com.Alonsoruibal.Chess.Search
{
	/// <summary>
	/// info
	/// the engine wants to send infos to the GUI.
	/// </summary>
	/// <remarks>
	/// info
	/// the engine wants to send infos to the GUI. This should be done whenever one of the info has changed.
	/// The engine can send only selected infos and multiple infos can be send with one info command,
	/// e.g. "info currmove e2e4 currmovenumber 1" or
	/// "info depth 12 nodes 123456 nps 100000".
	/// Also all infos belonging to the pv should be sent together
	/// e.g. "info depth 2 score cp 214 time 1242 nodes 2124 nps 34928 pv e2e4 e7e5 g1f3"
	/// I suggest to start sending "currmove", "currmovenumber", "currline" and "refutation" only after one second
	/// to avoid too much traffic.
	/// Additional info:
	/// depth
	/// search depth in plies
	/// seldepth
	/// selective search depth in plies,
	/// if the engine sends seldepth there must also a "depth" be present in the same string.
	/// time
	/// the time searched in ms, this should be sent together with the pv.
	/// nodes
	/// x nodes searched, the engine should send this info regularly
	/// pv  ...
	/// the best line found
	/// multipv
	/// this for the multi pv mode.
	/// for the best move/pv add "multipv 1" in the string when you send the pv.
	/// in k-best mode always send all k variants in k strings together.
	/// score
	/// * cp
	/// the score from the engine's point of view in centipawns.
	/// * mate
	/// mate in y moves, not plies.
	/// If the engine is getting mated use negativ values for y.
	/// * lowerbound
	/// the score is just a lower bound.
	/// * upperbound
	/// the score is just an upper bound.
	/// currmove
	/// currently searching this move
	/// currmovenumber
	/// currently searching move number x, for the first move x should be 1 not 0.
	/// hashfull
	/// the hash is x permill full, the engine should send this info regularly
	/// nps
	/// x nodes per second searched, the engine should send this info regularly
	/// tbhits
	/// x positions where found in the endgame table bases
	/// cpuload
	/// the cpu usage of the engine is x permill.
	/// string
	/// any string str which will be displayed be the engine,
	/// if there is a string command the rest of the line will be interpreted as .
	/// refutation   ...
	/// move  is refuted by the line  ... , i can be any number &gt;= 1.
	/// Example: after move d1h5 is searched, the engine can send
	/// "info refutation d1h5 g6h5"
	/// if g6h5 is the best answer after d1h5 or if g6h5 refutes the move d1h5.
	/// if there is norefutation for d1h5 found, the engine should just send
	/// "info refutation d1h5"
	/// The engine should only send this if the option "UCI_ShowRefutations" is set to true.
	/// currline   ...
	/// this is the current line the engine is calculating.  is the number of the cpu if
	/// the engine is running on more than one cpu.  = 1,2,3....
	/// if the engine is just using one cpu,  can be omitted.
	/// If  is greater than 1, always send all k lines in k strings together.
	/// The engine should only send this if the option "UCI_ShowCurrLine" is set to true.
	/// </remarks>
	public class SearchStatusInfo
	{
		internal int depth;

		internal int selDepth;

		internal long time;

		internal long nodes;

		internal string pv;

		internal int multiPv;

		internal int scoreMate;

		internal int scoreCp;

		internal string currMove;

		internal int currMoveNumber;

		internal int hashFull;

		internal long nps;

		internal int tbHits;

		internal int cpuLoad;

		internal string @string;

		internal string refutation;

		internal string currLine;

		public virtual int GetDepth()
		{
			return depth;
		}

		public virtual void SetDepth(int depth)
		{
			this.depth = depth;
		}

		public virtual int GetSelDepth()
		{
			return selDepth;
		}

		public virtual void SetSelDepth(int selDepth)
		{
			this.selDepth = selDepth;
		}

		public virtual long GetTime()
		{
			return time;
		}

		public virtual void SetTime(long time)
		{
			this.time = time;
		}

		public virtual long GetNodes()
		{
			return nodes;
		}

		public virtual void SetNodes(long nodes)
		{
			this.nodes = nodes;
		}

		public virtual string GetPv()
		{
			return pv;
		}

		public virtual void SetPv(string pv)
		{
			this.pv = pv;
		}

		public virtual int GetMultiPv()
		{
			return multiPv;
		}

		public virtual void SetMultiPv(int multiPv)
		{
			this.multiPv = multiPv;
		}

		public virtual int GetScoreMate()
		{
			return scoreMate;
		}

		public virtual void SetScoreMate(int scoreMate)
		{
			this.scoreMate = scoreMate;
		}

		public virtual int GetScoreCp()
		{
			return scoreCp;
		}

		public virtual void SetScoreCp(int score)
		{
			this.scoreCp = score;
		}

		public virtual string GetCurrMove()
		{
			return currMove;
		}

		public virtual void SetCurrMove(string currMove)
		{
			this.currMove = currMove;
		}

		public virtual int GetCurrMoveNumber()
		{
			return currMoveNumber;
		}

		public virtual void SetCurrMoveNumber(int currMoveNumber)
		{
			this.currMoveNumber = currMoveNumber;
		}

		public virtual int GetHashFull()
		{
			return hashFull;
		}

		public virtual void SetHashFull(int hashFull)
		{
			this.hashFull = hashFull;
		}

		public virtual long GetNps()
		{
			return nps;
		}

		public virtual void SetNps(long nps)
		{
			this.nps = nps;
		}

		public virtual int GetTbHits()
		{
			return tbHits;
		}

		public virtual void SetTbHits(int tbHits)
		{
			this.tbHits = tbHits;
		}

		public virtual int GetCpuLoad()
		{
			return cpuLoad;
		}

		public virtual void SetCpuLoad(int cpuLoad)
		{
			this.cpuLoad = cpuLoad;
		}

		public virtual string GetString()
		{
			return @string;
		}

		public virtual void SetString(string @string)
		{
			this.@string = @string;
		}

		public virtual string GetRefutation()
		{
			return refutation;
		}

		public virtual void SetRefutation(string refutation)
		{
			this.refutation = refutation;
		}

		public virtual string GetCurrLine()
		{
			return currLine;
		}

		public virtual void SetCurrLine(string currLine)
		{
			this.currLine = currLine;
		}

		public virtual void SetScore(int score)
		{
			if ((score < -Evaluator.VICTORY + 100) || (score > Evaluator.VICTORY - 100))
			{
				int x = (score < 0 ? -Evaluator.VICTORY : Evaluator.VICTORY) - score;
				if ((x & 1) != 0)
				{
					scoreMate = (x >> 1) + 1;
				}
				else
				{
					scoreMate = x >> 1;
				}
			}
			else
			{
				scoreCp = score;
			}
		}

		/// <summary>
		/// in UCI format
		/// TODO complete
		/// </summary>
		public override string ToString()
		{
			StringBuilder sb = new StringBuilder();
			if (depth != 0)
			{
				sb.Append("depth ");
				sb.Append(depth);
			}
			if (scoreMate != null)
			{
				sb.Append(" score mate ");
				sb.Append(scoreMate);
			}
			else
			{
				if (scoreCp != null)
				{
					sb.Append(" score cp ");
					sb.Append(scoreCp);
				}
			}
			if (nodes != 0)
			{
				sb.Append(" nodes ");
				sb.Append(nodes);
			}
			if (time != 0)
			{
				sb.Append(" time ");
				sb.Append(time);
			}
			if (nps != 0)
			{
				sb.Append(" nps ");
				sb.Append(nps);
			}
			if (pv != null)
			{
				sb.Append(" pv ");
				sb.Append(pv);
			}
			return sb.ToString();
		}
	}
}
