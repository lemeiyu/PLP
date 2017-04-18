package cop5556sp17;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Program;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;


public class ParserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.factor();
	}

	@Test
	public void testArg() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,5) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		//System.out.println(scanner);
		Parser parser = new Parser(scanner);
        parser.arg();
	}

	@Test
	public void testArgerror() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		thrown.expect(Parser.SyntaxException.class);
		parser.arg();
	}


	@Test
	public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "prog0 {}";
		Parser parser = new Parser(new Scanner(input).scan());
		ASTNode astNode = parser.parse();
	}

	@Test
	public void testExpression0() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "3+7-8*9-(2+3)";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.expression();
	}
	
	@Test
	public void testStatementChain0() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "hz2 |-> blur (3+2) -> move (hz1-3) -> hz3;";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.statement();
	}
	
	@Test
	public void testStatementAssign0() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "hz2 <- 3+2;\n hz4 -> move (hz1-3) -> hz3;";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.statement();
	}
	
	@Test
	public void testMakeABigNews() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "sb00 url wwwdsbcom, file fsb, integer sb250, boolean t1{ \n"
				+ "integer sb62 boolean t2 image isb frame sb38\n"
				+ "sleep sb250+sb62; while (t1) {if(t2){sleep sb62 == sb250*2;}}\n"
				+ "fsb->blur (sb250)|->gray (3862250)->convolve (38+62)->show (sb250)->hide (sb62)->move (wwwdsbcom);\n"
				+ "xloc (2+8)->yloc (3+2)->width (100)->height (250)->scale (sb38+sb62, dsb75)->blur ;\n"
				+ "fsb <- 3862250+62;}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}
	
	@Test
	public void testExpression1() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "w372sdf < 99 | h7== 53 |iuh-2!=    78%(66/3+5)-7|screenwidth>=5| screenheight<=2 <false>true";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.expression();
	}
	
	@Test
	public void testExpression2() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "ss37d|--3";
		Parser parser = new Parser(new Scanner(input).scan());
		thrown.expect(Parser.SyntaxException.class);
		parser.expression();
	}
	
	@Test
	public void testMissingKWBlockStatement() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "{a+b;}";
		Parser parser = new Parser(new Scanner(input).scan());
		thrown.expect(Parser.SyntaxException.class);
		parser.block();
	}
	
}
