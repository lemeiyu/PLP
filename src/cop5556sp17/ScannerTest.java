package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;

public class ScannerTest {

	@Rule
    public ExpectedException thrown = ExpectedException.none();


	
	@Test
	public void testEmpty() throws IllegalCharException, IllegalNumberException {
		String input = "";
		Scanner scanner = new Scanner(input);
		scanner.scan();
	}

	@Test
	public void testSemiConcat() throws IllegalCharException, IllegalNumberException {
		//input string
		String input = ";;;";
		//create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		//get the first token and check its kind, position, and contents
		Scanner.Token token = scanner.nextToken();
		assertEquals(SEMI, token.kind);
		assertEquals(0, token.pos);
		String text = SEMI.getText();
		assertEquals(text.length(), token.length);
		assertEquals(text, token.getText());
		//get the next token and check its kind, position, and contents
		Scanner.Token token1 = scanner.nextToken();
		assertEquals(SEMI, token1.kind);
		assertEquals(1, token1.pos);
		assertEquals(text.length(), token1.length);
		assertEquals(text, token1.getText());
		Scanner.Token token2 = scanner.nextToken();
		assertEquals(SEMI, token2.kind);
		assertEquals(2, token2.pos);
		assertEquals(text.length(), token2.length);
		assertEquals(text, token2.getText());
		//check that the scanner has inserted an EOF token at the end
		Scanner.Token token3 = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF,token3.kind);
	}
	
	
	/**
	 * This test illustrates how to check that the Scanner detects errors properly. 
	 * In this test, the input contains an int literal with a value that exceeds the range of an int.
	 * The scanner should detect this and throw and IllegalNumberException.
	 * 
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	
	@Test
	public void testIntOverflowError() throws IllegalCharException, IllegalNumberException{
		String input = "99999999999999999";
		Scanner scanner = new Scanner(input);
		thrown.expect(IllegalNumberException.class);
		scanner.scan();
	}

//TODO  more tests
	@Test
	public void testMultilineInput() throws IllegalCharException, IllegalNumberException {
		//input string
		String input = "integer i1<- 233;\n move i1 -> xloc 3333;";
		//create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		//get the first token and check its kind, position, and contents
		Scanner.Token token = scanner.nextToken();
		assertEquals(KW_INTEGER, token.kind);
		assertEquals(0, token.pos);
		String text = KW_INTEGER.getText();
		assertEquals(text.length(), token.length);
		assertEquals(text, token.getText());
		//get the next token and check its kind, position, and contents
		Scanner.Token token1 = scanner.nextToken();
		assertEquals(IDENT, token1.kind);
		//assertEquals(1, token1.pos);
		text = "i1";
		assertEquals(text.length(), token1.length);
		assertEquals(text, token1.getText());
		
		Scanner.Token token2 = scanner.nextToken();
		assertEquals(ASSIGN, token2.kind);
		//assertEquals(2, token2.pos);
		text = ASSIGN.getText();
		assertEquals(text.length(), token2.length);
		assertEquals(text, token2.getText());
		
		Scanner.Token token3 = scanner.nextToken();
		assertEquals(INT_LIT, token3.kind);
		//assertEquals(3, token3.pos);
		text = "233";
		assertEquals(text.length(), token3.length);
		assertEquals(text, token3.getText());
		assertEquals(233, token3.intVal());
		
		Scanner.Token token4 = scanner.nextToken();
		assertEquals(SEMI, token4.kind);
		//assertEquals(4, token4.pos);
		text = SEMI.getText();
		assertEquals(text.length(), token4.length);
		assertEquals(text, token4.getText());
		
		Scanner.Token token5 = scanner.nextToken();
		assertEquals(KW_MOVE, token5.kind);
		//assertEquals(5, token5.pos);
		text = KW_MOVE.getText();
		assertEquals(text.length(), token5.length);
		assertEquals(text, token5.getText());
		assertEquals(token5.getLinePos().line, 1);
		assertEquals(token5.getLinePos().posInLine, 1);
		
		Scanner.Token token6 = scanner.nextToken();
		assertEquals(IDENT, token6.kind);
		//assertEquals(6, token6.pos);
		text = "i1";
		assertEquals(text.length(), token6.length);
		assertEquals(text, token6.getText());
		
		Scanner.Token token7 = scanner.nextToken();
		assertEquals(ARROW, token7.kind);
		//assertEquals(7, token7.pos);
		text = ARROW.getText();
		assertEquals(text.length(), token7.length);
		assertEquals(text, token7.getText());
		
		Scanner.Token token8 = scanner.nextToken();
		assertEquals(KW_XLOC, token8.kind);
		//assertEquals(8, token8.pos);
		text = KW_XLOC.getText();
		assertEquals(text.length(), token8.length);
		assertEquals(text, token8.getText());
		
		Scanner.Token token9 = scanner.nextToken();
		assertEquals(INT_LIT, token9.kind);
		//assertEquals(9, token9.pos);
		text = "3333";
		assertEquals(text.length(), token9.length);
		assertEquals(text, token9.getText());
		assertEquals(3333, token9.intVal());
		
		Scanner.Token token10 = scanner.nextToken();
		assertEquals(SEMI, token10.kind);
		//assertEquals(10, token10.pos);
		text = SEMI.getText();
		assertEquals(text.length(), token10.length);
		assertEquals(text, token10.getText());
		
		//check that the scanner has inserted an EOF token at the end
		Scanner.Token token11 = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF,token11.kind);
	}
	
}