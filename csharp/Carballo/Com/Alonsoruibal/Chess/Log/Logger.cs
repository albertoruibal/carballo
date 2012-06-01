/*
 * Code converted with Sharpen
 * 
 */using Sharpen;

namespace Com.Alonsoruibal.Chess.Log
{
	public class Logger
	{
		public static bool noLog = false;

		internal string prefix;

		private Logger(string prefix)
		{
			this.prefix = prefix;
		}

		public static Com.Alonsoruibal.Chess.Log.Logger GetLogger(string prefix)
		{
			return new Com.Alonsoruibal.Chess.Log.Logger(prefix);
		}

		public virtual void Info(object @in)
		{
			if (noLog)
			{
				return;
			}
			System.Console.Out.Write("INFO ");
			System.Console.Out.Write(prefix);
			System.Console.Out.Write(" - ");
			System.Console.Out.WriteLine(@in.ToString());
		}

		public virtual void Debug(object @in)
		{
			if (noLog)
			{
				return;
			}
			System.Console.Out.Write("DEBUG ");
			System.Console.Out.Write(prefix);
			System.Console.Out.Write(" - ");
			System.Console.Out.WriteLine(@in.ToString());
		}

		public virtual void Error(object @in)
		{
			if (noLog)
			{
				return;
			}
			System.Console.Out.Write("ERROR ");
			System.Console.Out.Write(prefix);
			System.Console.Out.Write(" - ");
			System.Console.Out.WriteLine(@in.ToString());
		}
	}
}
