/*
 * Code converted with Sharpen
 * 
 */using Sharpen;

namespace Com.Alonsoruibal.Chess
{
	/// <summary>Holds configuration parameters</summary>
	/// <author>rui</author>
	public class Config
	{
		public bool experimental = false;

		public const bool DEFAULT_USE_BOOK = true;

		public const int DEFAULT_BOOK_KNOWGLEDGE = 100;

		public static readonly string DEFAULT_EVALUATOR = "experimental";

		public const bool DEFAULT_NULL_MOVE = true;

		public const int DEFAULT_NULL_MOVE_MARGIN = 200;

		public const bool DEFAULT_STATIC_NULL_MOVE = true;

		public const bool DEFAULT_IID = true;

		public const int DEFAULT_IID_MARGIN = 300;

		public const bool DEFAULT_LMR = true;

		public const int DEFAULT_EXTENSIONS_CHECK = 2;

		public const int DEFAULT_EXTENSIONS_MATE_THREAT = 1;

		public const int DEFAULT_EXTENSIONS_PAWN_PUSH = 1;

		public const int DEFAULT_EXTENSIONS_PASSED_PAWN = 1;

		public const int DEFAULT_EXTENSIONS_RECAPTURE = 2;

		public const int DEFAULT_EXTENSIONS_SINGULAR = 2;

		public const int DEFAULT_SINGULAR_EXTENSION_MARGIN = 50;

		public const bool DEFAULT_ASPIRATION_WINDOW = true;

		public static readonly string DEFAULT_ASPIRATION_WINDOW_SIZES = "10,25,150,400,550,1025";

		public const int DEFAULT_TRANSPOSITION_TABLE_SIZE = 64;

		public const bool DEFAULT_FUTILITY = true;

		public const int DEFAULT_FUTILITY_MARGIN = 100;

		public const bool DEFAULT_AGGRESIVE_FUTILITY = true;

		public const int DEFAULT_AGGRESIVE_FUTILITY_MARGIN = 200;

		public const int DEFAULT_FUTILITY_MARGIN_QS = 150;

		public const bool DEFAULT_RAZORING = true;

		public const int DEFAULT_RAZORING_MARGIN = 900;

		public const int DEFAULT_CONTEMPT_FACTOR = 90;

		public const int DEFAULT_EVAL_CENTER = 100;

		public const int DEFAULT_EVAL_POSITIONAL = 100;

		public const int DEFAULT_EVAL_ATTACKS = 100;

		public const int DEFAULT_EVAL_MOBILITY = 100;

		public const int DEFAULT_EVAL_PAWN_STRUCTURE = 100;

		public const int DEFAULT_EVAL_PASSED_PAWNS = 100;

		public const int DEFAULT_EVAL_KING_SAFETY = 100;

		public const int DEFAULT_RAND = 0;

		private bool useBook = DEFAULT_USE_BOOK;

		private Com.Alonsoruibal.Chess.Book.Book book;

		private int bookKnowledge = DEFAULT_BOOK_KNOWGLEDGE;

		private string evaluator = DEFAULT_EVALUATOR;

		private bool nullMove = DEFAULT_NULL_MOVE;

		private int nullMoveMargin = DEFAULT_NULL_MOVE_MARGIN;

		private bool staticNullMove = DEFAULT_STATIC_NULL_MOVE;

		private bool iid = DEFAULT_IID;

		private int iidMargin = DEFAULT_IID_MARGIN;

		private bool lmr = DEFAULT_LMR;

		private int extensionsCheck = DEFAULT_EXTENSIONS_CHECK;

		private int extensionsMateThreat = DEFAULT_EXTENSIONS_MATE_THREAT;

		private int extensionsPawnPush = DEFAULT_EXTENSIONS_PAWN_PUSH;

		private int extensionsPassedPawn = DEFAULT_EXTENSIONS_PASSED_PAWN;

		private int extensionsRecapture = DEFAULT_EXTENSIONS_RECAPTURE;

		private int extensionsSingular = DEFAULT_EXTENSIONS_SINGULAR;

