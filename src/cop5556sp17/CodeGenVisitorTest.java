
package cop5556sp17;

import java.io.FileOutputStream;
import java.io.OutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Program;

public class CodeGenVisitorTest {

	static final boolean doPrint = true;

	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}

	boolean devel = false;
	boolean grade = false;

	@Test
	public void emptyProg() throws Exception {
		// scan, parse, and type check the program

		/*
		 * String progname = "LQTest"; String input = progname +
		 * " integer int1,integer int2,boolean bool1,boolean bool2,file file1,file file2,file outputFile1,url url1,url url2{\n"
		 * + "integer int3\n" + "integer int4\n" + "integer int5\n" +
		 * "integer int6\n" + "boolean bool3\n" + "boolean bool4\n" +
		 * "image image1\n" + "image image2\n" + "image image3\n" +
		 * "image image4\n" + "image image5\n" + "frame frame1\n" +
		 * "frame frame2\n" + "frame frame3\n" + "frame frame4\n" +
		 * "frame frame5\n"
		 * +"file1->image3->image4;\n"+"if(image3 == image4){}\n"+"}\n";
		 */

		String input = "subImage url u {image i image j image k frame f \n"
				+ "u -> i; \n"
				+ "u -> j; \n "
				+ "k <- i-j; k -> f -> show;\n}";

		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);

		show(program);

		// generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel, grade, null);
		byte[] bytecode = (byte[]) program.visit(cv, null);

		// output the generated bytecode
		CodeGenUtils.dumpBytecode(bytecode);

		// write byte code to file
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName);

		// directly execute bytecode
		String[] args = new String[9]; // create command line argument array to
										// initialize params, none in this case
		/*args[0] = "50";
		args[1] = "100";
		args[2] = "true";
		args[3] = "false";
		args[4] = "Images/image1.jpg";
		args[5] = "Images/image2.jpg";
		args[6] = "Images/output1.jpg";
		args[7] = "https://s3.amazonaws.com/glocal-files/image/bi+(65).jpg";
		args[8] = "https://s3.amazonaws.com/glocal-files/image/bi+(100).jpg";*/
		
		args[0] = "https://s3.amazonaws.com/glocal-files/image/bi+(65).jpg";
		//args[1] = "false";
		
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
	}

}