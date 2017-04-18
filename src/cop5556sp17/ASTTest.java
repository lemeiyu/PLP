package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.AST.*;

public class ASTTest {

	static final boolean doPrint = true;
	static void show(Object s){
		if(doPrint){System.out.println(s);}
	}
	

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IdentExpression.class, ast.getClass());
	}

	@Test
	public void testFactor1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "123";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IntLitExpression.class, ast.getClass());
	}



	@Test
	public void testBinaryExpr0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "1+abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(IdentExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
	}
	
	@Test
	public void testChain0() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "fsb->blur (sb250)|->gray (3862250)->convolve (38+62)->show (sb250)->hide (sb62)->move (wwwdsbcom);\n";
		Parser parser = new Parser(new Scanner(input).scan());
		ASTNode ast = parser.statement();
		assertEquals(BinaryChain.class, ast.getClass());
	}
	
	@Test
	public void testChain1() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "xloc (2+8)->yloc (3+2)->width (100)->height (250)->scale (sb38+sb62, dsb75)->blur ;\n";
		Parser parser = new Parser(new Scanner(input).scan());
		ASTNode ast = parser.statement();
		assertEquals(BinaryChain.class, ast.getClass());
	}
	
	@Test
	 public void testBinaryChain() throws IllegalCharException, IllegalNumberException, SyntaxException {
	  String input = "zyn -> zyn;";
	  Scanner scanner = new Scanner(input);
	  scanner.scan();
	  Parser parser = new Parser(scanner);
	  ASTNode ast = parser.statement();
	  assertEquals(BinaryChain.class, ast.getClass());
	  BinaryChain bc = (BinaryChain) ast;
	  assertEquals(IdentChain.class, bc.getE0().getClass());
	  assertEquals(IdentChain.class, bc.getE1().getClass());
	  assertEquals(ARROW, bc.getArrow().kind);
	 }


	/* @Test
	 public void testFilterOpChain() throws IllegalCharException, IllegalNumberException, SyntaxException {
	  String input = "blur (zyn ==1);";
	  Scanner scanner = new Scanner(input);
	  scanner.scan();
	  Parser parser = new Parser(scanner);
	  ASTNode ast = parser.statement();
	  assertEquals(FilterOpChain.class, ast.getClass());
	  FilterOpChain fc = (FilterOpChain) ast;
	  System.out.println(fc.toString());

	 }*/

	 @Test
	 public void testWhileStatement() throws IllegalCharException, IllegalNumberException, SyntaxException {
	  String input = "while(zyn ==1 ){ zyn <- 1+2;}";
	  Scanner scanner = new Scanner(input);
	  scanner.scan();
	  Parser parser = new Parser(scanner);
	  ASTNode ast = parser.statement();
	  assertEquals(WhileStatement.class, ast.getClass());
	  WhileStatement ws = (WhileStatement) ast;
	  assertEquals(BinaryExpression.class, ws.getE().getClass());
	  assertEquals(Block.class, ws.getB().getClass());
	  System.out.println(ws.toString());

	 }
	 
	 @Test
		public void testMakeABigNews() throws IllegalCharException, IllegalNumberException, SyntaxException{
			String input = "sb00 url wwwdsbcom, file fsb, integer sb250, boolean t1{ \n"
					+ "integer sb62 boolean t2 image isb frame sb38\n"
					+ "sleep sb250+sb62; while (t1) {if(t2){sleep sb62 == sb250*2;}}\n"
					+ "fsb->blur (sb250)|->gray (3862250);\n"
					+ "xloc (2+8)->yloc (3+2)->width (100)->blur ;\n"
					+ "fsb <- 3862250+62;}";
			Parser parser = new Parser(new Scanner(input).scan());
			ASTNode ast = parser.parse();
			assertEquals(Program.class, ast.getClass());
		}

}
