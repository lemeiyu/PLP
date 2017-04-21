package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.*;
import cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		// symbolTable = new SymbolTable();
		slotNum = 1;
		slotStack = new Stack<Integer>();
		// slotStack.push(1);
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	// SymbolTable symbolTable;
	int slotNum;
	Stack<Integer> slotStack;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// ArrayList<ParamDec> params = program.getParams();
		/*
		 * int i = 0; for (ParamDec dec : program.getParams()) {
		 * dec.setSlotNum(i++); cw.visitField(0, dec.getIdent().getText(),
		 * dec.getTypeName().getJVMTypeDesc(), null, null); }
		 */
		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);

		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		// ArrayList<ParamDec> params = program.getParams();
		int i = 0;
		for (ParamDec dec : program.getParams()) {
			dec.setSlotNum(i++);
			cw.visitField(0, dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc(), null, null);
			dec.visit(this, mv);
		}
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		// TODO visit the local variables
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method

		cw.visitEnd();// end of class

		// generate classfile and return it
		return cw.toByteArray();
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		binaryChain.getE0().visit(this, 0);
		if (binaryChain.getArrow().isKind(Kind.BARARROW)) {
			// IdentChain identChain = (IdentChain) binaryChain.getE0();

			// mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNum());
			mv.visitInsn(DUP);

		} else {
			if (binaryChain.getE0().getTypeName() == URL) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL",
						PLPRuntimeImageIO.readFromURLSig, false);

				/*
				 * Debug
				 */

				System.out.println("INVOKESTATIC " + PLPRuntimeImageIO.className + " readFromURL "
						+ PLPRuntimeImageIO.readFromURLSig);

			} else if (binaryChain.getE0().getTypeName() == FILE) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile",
						PLPRuntimeImageIO.readFromFileDesc, false);

				/*
				 * Debug
				 */

				System.out.println("INVOKESTATIC " + PLPRuntimeImageIO.className + " readFromFile "
						+ PLPRuntimeImageIO.readFromFileDesc);

			}
		}

		if (binaryChain.getArrow().isKind(Kind.BARARROW)) {
			binaryChain.getE1().visit(this, 3);
		} else {
			binaryChain.getE1().visit(this, 1);
		}
		if (binaryChain.getE1() instanceof IdentChain) {
			IdentChain identChain = (IdentChain) binaryChain.getE1();
			if ((identChain.getDec() instanceof ParamDec)) {
				if (identChain.getDec().getTypeName() == TypeName.INTEGER) {
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
							identChain.getDec().getTypeName().getJVMTypeDesc());
				}

				/*
				 * Debug
				 */

				System.out.println("ALOAD 0");
				System.out.println("GETFIELD " + className + identChain.getDec().getIdent().getText()
						+ identChain.getDec().getTypeName().getJVMTypeDesc());

			} else {
				if (identChain.getDec().getTypeName() == TypeName.INTEGER) {
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlotNum());
				} else {
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNum());
				}

				/*
				 * Debug
				 */

				System.out.println("ALOAD " + identChain.getDec().getSlotNum());

			}
		}

		// binaryChain.getE1().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Implement this
		TypeName t0 = binaryExpression.getE0().getTypeName();
		TypeName t1 = binaryExpression.getE1().getTypeName();
		Token op = binaryExpression.getOp();
		switch (op.getKind()) {
		case PLUS:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if (t0 == TypeName.INTEGER) {
				mv.visitInsn(IADD);
				/*
				 * Debug
				 */
				System.out.println("IADD");

			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);

				/*
				 * Debug
				 */

				System.out.println("INVOKESTATIC " + PLPRuntimeImageOps.JVMName + " add " + PLPRuntimeImageOps.addSig);

			}
			break;

		case MINUS:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if (t0 == TypeName.INTEGER) {
				mv.visitInsn(ISUB);
				/*
				 * Debug
				 */
				System.out.println("ISUB");
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
				/*
				 * Debug
				 */

				System.out.println("INVOKESTATIC " + PLPRuntimeImageOps.JVMName + " sub " + PLPRuntimeImageOps.subSig);

			}
			break;

		case TIMES:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if ((t0 == TypeName.INTEGER) && (t1 == TypeName.INTEGER)) {
				mv.visitInsn(IMUL);
				/*
				 * Debug
				 */
				System.out.println("IMUL");

			} else if ((t0 == TypeName.INTEGER) && (t1 == TypeName.IMAGE)) {
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
				/*
				 * Debug
				 */

				System.out.println("SWAP");
				System.out.println("INVOKESTATIC " + PLPRuntimeImageOps.JVMName + " mul " + PLPRuntimeImageOps.mulSig);

			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
				/*
				 * Debug
				 */

				System.out.println("INVOKESTATIC " + PLPRuntimeImageOps.JVMName + " mul " + PLPRuntimeImageOps.mulSig);

			}
			break;

		case DIV:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if ((t0 == TypeName.INTEGER) && (t1 == TypeName.INTEGER)) {
				mv.visitInsn(IDIV);
				/*
				 * Debug
				 */
				System.out.println("IDIV");
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
				/*
				 * Debug
				 */

				System.out.println("INVOKESTATIC " + PLPRuntimeImageOps.JVMName + " div " + PLPRuntimeImageOps.divSig);

			}
			break;

		case MOD:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if ((t0 == TypeName.INTEGER) && (t1 == TypeName.INTEGER)) {
				mv.visitInsn(IREM);
				/*
				 * Debug
				 */
				System.out.println("IREM");
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
				/*
				 * Debug
				 */

				System.out.println("INVOKESTATIC " + PLPRuntimeImageOps.JVMName + " mod " + PLPRuntimeImageOps.modSig);

			}
			break;

		case OR:
			binaryExpression.getE0().visit(this, arg);
			Label or_l1 = new Label();
			mv.visitJumpInsn(IFNE, or_l1);
			binaryExpression.getE1().visit(this, arg);
			mv.visitJumpInsn(IFNE, or_l1);
			mv.visitInsn(ICONST_0);
			Label or_l2 = new Label();
			mv.visitJumpInsn(GOTO, or_l2);
			mv.visitLabel(or_l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(or_l2);
			// mv.visitInsn(ISTORE);
			break;

		case AND:
			binaryExpression.getE0().visit(this, arg);
			Label and_l1 = new Label();
			mv.visitJumpInsn(IFEQ, and_l1);
			binaryExpression.getE1().visit(this, arg);
			mv.visitJumpInsn(IFEQ, and_l1);
			mv.visitInsn(ICONST_1);
			Label and_l2 = new Label();
			mv.visitJumpInsn(GOTO, and_l2);
			mv.visitLabel(and_l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(and_l2);
			break;

		case LT:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			Label lt_l1 = new Label();
			mv.visitJumpInsn(IF_ICMPGE, lt_l1);
			mv.visitInsn(ICONST_1);
			Label lt_l2 = new Label();
			mv.visitJumpInsn(GOTO, lt_l2);
			mv.visitLabel(lt_l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(lt_l2);
			break;

		case LE:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			Label le_l1 = new Label();
			mv.visitJumpInsn(IF_ICMPGT, le_l1);
			mv.visitInsn(ICONST_1);
			Label le_l2 = new Label();
			mv.visitJumpInsn(GOTO, le_l2);
			mv.visitLabel(le_l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(le_l2);
			break;

		case GT:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			Label gt_l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLE, gt_l1);
			mv.visitInsn(ICONST_1);
			Label gt_l2 = new Label();
			mv.visitJumpInsn(GOTO, gt_l2);
			mv.visitLabel(gt_l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(gt_l2);
			break;

		case GE:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			Label ge_l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLT, ge_l1);
			mv.visitInsn(ICONST_1);
			Label ge_l2 = new Label();
			mv.visitJumpInsn(GOTO, ge_l2);
			mv.visitLabel(ge_l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(ge_l2);
			break;

		case EQUAL:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if (t0 == TypeName.INTEGER || t0 == BOOLEAN) {
				Label equal_l1 = new Label();
				mv.visitJumpInsn(IF_ICMPNE, equal_l1);
				mv.visitInsn(ICONST_1);
				Label equal_l2 = new Label();
				mv.visitJumpInsn(GOTO, equal_l2);
				mv.visitLabel(equal_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(equal_l2);
			} else {
				Label equal_l1 = new Label();
				mv.visitJumpInsn(IF_ACMPNE, equal_l1);
				mv.visitInsn(ICONST_1);
				Label equal_l2 = new Label();
				mv.visitJumpInsn(GOTO, equal_l2);
				mv.visitLabel(equal_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(equal_l2);
			}

			break;

		case NOTEQUAL:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if (t0 == TypeName.INTEGER || t0 == BOOLEAN) {
				Label notequal_l1 = new Label();
				mv.visitJumpInsn(IF_ICMPEQ, notequal_l1);
				mv.visitInsn(ICONST_1);
				Label notequal_l2 = new Label();
				mv.visitJumpInsn(GOTO, notequal_l2);
				mv.visitLabel(notequal_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(notequal_l2);
			} else {
				Label notequal_l1 = new Label();
				mv.visitJumpInsn(IF_ACMPEQ, notequal_l1);
				mv.visitInsn(ICONST_1);
				Label notequal_l2 = new Label();
				mv.visitJumpInsn(GOTO, notequal_l2);
				mv.visitLabel(notequal_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(notequal_l2);
			}
			break;

		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Implement this
		// symbolTable.enterScope();
		Label blockstart = new Label();
		mv.visitLineNumber(block.getFirstToken().getLinePos().line, blockstart);
		mv.visitLabel(blockstart);
		// ArrayList<Dec> decs = block.getDecs();
		// int i = 0;
		for (Dec dec : block.getDecs()) {
			// dec.setSlotNum(slotNum++);
			dec.visit(this, mv);
		}
		// ArrayList<Statement> statements = block.getStatements();
		for (Statement statement : block.getStatements()) {
			statement.visit(this, mv);
			if (statement instanceof BinaryChain) {
				mv.visitInsn(POP);
				// Debug
				System.out.println("POP");
			}
		}
		// symbolTable.leaveScope();
		Label blockend = new Label();
		mv.visitLineNumber(0, blockend);
		mv.visitLabel(blockend);
		for (Dec dec : block.getDecs()) {
			mv.visitLocalVariable(dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc(), null, blockstart,
					blockend, dec.getSlotNum());
			slotNum--;
			slotStack.pop();
		}
		/*
		 * for (int i = 0; i < block.getDecs().size(); i++) { slotStack.pop();
		 * slotNum--; }
		 */
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Implement this
		if (booleanLitExpression.getValue()) {
			mv.visitInsn(ICONST_1);
		} else {
			mv.visitInsn(ICONST_0);
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		if (constantExpression.getFirstToken().isKind(Kind.KW_SCREENHEIGHT)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight",
					PLPRuntimeFrame.getScreenHeightSig, false);
		} else if (constantExpression.getFirstToken().isKind(Kind.KW_SCREENWIDTH)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth",
					PLPRuntimeFrame.getScreenWidthSig, false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Implement this
		slotStack.push(slotNum++);
		declaration.setSlotNum(slotStack.peek());
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		switch (filterOpChain.getFirstToken().getKind()) {
		case OP_BLUR:
			// since source != dest, dup or create new image.
			// mv.visitInsn(DUP);
			// load null so the method will create a new image.
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
			/*
			 * Debug
			 */

			System.out.println("ACONST_NULL");
			System.out.println("INVOKESTATIC " + PLPRuntimeFilterOps.JVMName + " blurOp " + PLPRuntimeFilterOps.opSig);

			break;

		case OP_GRAY:
			if ((int) arg != 3) {
				mv.visitInsn(ACONST_NULL);
			}
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
			/*
			 * Debug
			 */

			System.out.println("ACONST_NULL");
			System.out.println("INVOKESTATIC " + PLPRuntimeFilterOps.JVMName + " grayOp " + PLPRuntimeFilterOps.opSig);

			break;

		case OP_CONVOLVE:
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig,
					false);
			/*
			 * Debug
			 */

			System.out.println("ACONST_NULL");
			System.out.println(
					"INVOKESTATIC " + PLPRuntimeFilterOps.JVMName + " convolveOp " + PLPRuntimeFilterOps.opSig);

			break;

		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, arg);
		switch (frameOpChain.getFirstToken().getKind()) {
		case KW_SHOW:
			// mv.visitInsn(ACONST_NULL);
			// mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame",
			// "createOrSetFrame",
			// "(Ljava/awt/image/BufferedImage;Lcop5556sp17/PLPRuntimeFrame;)Lcop5556sp17/PLPRuntimeFrame;",
			// false);
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc,
					false);
			// mv.visitInsn(POP);

			/*
			 * Debug
			 */
			// System.out.println("ACONST_NULL");

			System.out.println(
					"INVOKEVIRTUAL " + PLPRuntimeFrame.JVMClassName + " showImage " + PLPRuntimeFrame.showImageDesc);

			break;

		case KW_HIDE:
			// mv.visitInsn(ACONST_NULL);
			// mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame",
			// "createOrSetFrame",
			// "(Ljava/awt/image/BufferedImage;Lcop5556sp17/PLPRuntimeFrame;)Lcop5556sp17/PLPRuntimeFrame;",
			// false);
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc,
					false);
			// mv.visitInsn(POP);

			/*
			 * Debug
			 */
			// System.out.println("ACONST_NULL");

			System.out.println(
					"INVOKEVIRTUAL " + PLPRuntimeFrame.JVMClassName + " hideImage " + PLPRuntimeFrame.hideImageDesc);

			break;

		case KW_MOVE:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc,
					false);
			// mv.visitInsn(POP);

			/*
			 * Debug
			 */
			// System.out.println("ACONST_NULL");

			System.out.println(
					"INVOKEVIRTUAL " + PLPRuntimeFrame.JVMClassName + " moveFrame " + PLPRuntimeFrame.moveFrameDesc);

			break;

		case KW_XLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc,
					false);

			/*
			 * Debug
			 */
			// System.out.println("ACONST_NULL");

			System.out.println(
					"INVOKEVIRTUAL " + PLPRuntimeFrame.JVMClassName + " getXVal " + PLPRuntimeFrame.getXValDesc);

			break;

		case KW_YLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc,
					false);
			/*
			 * Debug
			 */
			// System.out.println("ACONST_NULL");

			System.out.println(
					"INVOKEVIRTUAL " + PLPRuntimeFrame.JVMClassName + " getYVal " + PLPRuntimeFrame.getYValDesc);

			break;

		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// define true if right, false is left.
		Boolean side = (int) arg == 1;
		if (side) {
			// right side operation
			if (identChain.getDec() instanceof ParamDec) {
				// file, url, integer, boolean
				switch (identChain.getDec().getTypeName()) {
				case INTEGER:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getDec().getIdent().getText(),
							identChain.getDec().getTypeName().getJVMTypeDesc());
					identChain.getDec().setInitialized(true);

					/*
					 * Debug
					 */

					System.out.println("ALOAD 0");
					System.out.println("SWAP");
					System.out.println("PUTFIELD " + className + identChain.getDec().getIdent().getText()
							+ identChain.getDec().getTypeName().getJVMTypeDesc());

					break;

				case FILE:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
							identChain.getDec().getTypeName().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
							PLPRuntimeImageIO.writeImageDesc, false);
					identChain.getDec().setInitialized(true);
					// mv.visitInsn(POP);

					/*
					 * Debug
					 */

					System.out.println("ALOAD 0");
					System.out.println("GETFIELD " + className + identChain.getDec().getIdent().getText()
							+ identChain.getDec().getTypeName().getJVMTypeDesc());
					System.out.println(
							"INVOKESTATIC " + PLPRuntimeImageIO.className + "write" + PLPRuntimeImageIO.writeImageDesc);

					break;

				default:
					break;
				}
			} else {
				switch (identChain.getDec().getTypeName()) {
				case INTEGER:
					mv.visitVarInsn(ISTORE, identChain.getDec().getSlotNum());
					identChain.getDec().setInitialized(true);
					/*
					 * Debug
					 */

					System.out.println("ISTORE " + identChain.getDec().getSlotNum());

					break;

				case IMAGE:
					mv.visitVarInsn(ASTORE, identChain.getDec().getSlotNum());
					identChain.getDec().setInitialized(true);
					/*
					 * Debug
					 */

					System.out.println("ASTORE " + identChain.getDec().getSlotNum());

					break;

				case FILE:
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNum());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
							PLPRuntimeImageIO.writeImageDesc, false);
					identChain.getDec().setInitialized(true);
					// mv.visitInsn(POP);
					/*
					 * Debug
					 */

					System.out.println("ALOAD " + identChain.getDec().getSlotNum());
					System.out.println("INVOKESTATIC " + PLPRuntimeImageIO.className + " write "
							+ PLPRuntimeImageIO.writeImageDesc);

					break;

				case FRAME:
					if (identChain.getDec().getInitialized()) {
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNum());
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
								PLPRuntimeFrame.createOrSetFrameSig, false);
						mv.visitVarInsn(ASTORE, identChain.getDec().getSlotNum());

						System.out.println("ALOAD " + identChain.getDec().getSlotNum());
						System.out.println("INVOKESTATIC " + PLPRuntimeFrame.JVMClassName + " createOrSetFrame"
								+ PLPRuntimeFrame.createOrSetFrameSig);
						System.out.println("ASTORE "+identChain.getDec().getSlotNum());
					} else {
						mv.visitInsn(ACONST_NULL);
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
								PLPRuntimeFrame.createOrSetFrameSig, false);
						mv.visitVarInsn(ASTORE, identChain.getDec().getSlotNum());
						identChain.getDec().setInitialized(true);

						System.out.println("ACONST_NULL");
						System.out.println("ALOAD " + identChain.getDec().getSlotNum());
						System.out.println("INVOKESTATIC " + PLPRuntimeFrame.JVMClassName + " createOrSetFrame"
								+ PLPRuntimeFrame.createOrSetFrameSig);
						System.out.println("ASTORE "+identChain.getDec().getSlotNum());
					}

					// mv.visitInsn(POP);

					/*
					 * Debug
					 */
					System.out.println("POP");

					break;

				default:
					break;
				}
			}
		} else {
			// left side operation
			if (identChain.getDec() instanceof ParamDec) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
						identChain.getDec().getTypeName().getJVMTypeDesc());

				/*
				 * Debug
				 */

				System.out.println("ALOAD 0");
				System.out.println("GETFIELD " + className + identChain.getDec().getIdent().getText()
						+ identChain.getDec().getTypeName().getJVMTypeDesc());

			} else {
				if (identChain.getDec().getTypeName() == FRAME) {
					if (identChain.getDec().getInitialized()) {

						mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNum());
						/*
						 * Debug
						 */

						System.out.println("ALOAD " + identChain.getDec().getSlotNum());

					} else {
						mv.visitInsn(ACONST_NULL);
						/*
						 * Debug
						 */
						System.out.println("ACONST_NULL");
					}

				} else {
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNum());
					/*
					 * Debug
					 */

					System.out.println("ALOAD " + identChain.getDec().getSlotNum());

				}

			}

		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Implement this
		if (identExpression.getDec() instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);
			/*
			 * String type = new String(); switch
			 * (identExpression.getDec().getTypeName()) { case INTEGER: type =
			 * "I"; break;
			 * 
			 * case BOOLEAN: type = "Z"; break;
			 * 
			 * default: break; }
			 */
			mv.visitFieldInsn(GETFIELD, className, identExpression.getDec().getIdent().getText(),
					identExpression.getDec().getTypeName().getJVMTypeDesc());
		} else {
			if (identExpression.getTypeName() == TypeName.INTEGER
					|| identExpression.getTypeName() == TypeName.BOOLEAN) {
				mv.visitVarInsn(ILOAD, identExpression.getDec().getSlotNum());
			} else {
				mv.visitVarInsn(ALOAD, identExpression.getDec().getSlotNum());
			}

		}

		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Implement this
		if (identX.getDec() instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			// String type = new String();
			/*
			 * switch (identX.getDec().getTypeName()) { case INTEGER: type =
			 * "I"; break;
			 * 
			 * case BOOLEAN: type = "Z"; break;
			 * 
			 * default: break; }
			 */
			mv.visitFieldInsn(PUTFIELD, className, identX.getDec().getIdent().getText(),
					identX.getDec().getTypeName().getJVMTypeDesc());
		} else {
			if (identX.getDec().getTypeName() == IMAGE) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",
						PLPRuntimeImageOps.copyImageSig, false);
				mv.visitVarInsn(ASTORE, identX.getDec().getSlotNum());
				identX.getDec().setInitialized(true);
			} else if (identX.getDec().getTypeName() == TypeName.INTEGER
					|| identX.getDec().getTypeName() == TypeName.BOOLEAN) {
				mv.visitVarInsn(ISTORE, identX.getDec().getSlotNum());
				identX.getDec().setInitialized(true);
			} else {
				mv.visitVarInsn(ASTORE, identX.getDec().getSlotNum());
				identX.getDec().setInitialized(true);
			}
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Implement this

		System.out.println("if begin");

		ifStatement.getE().visit(this, arg);
		Label if_false = new Label();
		mv.visitJumpInsn(IFEQ, if_false);
		Label if_true = new Label();
		mv.visitLabel(if_true);

		System.out.println("if true");

		ifStatement.getB().visit(this, arg);
		mv.visitLabel(if_false);

		System.out.println("if false");

		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		switch (imageOpChain.getFirstToken().getKind()) {
		case OP_WIDTH:
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);

			/*
			 * Debug
			 */

			System.out.println("INVOKEVIRTUAL " + " java/awt/image/BufferedImage " + " getWidth " + "()I");

			break;

		case OP_HEIGHT:
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);

			/*
			 * Debug
			 */

			System.out.println("INVOKEVIRTUAL " + " java/awt/image/BufferedImage " + " getHeight " + "()I");

			break;

		case KW_SCALE:
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);

			/*
			 * Debug
			 */

			System.out.println("INVOKESTATIC " + PLPRuntimeImageOps.JVMName + " scale " + PLPRuntimeImageOps.scaleSig);

			break;

		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Implement this
		mv.visitLdcInsn(intLitExpression.getVal());
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Implement this
		// For assignment 5, only needs to handle integers and booleans
		MethodVisitor mv = (MethodVisitor) arg;

		TypeName typeName = paramDec.getTypeName();

		switch (typeName) {
		case INTEGER:
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			indexLoader(paramDec.getSlotNum(), mv);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
			break;

		case BOOLEAN:
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			indexLoader(paramDec.getSlotNum(), mv);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
			break;

		case FILE:
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			indexLoader(paramDec.getSlotNum(), mv);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
			break;

		case FRAME:
			// illegal
			break;

		case IMAGE:
			// illegal
			break;

		case URL:

			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			indexLoader(paramDec.getSlotNum(), mv);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");

			break;

		case NONE:

			break;

		default:
			break;
		}
		return null;

	}

	public void indexLoader(int index, MethodVisitor mv) {
		// mv.visitVarInsn(ALOAD, 1);
		switch (index) {
		case 0:
			mv.visitInsn(ICONST_0);
			break;

		case 1:
			mv.visitInsn(ICONST_1);
			break;

		case 2:
			mv.visitInsn(ICONST_2);
			break;

		case 3:
			mv.visitInsn(ICONST_3);
			break;

		case 4:
			mv.visitInsn(ICONST_4);
			break;

		case 5:
			mv.visitInsn(ICONST_5);
			break;

		default:
			mv.visitLdcInsn(index);
			break;
		}
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		/*
		 * Label start_try = new Label(); Label end_try = new Label(); Label
		 * start_exception = new Label(); Label end_exception = new Label();
		 * mv.visitTryCatchBlock(start_try, end_try, start_exception,
		 * "java/lang/InterruptedException"); mv.visitLabel(start_try);
		 */
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		/*
		 * mv.visitLabel(end_try); mv.visitJumpInsn(GOTO, end_exception);
		 * mv.visitLabel(start_exception); mv.visitFrame(Opcodes.F_SAME1, 0,
		 * null, 1, new Object[] {"java/lang/InterruptedException"});
		 * mv.visitVarInsn(ASTORE, 1); mv.visitLabel(end_exception);
		 */
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for (Expression expression : tuple.getExprList()) {
			expression.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Implement this
		Label while_guard = new Label();
		mv.visitJumpInsn(GOTO, while_guard);
		Label while_body = new Label();
		mv.visitLabel(while_body);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(while_guard);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, while_body);
		return null;
	}

}
