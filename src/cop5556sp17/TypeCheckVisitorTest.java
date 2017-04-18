/**  Important to test the error cases in case the
 * AST is not being completely traversed.
 * 
 * Only need to test syntactically correct programs, or
 * program fragments.
 */

package cop5556sp17;

/*import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;*/
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;
/*import cop5556sp17.AST.Dec;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.Statement;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;*/

public class TypeCheckVisitorTest {
	

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testAssignmentBoolLit0() throws Exception{
		String input = "p {\nboolean y \ny <- false;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);		
	}

	@Test
	public void testAssignmentBoolLitError0() throws Exception{
		String input = "p {\nboolean y \ny <- 3;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		program.visit(v, null);		
	}		
	
	@Test
	public void testMakeABigNews() throws Exception {
		String input = "sb00 url wwwdsbcom, file fsb, integer sb250, boolean t1{ \n"
				+ "integer sb62 \n"
				+ "boolean t2 \n"
				+ "image isb \n"
				+ "frame sb38\n"
				+ "sleep sb250+sb62;\n"
				+ " while (t1) \n"
				+ "{\n"
				+ "if(t2)\n"
				+ "{\n"
				+ "sleep sb62+sb250*2;\n"
				+ "}\n"
				+ "}\n"
				+ "fsb->blur |->gray;\n"
				+ "sb38 ->yloc; \n"
				+ "isb->blur->scale (sb62+1)|-> gray;\n"
				+ "sb62 <- 3862250+62;}";
		Parser parser = new Parser(new Scanner(input).scan());
		ASTNode ast = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		ast.visit(v, null);	
	}
}