		private int singularExtensionMargin = DEFAULT_SINGULAR_EXTENSION_MARGIN;

		private bool aspirationWindow = DEFAULT_ASPIRATION_WINDOW;

		private int[] aspirationWindowSizes;

		private int transpositionTableSize = DEFAULT_TRANSPOSITION_TABLE_SIZE;

		private bool futility = DEFAULT_FUTILITY;

		private int futilityMargin = DEFAULT_FUTILITY_MARGIN;

		private bool aggressiveFutility = DEFAULT_AGGRESIVE_FUTILITY;

		private int aggressiveFutilityMargin = DEFAULT_AGGRESIVE_FUTILITY_MARGIN;

		private int futilityMarginQS = DEFAULT_FUTILITY_MARGIN_QS;

		private bool razoring = DEFAULT_RAZORING;

		private int razoringMargin = DEFAULT_RAZORING_MARGIN;

		private int contemptFactor = DEFAULT_CONTEMPT_FACTOR;

		private int evalCenter = DEFAULT_EVAL_CENTER;

		private int evalPositional = DEFAULT_EVAL_POSITIONAL;

		private int evalAttacks = DEFAULT_EVAL_ATTACKS;

		private int evalMobility = DEFAULT_EVAL_MOBILITY;

		private int evalPawnStructure = DEFAULT_EVAL_PAWN_STRUCTURE;

		private int evalPassedPawns = DEFAULT_EVAL_PASSED_PAWNS;

		private int evalKingSafety = DEFAULT_EVAL_KING_SAFETY;

		private int rand = DEFAULT_RAND;

		public Config()
		{
			// For manual testing
			// Default values are static field to be used also from UCIEngine
			// 2 = 1 PLY
			// >0 refuses draw <0 looks for draw
			// It is initialized in constructor
			SetAspirationWindowSizes(DEFAULT_ASPIRATION_WINDOW_SIZES);
		}

		public virtual bool GetUseBook()
		{
			return useBook;
		}

		public virtual void SetUseBook(bool useBook)
		{
			this.useBook = useBook;
		}

		public virtual Com.Alonsoruibal.Chess.Book.Book GetBook()
		{
			return book;
		}

		public virtual void SetBook(Com.Alonsoruibal.Chess.Book.Book book)
		{
			this.book = book;
		}

		public virtual int GetBookKnowledge()
		{
			return bookKnowledge;
		}

		public virtual void SetBookKnowledge(int bookKnowledge)
		{
			this.bookKnowledge = bookKnowledge;
		}

		public virtual string GetEvaluator()
		{
			return evaluator;
		}

		public virtual void SetEvaluator(string evaluator)
		{
			this.evaluator = evaluator;
		}

		public virtual bool GetNullMove()
		{
			return nullMove;
		}

		public virtual void SetNullMove(bool nullMove)
		{
			this.nullMove = nullMove;
		}

		public virtual int GetNullMoveMargin()
		{
			return nullMoveMargin;
		}

		public virtual void SetNullMoveMargin(int nullMoveMargin)
		{
			this.nullMoveMargin = nullMoveMargin;
		}

		public virtual bool GetStaticNullMove()
		{
			return staticNullMove;
		}

		public virtual void SetStaticNullMove(bool staticNullMove)
		{
			this.staticNullMove = staticNullMove;
		}

		public virtual bool GetIid()
		{
			return iid;
		}

		public virtual void GetIid(bool iid)
		{
			this.iid = iid;
		}

		public virtual int GetIidMargin()
		{
			return iidMargin;
		}

		public virtual void SetIidMargin(int iidMargin)
		{
			this.iidMargin = iidMargin;
		}

		public virtual bool GetLmr()
		{
			return lmr;
		}

		public virtual void SetLmr(bool lmr)
		{
			this.lmr = lmr;
		}

		public virtual bool GetFutility()
		{
			return futility;
		}

		public virtual void SetFutility(bool futility)
		{
			this.futility = futility;
		}

		public virtual int GetFutilityMargin()
		{
			return futilityMargin;
		}

