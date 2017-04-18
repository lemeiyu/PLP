package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {
		ASTNode par = null;
		par = program();
		matchEOF();
		return par;
	}

	Expression expression() throws SyntaxException {
		// TODO
		Expression expr0 = null;
		Expression expr1 = null;
		Token firstToken = t;
		expr0 = term();
		while (isRelOp(t)) {
			Token relOp = t;
			consume();
			expr1 = term();
			expr0 = new BinaryExpression(firstToken, expr0, relOp, expr1);
		}
		
		return expr0;
	}

	Expression term() throws SyntaxException {
		// TODO
		Expression term0 = null;
		Expression term1 = null;
		Token firstToken = t;
		term0 = elem();
		while (isWeakOp(t)) {
			Token weakOp = t;
			consume();
			term1 = elem();
			term0 = new BinaryExpression(firstToken, term0, weakOp, term1);
		}
		
		return term0;

	}

	Expression elem() throws SyntaxException {
		// TODO
		Expression elem0 = null;
		Expression elem1 = null;
		Token firstToken = t;
		elem0 = factor();
		while (isStrongOp(t)) {
			Token strongOp = t;
			consume();
			elem1 = factor();
			elem0 = new BinaryExpression(firstToken, elem0, strongOp, elem1);
		}
		
		return elem0;

	}

	Expression factor() throws SyntaxException {
		Expression fac = null;
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			fac = new IdentExpression(t);
			consume();
		}
			break;
		case INT_LIT: {
			fac = new IntLitExpression(t);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			fac = new BooleanLitExpression(t);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			fac = new ConstantExpression(t);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			fac = expression();
			try {
				match(RPAREN);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("An expression must be surrounded by a pair of parentheses, "
						+ "lack a \")\" at line: " + lPos.line + ", column: " + lPos.posInLine);
			}
		}
			break;
		default:
			LinePos lPos = t.getLinePos();
			throw new SyntaxException("identity, number, boolean or screensetting is expected, but is "
					+ kind.toString() + ": " + t.getText() + ", at line: " + lPos.line + ", column: " + lPos.posInLine);
		}
		
		return fac;
		
	}

	Block block() throws SyntaxException {
		// TODO
		Block block = null;
		Token firstToken = null;
		ArrayList<Dec> decs = new ArrayList<Dec>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		
		try {
			firstToken = match(LBRACE);
		} catch (SyntaxException e) {
			// TODO Auto-generated catch block
			LinePos lPos = t.getLinePos();
			throw new SyntaxException("A program block must be surrounded by a pair of BRACE, "
					+ "lack a \"{\" at line: " + lPos.line + ", column: " + lPos.posInLine);
		}
		while (!t.isKind(RBRACE)) {
			switch (t.kind) {
			case KW_INTEGER:
			case KW_BOOLEAN:
			case KW_IMAGE:
			case KW_FRAME:
				decs.add(dec());
				//dec();

				break;

			case OP_SLEEP:
			case KW_WHILE:
			case KW_IF:
			case IDENT:

			case OP_BLUR:
			case OP_GRAY:
			case OP_CONVOLVE:

			case KW_SHOW:
			case KW_HIDE:
			case KW_MOVE:
			case KW_XLOC:
			case KW_YLOC:

			case OP_WIDTH:
			case OP_HEIGHT:
			case KW_SCALE:
				statements.add(statement());
				//statement();
				break;

			default:
				LinePos lPos = t.getLinePos();
				if (t.isKind(EOF)) {
					throw new SyntaxException("A Program block must be surrounded by a pair of BRACE, "
							+ "lack of a \"}\" at line: " + lPos.line + ", column: " + lPos.posInLine);
				} else {
					throw new SyntaxException("An integer, boolean, image or frame is required for a declaration. "
							+ "Or a sleep, while, if or any identity is requred for a statement." + "but is "
							+ t.kind.toString() + ": " + t.getText() + ", at line: " + lPos.line + ", column: "
							+ lPos.posInLine);
				}
			}//switch end
		}

		try {
			match(RBRACE);
		} catch (SyntaxException e) {
			// TODO Auto-generated catch block
			LinePos lPos = t.getLinePos();
			throw new SyntaxException("A Program block must be surrounded by a pair of BRACE, "
					+ "lack of a \"}\" at line: " + lPos.line + ", column: " + lPos.posInLine);
		}
		
		block = new Block(firstToken, decs, statements);
		
		return block;
		
	}

	Program program() throws SyntaxException {
		// TODO
		Program program = null;
		Token firstToken = null;
		Block block = null;
		ArrayList<ParamDec> params = new ArrayList<ParamDec>();
		try {
			firstToken = match(IDENT);
		} catch (SyntaxException e) {
			// TODO Auto-generated catch block
			LinePos lPos = t.getLinePos();
			throw new SyntaxException("A program needs an ientity as its name."
					+ " Lack of a name of this program, at line: " + lPos.line + ", column: " + lPos.posInLine);
		}
		switch (t.kind) {
		case LBRACE:
			block = block();
			break;

		case KW_INTEGER:
		case KW_FILE:
		case KW_URL:
		case KW_BOOLEAN:
			params.add(paramDec());
			//paramDec();
			while (t.isKind(COMMA)) {
				consume();
				params.add(paramDec());
				//paramDec();
			}
			block = block();
			break;

		default:
			LinePos lPos = t.getLinePos();
			throw new SyntaxException("A \"{\" or a parameter list is required by the program block. " + "At line: "
					+ lPos.line + ", column: " + lPos.posInLine);
		}
		
		program = new Program(firstToken, params, block);
		
		return program;
		
	}

	ParamDec paramDec() throws SyntaxException {
		// TODO
		ParamDec paramDec = null;
		Token firstToken = null;
		Token ident = null;
		switch (t.kind) {
		case KW_URL:
		case KW_FILE:
		case KW_INTEGER:
		case KW_BOOLEAN:
			firstToken = t;
			consume();
			try {
				ident = match(IDENT);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("A parameter needs an identity as its name, "
						+ "lack of a name of this parameter at line: " + lPos.line + ", column: " + lPos.posInLine);
			}

			break;

		default:
			LinePos lPos = t.getLinePos();
			throw new SyntaxException("A parameter needs a KIND to describe itself, "
					+ "lack of a KIND of this parameter or maybe lack of a \"{\" to surround the program block"
					+ "at line: " + lPos.line + ", column: " + lPos.posInLine);
		}
		
		paramDec = new ParamDec(firstToken, ident);
		
		return paramDec;
		
	}

	Dec dec() throws SyntaxException {
		// TODO
		Dec dec = null;
		Token firstToken = t;
		Token ident = null;
		switch (t.kind) {
		case KW_INTEGER:
		case KW_BOOLEAN:
		case KW_IMAGE:
		case KW_FRAME:
			consume();
			try {
				ident = match(IDENT);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("A declaration needs an identity as its name, "
						+ "lack of a name of the variable at line: " + lPos.line + ", column: " + lPos.posInLine);
			}
			break;

		default:
			LinePos lPos = t.getLinePos();
			throw new SyntaxException("A keyword: interger, boolean, image or frame is expected for a declaration,"
					+ " but is " + t.kind.toString() + ": " + t.getText() + ", at line: " + lPos.line + ", column: "
					+ lPos.posInLine);
			// break;
		}
		
		dec = new Dec(firstToken, ident);
		
		return dec;

	}

	Statement statement() throws SyntaxException {
		// TODO
		Statement statement = null;
		Token firstToken = t;
		switch (t.kind) {
		case KW_WHILE:
			Expression whileExpression = null;
			Block whileBlock = null;
			consume();
			try {
				match(LPAREN);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("An expression must be surrounded by a pair of parentheses, "
						+ "lack a \"(\" at line: " + lPos.line + ", column: " + lPos.posInLine);
			}
			whileExpression = expression();
			try {
				match(RPAREN);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("An expression must be surrounded by a pair of parentheses, "
						+ "lack a \")\" at line: " + lPos.line + ", column: " + lPos.posInLine);
			}
			whileBlock = block();
			statement = new WhileStatement(firstToken, whileExpression, whileBlock);
			break;

		case KW_IF:
			Expression ifExpression = null;
			Block ifBlock = null;
			consume();
			try {
				match(LPAREN);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("An expression must be surrounded by a pair of parentheses, "
						+ "lack a \"(\" at line: " + lPos.line + ", column: " + lPos.posInLine);
			}
			ifExpression = expression();
			try {
				match(RPAREN);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("An expression must be surrounded by a pair of parentheses, "
						+ "lack a \")\" at line: " + lPos.line + ", column: " + lPos.posInLine);
			}
			ifBlock = block();
			statement = new IfStatement(firstToken, ifExpression, ifBlock);
			break;

		case OP_SLEEP:
			Expression sleepExpression = null;
			consume();
			sleepExpression = expression();
			try {
				match(SEMI);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException(
						"There must be a SEMI at the end of sleep sentence. But there is " + t.kind.toString() + ": "
								+ t.getText() + ", at line: " + lPos.line + ", column: " + lPos.posInLine);
			}
			statement = new SleepStatement(firstToken, sleepExpression);
			break;

		case IDENT:
			Token t1 = peeknext();
			if (t1.isKind(ASSIGN)){
				//Token firstToken = t;
				IdentLValue identLValue = new IdentLValue(t);
				Expression AssignExpression = null;
				consume();
				match(ASSIGN);
				AssignExpression = expression();
				statement = new AssignmentStatement(firstToken, identLValue, AssignExpression);
			} else if (isArrowOp(t1)) {
				// Chain chain = null;
				statement = chain();
			} else {
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("A statement needs a keyword, " + "lack a keyword at line: " + lPos.line
						+ ", column: " + lPos.posInLine);
			}
			
			try {
				match(SEMI);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("This sentence must end with a SEMI, " + "lack a \";\" at line: " + lPos.line
						+ ", column: " + lPos.posInLine);
			}
			
			break;
			
		case OP_BLUR:
		case OP_GRAY:
		case OP_CONVOLVE:

		case KW_SHOW:
		case KW_HIDE:
		case KW_MOVE:
		case KW_XLOC:
		case KW_YLOC:

		case OP_WIDTH:
		case OP_HEIGHT:
		case KW_SCALE:
			statement = chain();
			try {
				match(SEMI);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("This sentence must end with a SEMI, " + "lack a \";\" at line: " + lPos.line
						+ ", column: " + lPos.posInLine);
			}
			break;

		default:
			LinePos lPos = t.getLinePos();
			throw new SyntaxException(
					"A keyword \"while\", \"if\" or an identity is expected. but is " + t.kind.toString() + ": "
							+ t.getText() + ", at line: " + lPos.line + ", column: " + lPos.posInLine);
		}
		
		return statement;
		
	}

	Chain chain() throws SyntaxException {
		// TODO
		Chain chain = null;
		Token firstToken = t;
		Token arrowOp = null;
		//if (isFilterOp(t) || isFrameOp(t) || isImageOp(t)) {
		chain = chainElem();
		try {
			arrowOp = match(ARROW, BARARROW);
		} catch (SyntaxException e) {
			// TODO Auto-generated catch block
			LinePos lPos = t.getLinePos();
			throw new SyntaxException("At least one arrow operator \"->\", \"|->\" is needed for a chain."
					+ "Lack of a arrow operator at line: " + lPos.line + ", column: " + lPos.posInLine);
		}
		ChainElem chainElem = chainElem();
		chain = new BinaryChain(firstToken, chain, arrowOp, chainElem);
		while (isArrowOp(t)) {
			arrowOp = t;
			consume();
			chainElem = chainElem();
			chain = new BinaryChain(firstToken, chain, arrowOp, chainElem);
		}
		/*} else {
			//t is IDENT
			chain = chainElem();
			try {
				arrowOp = match(ARROW, BARARROW);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("At least one arrow operator \"->\", \"|->\" is needed for a chain."
						+ "Lack of a arrow operator at line: " + lPos.line + ", column: " + lPos.posInLine);
			}
			ChainElem chainElem = chainElem();
			while (isArrowOp(t)) {
				consume();
				chainElem();
			}

		}*/
		
		return chain;
		
	}

	ChainElem chainElem() throws SyntaxException {
		//TODO
		ChainElem chainElem = null;
		Token firstToken = t;
		Tuple arg = null;
		
		if (t.isKind(IDENT)) {
			chainElem = new IdentChain(t);
			consume();
		} else if (isFilterOp(t)) {
			consume();
			arg = arg();
			chainElem = new FilterOpChain(firstToken, arg);
		} else if (isFrameOp(t)) {
			consume();
			arg = arg();
			chainElem = new FrameOpChain(firstToken, arg);
		} else if (isImageOp(t)) {
			consume();
			arg = arg();
			chainElem = new ImageOpChain(firstToken, arg);
		} else {
			/*
			 * try { match(OP_BLUR, OP_GRAY, OP_CONVOLVE, KW_SHOW, KW_HIDE,
			 * KW_MOVE, KW_XLOC, KW_YLOC, OP_WIDTH, OP_HEIGHT, KW_SCALE); }
			 * catch (SyntaxException e) {
			 */
			// TODO Auto-generated catch block
			LinePos lPos = t.getLinePos();
			throw new SyntaxException(
					"A filter, frame or image operator or an identity is needed by a chain element. But is "
							+ t.kind.toString() + ": " + t.getText() + ", at line: " + lPos.line + ", column: "
							+ lPos.posInLine);
			// }

		}

		return chainElem;
		
	}

	Tuple arg() throws SyntaxException {
		//TODO
		Tuple arg = null;
		Token firstToken = t;
		ArrayList <Expression> arglist = new ArrayList <>();
		if (t.isKind(LPAREN)) {
			//try {
			match(LPAREN);
			/*} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("The expression list must be surrounded by a pair of parentheses, " 
						+ "lack a \"(\" at line: " + lPos.line + ", column: " + lPos.posInLine);
			}*/
			arglist.add(expression());
			while (t.isKind(COMMA)) {
				//try {
				match(COMMA);
				/*} catch (SyntaxException e) {
					// TODO Auto-generated catch block
					LinePos lPos = t.getLinePos();
					throw new SyntaxException("An expression must be surrounded by COMMAs, " 
							+ "lack a \",\" at line: " + lPos.line + ", column: " + lPos.posInLine);
				}*/
				arglist.add(expression());
			}
			try {
				match(RPAREN);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				LinePos lPos = t.getLinePos();
				throw new SyntaxException("The expression list must be surrounded by a pair of parentheses, "
						+ "and expressions must be seperated by COMMAs, "
						+ "lack a \")\" or a \",\" at line: " + lPos.line + ", column: " + lPos.posInLine);
			}
		}/*else {
			return arg;
		}*/
		
		arg = new Tuple(firstToken, arglist);
		
		return arg;
		
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("Lack of the expected EOF.");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		LinePos lPos = t.getLinePos();
		throw new SyntaxException("Saw " + t.kind + ", but expected " + kind + ", at line: " + lPos.line 
				+ ", column: " + lPos.posInLine);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		for (int i = 0; i < kinds.length; i++) {
			if (t.isKind(kinds[i])) {
				return consume();
			}
		}
		throw new SyntaxException("No expected symbol matched.");
		//return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}
	
	/**
	 * Peek the next token without change the token number in scanner.
	 * 
	 * @return
	 */
	private Token peeknext() {
		Token tmp = scanner.peek();
		return tmp;
	}
	
	/*
	 * If the token is a relation operation, return true.
	 * 
	 * @return boolean
	 * 
	 */
	private boolean isRelOp(Token t) {
		// TODO Auto-generated method stub
		switch (t.kind) {
		case LE:
		case LT:
		case GT:
		case GE:
		case EQUAL:
		case NOTEQUAL:
			return true;
		default:
			return false;
		}
	}
	
	/*
	 * If the token is a frame operation, return true.
	 * 
	 * @return boolean
	 * 
	 */
	private boolean isImageOp(Token t) {
		// TODO Auto-generated method stub
		switch (t.kind) {
		case OP_WIDTH:
		case OP_HEIGHT:
		case KW_SCALE:
			return true;
		default:
			return false;
		}
	}
	
	/*
	 * If the token is a image operation, return true.
	 * 
	 * @return boolean
	 * 
	 */
	private boolean isFrameOp(Token t) {
		// TODO Auto-generated method stub
		switch (t.kind) {
		case KW_SHOW:
		case KW_HIDE:
		case KW_MOVE:
		case KW_XLOC:
		case KW_YLOC:
			return true;
		default:
			return false;
		}
	}
	
	/*
	 * If the token is a weak operation, return true.
	 * 
	 * @return boolean
	 * 
	 */
	private boolean isWeakOp(Token t) {
		// TODO Auto-generated method stub
		switch (t.kind) {
		case PLUS:
		case MINUS:
		case OR:
			return true;
		default:
			return false;
		}
	}
	
	/*
	 * If the token is a strong operation, return true.
	 * 
	 * @return boolean
	 * 
	 */
	private boolean isStrongOp(Token t) {
		// TODO Auto-generated method stub
		switch (t.kind) {
		case TIMES:
		case DIV:
		case AND:
		case MOD:
			return true;
		default:
			return false;
		}
	}
	
	/*
	 * If the token is a filter operation, return true.
	 * 
	 * @return boolean
	 * 
	 */
	private boolean isFilterOp(Token t) {
		// TODO Auto-generated method stub
		switch (t.kind) {
		case OP_BLUR:
		case OP_GRAY:
		case OP_CONVOLVE:
			return true;
		default:
			return false;
		}
	}
	
	/*
	 * If the token is an arrow operation, return true.
	 * 
	 * @return boolean
	 * 
	 */
	private boolean isArrowOp(Token t) {
		// TODO Auto-generated method stub
		switch (t.kind) {
		case ARROW:
		case BARARROW:
			return true;
		default:
			return false;
		}
	}

}
