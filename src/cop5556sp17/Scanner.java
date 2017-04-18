package cop5556sp17;

import java.util.ArrayList;


public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}

	/**
	 * Thrown by Scanner when an illegal character is encountered
	 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}

	/**
	 * Thrown by Scanner when an int literal is not a value that can be
	 * represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
		public IllegalNumberException(String message) {
			super(message);
		}
	}

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;

		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}

	public class Token {
		public final Kind kind;
		public final int pos; // position in input array
		public final int length;

		public int lines;
		public int columns;
		public int intval;

		// returns the text of this Token
		public String getText() {
			// TODO IMPLEMENT THIS
			String text;
			if (kind.equals(Kind.IDENT) || kind.equals(Kind.INT_LIT)) {
				text = chars.substring(pos, pos + length);
			} else {
				text = kind.getText();
			}

			// text = kind.getText();
			return text;
			
		}

		// returns a LinePos object representing the line and column of this
		// Token
		LinePos getLinePos() {
			// TODO IMPLEMENT THIS
			int lines = 0;
			int columns = -1;
			int ipos = pos < chars.length() - 1 ? pos : chars.length() - 1;
			for (int i = 0; i <= ipos; i++) {
				if (chars.charAt(i) == '\n') {
					lines++;
					columns = i;
				}
			}
			columns = pos - columns -1;
			LinePos linepos = new LinePos(lines, columns);
			return linepos;
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		Token(Kind kind, int pos, int length, int lines, int columns) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.lines = lines;
			this.columns = columns;
		}
		/**
		 * Precondition: kind = Kind.INT_LIT, the text can be represented with a
		 * Java int. Note that the validity of the input should have been
		 * checked when the Token was created. So the exception should never be
		 * thrown.
		 * 
		 * @return int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 * @throws IllegalNumberException
		 */
		public int intVal() throws NumberFormatException, IllegalNumberException {
			// TODO IMPLEMENT THIS
			try {
				intval = Integer.parseInt(getText());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				throw new Scanner.IllegalNumberException("illegal number" + chars + "at position" + pos);
			}
			return intval;
		}
		
		public int getIntVal() {
			return intval;
		}

		public boolean isKind(Kind kind) {
			// TODO Auto-generated method stub
			return this.kind.equals(kind);
		}
		
		public Kind getKind() {
			return kind;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Token)) {
				return false;
			}
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (kind != other.kind) {
				return false;
			}
			if (length != other.length) {
				return false;
			}
			if (pos != other.pos) {
				return false;
			}
			return true;
		}

		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();

	}

	public static enum State{
		START, IN_DIGIT, IN_IDENT, AFTER_EQUAL, GOT_BAR, AFTER_BAR, 
		AFTER_NOT, AFTER_LT, AFTER_GT, AFTER_DIV, COMMENT, COMMENT_STAR,
		AFTER_MINUS;
	}

	/**
	 * Initializes Scanner object by traversing chars and adding tokens to
	 * tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0;
		int length = chars.length();
		State state = State.START;
		int startPos = 0;
		int ch;
		lines = 0;
		columns = 0;
		while (pos <= length) {
			ch = pos < length ? chars.charAt(pos) : -1;
			switch (state) {
			case START: {
				pos = skipWhiteSpace(pos);
				ch = pos < length ? chars.charAt(pos) : -1;
				startPos = pos;
				switch (ch) {
				case -1: {
					tokens.add(new Token(Kind.EOF, pos, 0));
					pos++;
					columns++;
				}
					break;

				// Separator begin
				case ';': {
					tokens.add(new Token(Kind.SEMI, startPos, 1));
					pos++;
					columns++;
				}
					break;

				case ',': {
					tokens.add(new Token(Kind.COMMA, startPos, 1));
					pos++;
					columns++;
				}
					break;

				case '(': {
					tokens.add(new Token(Kind.LPAREN, startPos, 1));
					pos++;
					columns++;
				}
					break;

				case ')': {
					tokens.add(new Token(Kind.RPAREN, startPos, 1));
					pos++;
					columns++;
				}
					break;

				case '{': {
					tokens.add(new Token(Kind.LBRACE, startPos, 1));
					pos++;
					columns++;
				}
					break;

				case '}': {
					tokens.add(new Token(Kind.RBRACE, startPos, 1));
					pos++;
					columns++;
				}
					break;
				// Separator end

				// Operator begin
				case '|': {
					state = State.GOT_BAR;
					pos++;
					columns++;
				}
					break;

				case '&': {
					tokens.add(new Token(Kind.AND, startPos, 1));
					pos++;
					columns++;
				}
					break;

				case '=': {
					state = State.AFTER_EQUAL;
					pos++;
					columns++;
				}
					break;

				case '!': {
					state = State.AFTER_NOT;
					pos++;
					columns++;
				}
					break;

				case '<': {
					state = State.AFTER_LT;
					pos++;
					columns++;
				}
					break;

				case '>': {
					state = State.AFTER_GT;
					pos++;
					columns++;
				}
					break;

				case '+': {
					tokens.add(new Token(Kind.PLUS, startPos, 1));
					pos++;
					columns++;
				}
					break;

				case '-': {
					state = State.AFTER_MINUS;
					pos++;
					columns++;
				}
					break;

				case '*': {
					tokens.add(new Token(Kind.TIMES, startPos, 1));
					pos++;
					columns++;
				}
					break;

				case '/': {
					state = State.AFTER_DIV;
					pos++;
					columns++;
				}
					break;

				case '%': {
					tokens.add(new Token(Kind.MOD, startPos, 1));
					pos++;
					columns++;
				}
					break;
				// Operator end

				// Digits begin
				case '0': {
					tokens.add(new Token(Kind.INT_LIT, startPos, 1));
					pos++;
					columns++;
				}
					break;

				// default evaluate if the char is 1-9 or letter
				default: {
					if (Character.isDigit(ch)) {
						state = State.IN_DIGIT;
						pos++;
						columns++;
					} else if (Character.isJavaIdentifierStart(ch)) {
						state = State.IN_IDENT;
						pos++;
						columns++;
					} else {
						throw new IllegalCharException(
								"illegal char: " + ch + ", at line " + lines + ", column " + columns);
					}
				} // default
					break;
				}// switch ch
			}
				break; // case START

			case AFTER_EQUAL: {
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case '=': {
					tokens.add(new Token(Kind.EQUAL, startPos, 2));
					pos++;
					columns++;
					state = State.START;
				}
					break;

				default: {
					throw new IllegalCharException("single equal, at line " + lines + ", column " + columns);
				} // default;
				}// switch ch
			}
				break; // case AFTER_EQUAL

			case GOT_BAR: {
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case '-': {
					state = State.AFTER_BAR;
					pos++;
					columns++;
				}
					break;
					
				default: {
					tokens.add(new Token(Kind.OR, startPos, 1));
					state = State.START;
					if (ch == '\n') {
						lines++;
						columns = 0;
					}
					
				}
					break;
				}// switch ch
			}
				break;// case GOT_BAR

			case AFTER_BAR: {
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case '>': {
					tokens.add(new Token(Kind.BARARROW, startPos, 3));
					pos++;
					columns++;
					state = State.START;
				}
					break;

				default: {
					/*throw new IllegalCharException("illegal operator: " + chars.substring(startPos, pos) + ", at line "
							+ lines + ", column " + columns);*/
					tokens.add(new Token(Kind.OR, startPos++, 1));
					tokens.add(new Token(Kind.MINUS, startPos++, 1));
					state = State.START;
				}

				}// switch ch
			}
				break;// case AFTER_BAR
			case AFTER_DIV: {
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case '*': {
					state = State.COMMENT;
					pos++;
					columns++;
				}
					break;
				default: {
					tokens.add(new Token(Kind.DIV, startPos, 1));
					state = State.START;
					if (ch == '\n') {
						lines++;
						columns = 0;
					}

					/*// if ch is an operator other than '*'
					if (ch == '|' || ch == '&' || ch == '=' || ch == '!' || ch == '<' || ch == '>' || ch == '+'
							|| ch == '/' || ch == '%' || ch == '-') {
						throw new IllegalCharException("illegal operator: " + chars.substring(startPos, pos)
								+ ", at line " + lines + ", column " + columns);
					}
					// if ch is not an operator, it is an '/'
					else {
						tokens.add(new Token(Kind.DIV, startPos, 1));
						state = State.START;
						if (ch == '\n') {
							lines++;
							columns = 0;
						}
					}*/
					
				}
					break;
				}// switch ch
			}
				break;// case AFTER_DIV

			case AFTER_GT: {
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case '=': {
					tokens.add(new Token(Kind.GE, startPos, 2));
					pos++;
					columns++;
					state = State.START;
				}
					break;

				default: {
					tokens.add(new Token(Kind.GT, startPos, 1));
					state = State.START;
					if (ch == '\n') {
						lines++;
						columns = 0;
					}
					
				}
					break;
				}// switch ch
			}
				break;// case AFTER_GT

			case AFTER_LT: {
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case '=': {
					tokens.add(new Token(Kind.LE, startPos, 2));
					pos++;
					columns++;
					state = State.START;
				}
					break;

				case '-': {
					tokens.add(new Token(Kind.ASSIGN, startPos, 2));
					pos++;
					columns++;
					state = State.START;
				}
					break;

				default: {
					tokens.add(new Token(Kind.LT, startPos, 1));
					state = State.START;
					if (ch == '\n') {
						lines++;
						columns = 0;
					}
					
				}
					break;
				}// switch ch
			}
				break;// case AFTER_LT

			case AFTER_MINUS: {
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case '>': {
					tokens.add(new Token(Kind.ARROW, startPos, 2));
					pos++;
					columns++;
					state = State.START;
				}
					break;

				default: {
					tokens.add(new Token(Kind.MINUS, startPos, 1));
					state = State.START;
					if (ch == '\n') {
						lines++;
						columns = 0;
					}
					
				}
					break;
				}// switch ch
			}
				break;// case AFTER_MINUS

			case AFTER_NOT: {
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case '=': {
					tokens.add(new Token(Kind.NOTEQUAL, startPos, 2));
					pos++;
					columns++;
					state = State.START;
				}
					break;

				default: {
					tokens.add(new Token(Kind.NOT, startPos, 1));
					state = State.START;
					if (ch == '\n') {
						lines++;
						columns = 0;
					}
					
				}
					break;
				}// switch ch
			}
				break;// case AFTER_NOT

			case IN_DIGIT: {
				ch = pos < length ? chars.charAt(pos) : -1;
				// ch is a digit, so the number is not end.
				if (Character.isDigit(ch)) {
					pos++;
					columns++;
				}
				// ch is not a digit, so the number is end, add this number into
				// tokens.
				else {
					Token t = new Token(Kind.INT_LIT, startPos, pos - startPos);
					t.intVal();
					tokens.add(t);
					state = State.START;
					if (ch == '\n') {
						pos++;
						lines++;
						columns = 0;
					}
				}
			}
				break;// case IN_DIGIT

			case IN_IDENT: {
				ch = pos < length ? chars.charAt(pos) : -1;
				// ch is still a char (or digit), so the number is not end.
				if (Character.isJavaIdentifierPart(ch)) {
					pos++;
					columns++;
				}
				// ch is not, then the identity is end
				else {
					// save the identity into temp, and evaluate if it is
					// keywords.
					String temp = chars.substring(startPos, pos);
					switch (temp) {
					case "integer": {
						tokens.add(new Token(Kind.KW_INTEGER, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "boolean": {
						tokens.add(new Token(Kind.KW_BOOLEAN, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "image": {
						tokens.add(new Token(Kind.KW_IMAGE, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "url": {
						tokens.add(new Token(Kind.KW_URL, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "file": {
						tokens.add(new Token(Kind.KW_FILE, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "frame": {
						tokens.add(new Token(Kind.KW_FRAME, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "while": {
						tokens.add(new Token(Kind.KW_WHILE, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "if": {
						tokens.add(new Token(Kind.KW_IF, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "sleep": {
						tokens.add(new Token(Kind.OP_SLEEP, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "screenheight": {
						tokens.add(new Token(Kind.KW_SCREENHEIGHT, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "screenwidth": {
						tokens.add(new Token(Kind.KW_SCREENWIDTH, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "gray": {
						tokens.add(new Token(Kind.OP_GRAY, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "convolve": {
						tokens.add(new Token(Kind.OP_CONVOLVE, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "blur": {
						tokens.add(new Token(Kind.OP_BLUR, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "scale": {
						tokens.add(new Token(Kind.KW_SCALE, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "width": {
						tokens.add(new Token(Kind.OP_WIDTH, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "height": {
						tokens.add(new Token(Kind.OP_HEIGHT, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "xloc": {
						tokens.add(new Token(Kind.KW_XLOC, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "yloc": {
						tokens.add(new Token(Kind.KW_YLOC, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "hide": {
						tokens.add(new Token(Kind.KW_HIDE, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "show": {
						tokens.add(new Token(Kind.KW_SHOW, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "move": {
						tokens.add(new Token(Kind.KW_MOVE, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "true": {
						tokens.add(new Token(Kind.KW_TRUE, startPos, pos - startPos));
						state = State.START;
					}
						break;

					case "false": {
						tokens.add(new Token(Kind.KW_FALSE, startPos, pos - startPos));				
						state = State.START;
					}
						break;

					default: {
						tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
						state = State.START;
						if (ch == '\n') {
							lines++;
							columns = 0;
						}
					}
						break;
					}// switch temp

				}
			}
				break;// case IN_IDENT

			case COMMENT: {
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case '*': {
					state = State.COMMENT_STAR;
					pos++;
					columns++;
				}
					break;

				case -1: {
					throw new IllegalCharException(
							"Lack of the end of comment at line " + lines + ", column " + columns);
				}

				default: {
					pos++;
					if (ch == '\n') {
						lines++;
						columns = 0;
					}
				}
					break;
				}
			}
				break;// case COMMENT

			case COMMENT_STAR: {
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case '/': {
					state = State.START;
					pos++;
					columns++;
				}
					break;

				default: {
					if (ch == '*') {
						pos++;
						columns++;
					} else {
						state = State.COMMENT;
						pos++;
						columns++;
						if (ch == '\n') {
							lines++;
							columns = 0;
						}
					}
				}
					break;
				}// switch ch
			}
				break;// case COMMENT_STAR

			default:
				assert false;
			}

		}
		// TODO IMPLEMENT THIS!!!!

		// Add the end icon of the file
//		tokens.add(new Token(Kind.EOF, pos, 0));

		return this;
	}

	private int skipWhiteSpace(int pos) {
		// TODO Auto-generated method stub
		
			while ((pos < chars.length()) && Character.isWhitespace(chars.charAt(pos))) {
				columns++;
				if (chars.charAt(pos++) == '\n') {
					lines++;
					columns = 0;
				}
			}
		
		return pos;
	}

	final ArrayList<Token> tokens;
	final String chars; // This is the input String.
	int tokenNum;
	int lines;
	int columns;

	/*
	 * Return the next token in the token list and update the state so that the
	 * next call will return the Token..
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}

	/*
	 *Return the last token in the token list and update the state so that the
	 *next call will return the Token.. 
	 */
	public void lastToken() {
		/*if (tokenNum >= tokens.size())
			return null;*/
		if (tokenNum < 0)
			return;
		tokenNum = tokenNum - 2;
		return;
	}
	
	/*
	 * Return the next token in the token list without updating the state. (So
	 * the following call to next will return the same token.)
	 */
	public Token peek() {
		if (tokenNum >= tokens.size())
			return null;
		// return tokens.get(tokenNum+1); //original code
		return tokens.get(tokenNum); // correction from canvas
	}

	/**
	 * Returns a LinePos object containing the line and position in line of the
	 * given token.
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		// TODO IMPLEMENT THIS
		//t.lines = lines;
		//t.columns = columns - t.length + 1;
		LinePos linePos = new LinePos(-1, -1);
		if (t != null) {
			linePos = t.getLinePos();
		}
		return linePos;
	}

}
