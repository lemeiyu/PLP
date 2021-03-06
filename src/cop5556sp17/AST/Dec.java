package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;

public class Dec extends ASTNode {
	
	final Token ident;
	public Type.TypeName typeName;
	int slotNumber;
	boolean initialized;

	public Dec(Token firstToken, Token ident) {
		super(firstToken);

		this.ident = ident;
		initialized = false;
	}

	public Token getType() {
		return firstToken;
	}

	public Token getIdent() {
		return ident;
	}
	
	public TypeName getTypeName() {
		return typeName;
	}
	
	public void setTypeName(TypeName typeName) {
		this.typeName = typeName;
	}

	public void setSlotNum(int i) {
		slotNumber = i;
	}
	
	public int getSlotNum() {
		return slotNumber;
	}
	
	public void setInitialized(boolean t) {
		initialized = t;
	}
	
	public boolean getInitialized() {
		return initialized;
	}
	
	@Override
	public String toString() {
		return "Dec [ident=" + ident + ", firstToken=" + firstToken + "]";
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ident == null) ? 0 : ident.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Dec)) {
			return false;
		}
		Dec other = (Dec) obj;
		if (ident == null) {
			if (other.ident != null) {
				return false;
			}
		} else if (!ident.equals(other.ident)) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitDec(this,arg);
	}

}