		public virtual void SetFutilityMargin(int futilityMargin)
		{
			this.futilityMargin = futilityMargin;
		}

		public virtual bool GetAggressiveFutility()
		{
			return aggressiveFutility;
		}

		public virtual void SetAggressiveFutility(bool aggressiveFutility)
		{
			this.aggressiveFutility = aggressiveFutility;
		}

		public virtual int GetAggressiveFutilityMargin()
		{
			return aggressiveFutilityMargin;
		}

		public virtual void SetAggressiveFutilityMargin(int aggressiveFutilityMargin)
		{
			this.aggressiveFutilityMargin = aggressiveFutilityMargin;
		}

		public virtual int GetFutilityMarginQS()
		{
			return futilityMarginQS;
		}

		public virtual void SetFutilityMarginQS(int futilityMarginQS)
		{
			this.futilityMarginQS = futilityMarginQS;
		}

		public virtual bool GetAspirationWindow()
		{
			return aspirationWindow;
		}

		public virtual void SetAspirationWindow(bool aspirationWindow)
		{
			this.aspirationWindow = aspirationWindow;
		}

		public virtual int[] GetAspirationWindowSizes()
		{
			return aspirationWindowSizes;
		}

		public virtual void SetAspirationWindowSizes(string aspirationWindowSizesString)
		{
			string[] aux = aspirationWindowSizesString.Split(",");
			aspirationWindowSizes = new int[aux.Length];
			for (int i = 0; i < aux.Length; i++)
			{
				aspirationWindowSizes[i] = System.Convert.ToInt32(aux[i]);
			}
		}

		public virtual int GetTranspositionTableSize()
		{
			return transpositionTableSize;
		}

		public virtual void SetTranspositionTableSize(int transpositionTableSize)
		{
			this.transpositionTableSize = transpositionTableSize;
		}

		public virtual int GetExtensionsCheck()
		{
			return extensionsCheck;
		}

		public virtual void SetExtensionsCheck(int extensionsCheck)
		{
			this.extensionsCheck = extensionsCheck;
		}

		public virtual int GetExtensionsMateThreat()
		{
			return extensionsMateThreat;
		}

		public virtual void SetExtensionsMateThreat(int extensionsMateThreat)
		{
			this.extensionsMateThreat = extensionsMateThreat;
		}

		public virtual int GetExtensionsPawnPush()
		{
			return extensionsPawnPush;
		}

		public virtual void SetExtensionsPawnPush(int extensionsPawnPush)
		{
			this.extensionsPawnPush = extensionsPawnPush;
		}

		public virtual int GetExtensionsPassedPawn()
		{
			return extensionsPassedPawn;
		}

		public virtual void SetExtensionsPassedPawn(int extensionsPassedPawn)
		{
			this.extensionsPassedPawn = extensionsPassedPawn;
		}

		public virtual int GetExtensionsRecapture()
		{
			return extensionsRecapture;
		}

		public virtual void SetExtensionsRecapture(int extensionsRecapture)
		{
			this.extensionsRecapture = extensionsRecapture;
		}

		public virtual int GetExtensionsSingular()
		{
			return extensionsSingular;
		}

		public virtual void SetExtensionsSingular(int extensionsSingular)
		{
			this.extensionsSingular = extensionsSingular;
		}

		public virtual int GetSingularExtensionMargin()
		{
			return singularExtensionMargin;
		}

		public virtual void SetSingularExtensionMargin(int singularExtensionMargin)
		{
			this.singularExtensionMargin = singularExtensionMargin;
		}

		public virtual int GetContemptFactor()
		{
			return contemptFactor;
		}

		public virtual void SetContemptFactor(int contemptFactor)
		{
			this.contemptFactor = contemptFactor;
		}

		public virtual int GetEvalCenter()
		{
			return evalCenter;
		}

		public virtual void SetEvalCenter(int evalCenter)
		{
			this.evalCenter = evalCenter;
		}

		public virtual int GetEvalPositional()
		{
			return evalPositional;
		}

		public virtual void SetEvalPositional(int evalPositional)
		{
			this.evalPositional = evalPositional;
		}

