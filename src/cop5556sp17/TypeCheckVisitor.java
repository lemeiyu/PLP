package cop5556sp17;

//import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
//import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
//import cop5556sp17.AST.Expression;
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
//import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

//import java.util.ArrayList;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
//import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
//import static cop5556sp17.Scanner.Kind.OP_BLUR;
//import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
//import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		binaryChain.getE0().visit(this, arg);
		binaryChain.getE1().visit(this, arg);

		TypeName t0 = binaryChain.getE0().getTypeName();
		TypeName t1 = binaryChain.getE1().getTypeName();
		Token op = binaryChain.getArrow();
		ChainElem elem = binaryChain.getE1();
		// binaryChain.getE1().getClass().equals(ImageOpChain.class);
		if (op.isKind(ARROW)) {
			if (t0 == URL) {
				if (t1 == IMAGE) {
					binaryChain.setTypeName(IMAGE);
				} else {
					LinePos linePos = binaryChain.getE1().getFirstToken().getLinePos();
					throw new TypeCheckException("Type of chain Element is not compatible, at line: " + linePos.line
							+ ", column: " + linePos.posInLine);
				}
			} else if (t0 == FILE) {
				if (t1 == IMAGE) {
					binaryChain.setTypeName(IMAGE);
				} else {
					LinePos linePos = binaryChain.getE1().getFirstToken().getLinePos();
					throw new TypeCheckException("Type of chain Element is not compatible, at line: " + linePos.line
							+ ", column: " + linePos.posInLine);
				}
			} else if (t0 == FRAME) {
				if (elem instanceof FrameOpChain) {
					if (elem.getFirstToken().isKind(KW_XLOC) || elem.getFirstToken().isKind(KW_YLOC)) {
						binaryChain.setTypeName(TypeName.INTEGER);
					} else {
						binaryChain.setTypeName(TypeName.FRAME);
					}
				} else {
					LinePos linePos = binaryChain.getE1().getFirstToken().getLinePos();
					throw new TypeCheckException("Type of chain Element is not compatible, at line: " + linePos.line
							+ ", column: " + linePos.posInLine);
				}
			} else if (t0 == IMAGE) {
				if (t1 == FRAME) {
					binaryChain.setTypeName(FRAME);
				} else if (t1 == FILE) {
					binaryChain.setTypeName(NONE);
				} else if (elem instanceof ImageOpChain) {
					if (elem.getFirstToken().isKind(KW_SCALE)) {
						binaryChain.setTypeName(IMAGE);
					} else {
						binaryChain.setTypeName(INTEGER);
					}
				} else if (elem instanceof FilterOpChain) {
					binaryChain.setTypeName(IMAGE);
				} else if ((elem instanceof IdentChain) && (t1 == IMAGE)) {
					binaryChain.setTypeName(IMAGE);
				} else {
					LinePos linePos = binaryChain.getE1().getFirstToken().getLinePos();
					throw new TypeCheckException("Type of chain Element is not compatible, at line: " + linePos.line
							+ ", column: " + linePos.posInLine);
				}
			} else if (t0 == INTEGER) {
				if ((elem instanceof IdentChain) && (t1 == INTEGER)) {
					binaryChain.setTypeName(INTEGER);
				} else {
					LinePos linePos = binaryChain.getE1().getFirstToken().getLinePos();
					throw new TypeCheckException("Type of chain Element is not compatible, at line: " + linePos.line
							+ ", column: " + linePos.posInLine);
				}
			} else {
				LinePos linePos = binaryChain.getE1().getFirstToken().getLinePos();
				throw new TypeCheckException("Type of chain Element is not compatible, at line: " + linePos.line
						+ ", column: " + linePos.posInLine);
			}
		} else {
			if (elem instanceof FilterOpChain) {
				binaryChain.setTypeName(IMAGE);
			} else {
				LinePos linePos = op.getLinePos();
				throw new TypeCheckException(
						"Wrong arrow operator, at line: " + linePos.line + ", column: " + linePos.posInLine);
			}
		}

		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		binaryExpression.getE0().visit(this, arg);
		binaryExpression.getE1().visit(this, arg);

		TypeName t0 = binaryExpression.getE0().getTypeName();
		TypeName t1 = binaryExpression.getE1().getTypeName();
		Token op = binaryExpression.getOp();

		if (t0 == t1) {
			if (op.isKind(EQUAL) || op.isKind(Kind.NOTEQUAL)) {
				binaryExpression.setTypeName(BOOLEAN);
			} else if (op.isKind(Kind.PLUS) || op.isKind(MINUS)) {
				binaryExpression.setTypeName(t0);
			} else if (op.isKind(TIMES)) {
				binaryExpression.setTypeName(t0);
			} else if (op.isKind(Kind.DIV) || op.isKind(MOD)) {
				if (t0 == TypeName.INTEGER) {
					binaryExpression.setTypeName(INTEGER);
				} else {
					LinePos linePos = binaryExpression.getFirstToken().getLinePos();
					throw new TypeCheckException("Expression cannot be divided, at line: " + linePos.line + ", column: "
							+ linePos.posInLine);
				}
			} else if (op.isKind(Kind.LT) || op.isKind(GT) || op.isKind(LE) || op.isKind(GE)) {
				if ((t0 == INTEGER) || (t0 == TypeName.BOOLEAN)) {
					binaryExpression.setTypeName(TypeName.BOOLEAN);
				} else {
					LinePos linePos = binaryExpression.getFirstToken().getLinePos();
					throw new TypeCheckException("Expressions cannot be compared, at line: " + linePos.line
							+ ", column: " + linePos.posInLine);
				}
			} else if (op.isKind(AND) || op.isKind(OR)) {
				if (t0 == TypeName.BOOLEAN) {
					binaryExpression.setTypeName(BOOLEAN);
				} else {
					LinePos linePos = binaryExpression.getFirstToken().getLinePos();
					throw new TypeCheckException("Expressions cannot be compared, at line: " + linePos.line
							+ ", column: " + linePos.posInLine);
				}
			}
		} else if (op.isKind(Kind.TIMES)) {
			if (((t0 == INTEGER) && (t1 == IMAGE)) || (t0 == IMAGE) && (t1 == INTEGER)) {
				binaryExpression.setTypeName(IMAGE);
			} else {
				LinePos linePos = binaryExpression.getFirstToken().getLinePos();
				throw new TypeCheckException("Types of expression are not compatible, at line: " + linePos.line
						+ ", column: " + linePos.posInLine);
			}
		} else if (op.isKind(Kind.DIV) || op.isKind(MOD)) {
			if ((t0 == IMAGE) && (t1 == INTEGER)) {
				binaryExpression.setTypeName(TypeName.IMAGE);
			} else {
				LinePos linePos = binaryExpression.getFirstToken().getLinePos();
				throw new TypeCheckException("Types of expression are not compatible, at line: " + linePos.line
						+ ", column: " + linePos.posInLine);
			}
		} else {
			LinePos linePos = binaryExpression.getFirstToken().getLinePos();
			throw new TypeCheckException("Types of expression are not compatible, at line: " + linePos.line
					+ ", column: " + linePos.posInLine);
		}

		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.enterScope();
		for (int i = 0; i < block.getDecs().size(); i++) {
			block.getDecs().get(i).visit(this, arg);
		}
		for (int i = 0; i < block.getStatements().size(); i++) {
			block.getStatements().get(i).visit(this, arg);
		}
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setTypeName(BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		filterOpChain.getArg().visit(this, arg);
		if (filterOpChain.getArg().getLength() == 0) {
			filterOpChain.setTypeName(IMAGE);
		} else {
			LinePos linePos = filterOpChain.getFirstToken().getLinePos();
			throw new TypeCheckException("Filter operation don't need any parameter. At line: " + linePos.line
					+ ", column: " + linePos.posInLine);
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		frameOpChain.getArg().visit(this, arg);
		frameOpChain.setKind(frameOpChain.getFirstToken().getKind());
		if (frameOpChain.getFirstToken().isKind(KW_SHOW) || frameOpChain.getFirstToken().isKind(Kind.KW_HIDE)) {
			if (frameOpChain.getArg().getLength() == 0) {
				frameOpChain.setTypeName(NONE);
			} else {
				LinePos linePos = frameOpChain.getFirstToken().getLinePos();
				throw new TypeCheckException("Show and hide operation do not need any parameter. At line: "
						+ linePos.line + ", column: " + linePos.posInLine);
			}

		} else if (frameOpChain.getFirstToken().isKind(KW_XLOC) || frameOpChain.getFirstToken().isKind(KW_YLOC)) {
			if (frameOpChain.getArg().getLength() == 0) {
				frameOpChain.setTypeName(INTEGER);
			} else {
				LinePos linePos = frameOpChain.getFirstToken().getLinePos();
				throw new TypeCheckException("XLOC and YLOC method do not need any parameter. At line: " + linePos.line
						+ ", column: " + linePos.posInLine);
			}

		} else if (frameOpChain.getFirstToken().isKind(KW_MOVE)) {
			if (frameOpChain.getArg().getLength() == 2) {
				frameOpChain.setTypeName(NONE);
			} else {
				LinePos linePos = frameOpChain.getFirstToken().getLinePos();
				throw new TypeCheckException("Move operation needs two integer as xloc and yloc. At line: "
						+ linePos.line + ", colomn: " + linePos.posInLine);
			}
		} else {
			LinePos linePos = frameOpChain.getFirstToken().getLinePos();
			throw new TypeCheckException("Unknow error. At line: " + linePos.line + ", column: " + linePos.posInLine);
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec dec = symtab.lookup(identChain.getFirstToken().getText());
		if (dec == null) {
			LinePos linePos = identChain.getFirstToken().getLinePos();
			throw new TypeCheckException(
					"Identity has not been declared, at line: " + linePos.line + ", column: " + linePos.posInLine);
		}
		identChain.setDec(dec);
		identChain.setTypeName(dec.getTypeName());
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec dec = symtab.lookup(identExpression.getFirstToken().getText());
		if (dec == null) {
			LinePos linePos = identExpression.getFirstToken().getLinePos();
			throw new TypeCheckException(
					"Identity has not been declared, at line: " + linePos.line + ", column: " + linePos.posInLine);
		}
		identExpression.setTypeName(dec.getTypeName());
		identExpression.setDec(dec);
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		ifStatement.getE().visit(this, arg);
		ifStatement.getB().visit(this, arg);
		if (ifStatement.getE().getTypeName() != TypeName.BOOLEAN) {
			LinePos linePos = ifStatement.getFirstToken().getLinePos();
			throw new TypeCheckException("If statement need an expression which returns a boolean, but it is "
					+ ifStatement.getE().getTypeName() + ", at line: " + linePos.line + ", column: "
					+ linePos.posInLine);
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setTypeName(INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		sleepStatement.getE().visit(this, arg);
		if (sleepStatement.getE().getTypeName() != TypeName.INTEGER) {
			LinePos linePos = sleepStatement.getFirstToken().getLinePos();
			throw new TypeCheckException("Wrong expression type at line: " + linePos.line + ", column: "
					+ linePos.posInLine + ". A sleep statement need an expression which returns an integer.");
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		whileStatement.getE().visit(this, arg);
		whileStatement.getB().visit(this, arg);
		if (whileStatement.getE().getTypeName() != TypeName.BOOLEAN) {
			LinePos linePos = whileStatement.getFirstToken().getLinePos();
			throw new TypeCheckException("While statement need an expression which returns a boolean, but it is "
					+ whileStatement.getE().getTypeName() + ", at line: " + linePos.line + ", column: "
					+ linePos.posInLine);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		declaration.setTypeName(Type.getTypeName(declaration.getType()));
		if (!symtab.insert(declaration.getIdent().getText(), declaration)) {
			LinePos linePos = declaration.getIdent().getLinePos();
			throw new TypeCheckException(
					"Duplicated declaration at line: " + linePos.line + ", column: " + linePos.posInLine);
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		for (int i = 0; i < program.getParams().size(); i++) {
			program.getParams().get(i).visit(this, arg);
		}
		program.getB().visit(this, arg);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getVar().visit(this, arg);
		assignStatement.getE().visit(this, arg);
		// TODO Auto-generated method stub
		if (assignStatement.getE().getTypeName() != assignStatement.getVar().getDec().getTypeName()) {
			LinePos linePos = assignStatement.firstToken.getLinePos();
			throw new TypeCheckException("The type of variable " + assignStatement.getVar().getText()
					+ " and the type returned by expression are not compatible, at line: " + linePos.line + ", column: "
					+ linePos.posInLine);
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec dec;
		dec = symtab.lookup(identX.getText());
		if (dec == null) {
			LinePos linePos = identX.getFirstToken().getLinePos();
			throw new TypeCheckException(
					"Identity has not been declared, at line: " + linePos.line + ", column: " + linePos.posInLine);
		}
		identX.setDec(dec);
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		paramDec.setTypeName(Type.getTypeName(paramDec.getFirstToken()));
		if (!symtab.insert(paramDec.getIdent().getText(), paramDec)) {
			LinePos linePos = paramDec.getIdent().getLinePos();
			throw new TypeCheckException(
					"Duplicated declarations at line: " + linePos.line + ", column: " + linePos.posInLine);
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setTypeName(INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		imageOpChain.getArg().visit(this, arg);
		imageOpChain.setKind(imageOpChain.getFirstToken().getKind());
		if (imageOpChain.getFirstToken().isKind(OP_WIDTH) || imageOpChain.getFirstToken().isKind(OP_HEIGHT)) {
			if (imageOpChain.getArg().getLength() == 0) {
				imageOpChain.setTypeName(INTEGER);
			} else {
				LinePos linePos = imageOpChain.getFirstToken().getLinePos();
				throw new TypeCheckException("Operation width and height do not need any parameter, at line: "
						+ linePos.line + ", column: " + linePos.posInLine);
			}
		} else if (imageOpChain.getFirstToken().isKind(KW_SCALE)) {
			if (imageOpChain.getArg().getLength() == 1) {
				imageOpChain.setTypeName(IMAGE);
			} else {
				LinePos linePos = imageOpChain.getFirstToken().getLinePos();
				throw new TypeCheckException("Scale operation need one integer as its parameter. At line: "
						+ linePos.line + ", column: " + linePos.posInLine);
			}
		}
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		for (int i = 0; i < tuple.getExprList().size(); i++) {
			tuple.getExprList().get(i).visit(this, arg);
			if (tuple.getExprList().get(i).getTypeName() != INTEGER) {
				LinePos linePos = tuple.getExprList().get(i).getFirstToken().getLinePos();
				throw new TypeCheckException("Every expressions in tuple should have return type integer. At line: "
						+ linePos.line + ", column: " + linePos.posInLine);
			}
		}
		return null;
	}

	/*
	 * public Object visitStatement(Statement statement, Object arg) throws
	 * Exception {
	 * 
	 * return null; }
	 */

}