		public virtual int GetEvalAttacks()
		{
			return evalAttacks;
		}

		public virtual void SetEvalAttacks(int evalAttacks)
		{
			this.evalAttacks = evalAttacks;
		}

		public virtual int GetEvalMobility()
		{
			return evalMobility;
		}

		public virtual void SetEvalMobility(int evalMobility)
		{
			this.evalMobility = evalMobility;
		}

		public virtual int GetEvalPawnStructure()
		{
			return evalPawnStructure;
		}

		public virtual void SetEvalPawnStructure(int evalPawnStructure)
		{
			this.evalPawnStructure = evalPawnStructure;
		}

		public virtual int GetEvalPassedPawns()
		{
			return evalPassedPawns;
		}

		public virtual void SetEvalPassedPawns(int evalPassedPawns)
		{
			this.evalPassedPawns = evalPassedPawns;
		}

		public virtual int GetEvalKingSafety()
		{
			return evalKingSafety;
		}

		public virtual void SetEvalKingSafety(int evalKingSafety)
		{
			this.evalKingSafety = evalKingSafety;
		}

		public virtual int GetRand()
		{
			return rand;
		}

		public virtual void SetRand(int rand)
		{
			this.rand = rand;
		}

		public virtual bool GetRazoring()
		{
			return razoring;
		}

		public virtual void SetRazoring(bool razoring)
		{
			this.razoring = razoring;
		}

		public virtual int GetRazoringMargin()
		{
			return razoringMargin;
		}

		public virtual void SetRazoringMargin(int razoringMargin)
		{
			this.razoringMargin = razoringMargin;
		}

		/// <summary>2100 is the max, 500 the min</summary>
		/// <param name="engineElo"></param>
		public virtual void SetElo(int engineElo)
		{
			int kPercentage = ((engineElo - 500) * 100) / 1600;
			// knowledge percentage
			int bookPercentage = ((engineElo - 500) * 100) / 1600;
			// book knowledge percentage
			int ePercentage = 88 - ((engineElo - 500) * 88) / 1600;
			// percentage of errors
			SetRand(ePercentage);
			SetUseBook(true);
			SetBookKnowledge(bookPercentage);
			SetEvalPawnStructure(kPercentage);
			SetEvalPassedPawns(kPercentage);
			SetEvalKingSafety(kPercentage);
			SetEvalMobility(kPercentage);
			SetEvalPositional(kPercentage);
			SetEvalCenter(kPercentage);
		}

		public override string ToString()
		{
			return "Config [aggressiveFutility=" + aggressiveFutility + ", aggressiveFutilityMargin="
				 + aggressiveFutilityMargin + ", aspirationWindow=" + aspirationWindow + ", aspirationWindowSizes="
				 + Arrays.ToString(aspirationWindowSizes) + ", book=" + book + ", contemptFactor="
				 + contemptFactor + ", evalKingSafety=" + evalKingSafety + ", evalMobility=" + evalMobility
				 + ", evalPassedPawns=" + evalPassedPawns + ", evalPawnStructure=" + evalPawnStructure
				 + ", evaluator=" + evaluator + ", extensionsCheck=" + extensionsCheck + ", extensionsMateThreat="
				 + extensionsMateThreat + ", extensionsPawnPush=" + extensionsPawnPush + ", extensionsPassedPawn="
				 + extensionsPassedPawn + ", extensionsRecapture=" + extensionsRecapture + ", extensionsSingular="
				 + extensionsSingular + ", singularExtensionMargin=" + singularExtensionMargin +
				 ", futility=" + futility + ", futilityMargin=" + futilityMargin + ", iid=" + iid
				 + ", lmr=" + lmr + ", nullMove=" + nullMove + ", nullMoveMargin=" + nullMoveMargin
				 + ", staticNullMove=" + staticNullMove + ", razoring=" + razoring + ", razoringMargin="
				 + razoringMargin + ", transpositionTableSize=" + transpositionTableSize + ", useBook="
				 + useBook + "]";
		}
	}
}
