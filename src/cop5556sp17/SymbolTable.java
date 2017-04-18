package cop5556sp17;

import java.util.HashMap;
//import java.util.LinkedList;
import java.util.Stack;

import cop5556sp17.AST.Dec;

public class SymbolTable {

	// TODO add fields

	/**
	 * to be called when block entered
	 */
	Stack<Integer> scopeStack;
	HashMap<String, ScopeDecPair> identRecord;
	int curScope;
	int nextScope;

	public void enterScope() {
		// TODO: IMPLEMENT THIS
		curScope = ++nextScope;
		scopeStack.push(curScope);
	}

	/**
	 * leaves scope
	 */
	public void leaveScope() {
		// TODO: IMPLEMENT THIS
		curScope = scopeStack.pop();
	}

	public boolean insert(String ident, Dec dec) {
		// TODO: IMPLEMENT THIS
		if (identRecord.containsKey(ident)) {
			// this identity name is already in the map.
			// need to check if the identity has more than one declaration
			// within this scope.
			if (identRecord.get(ident).lookup(curScope)) {
				// the identity has been declared in this scope
				return false;
			} else {
				identRecord.get(ident).put(curScope, dec);
			}
		} else {
			// this identity name is a new one.
			// create its active record and put them into the map
			identRecord.put(ident, new ScopeDecPair(curScope, dec));
		}
		return true;
	}

	public Dec lookup(String ident) {
		// TODO: IMPLEMENT THIS
		if (identRecord.containsKey(ident)) {
			for (int i = scopeStack.size() - 1; i >= 0; i--) {
				int j = scopeStack.get(i);
				if (identRecord.get(ident).lookup(j)) {
					Dec dec = identRecord.get(ident).sdpair.get(j);
					return dec;
				}
			}
		} else {
			return null;
		}
		return null;
	}

	public SymbolTable() {
		// TODO: IMPLEMENT THIS
		scopeStack = new Stack<Integer>();
		identRecord = new HashMap<String, ScopeDecPair>();
		curScope = 0;
		nextScope = 0;
		scopeStack.push(curScope);
	}

	@Override
	public String toString() {
		// TODO: IMPLEMENT THIS
		return "";
	}

	public class ScopeDecPair {
		HashMap<Integer, Dec> sdpair;
		int recentScope;

		public ScopeDecPair() {
			// TODO Auto-generated constructor stub
			sdpair = new HashMap<Integer, Dec>();
			recentScope = 0;
		}

		public ScopeDecPair(int scope, Dec dec) {
			sdpair = new HashMap<Integer, Dec>();
			put(scope, dec);
			recentScope = scope;
		}

		public void put(int scope, Dec dec) {
			sdpair.put(scope, dec);
		}

		public void remove() {

		}

		public boolean lookup(int scope) {
			if (sdpair.containsKey(scope)) {
				return true;
			}
			return false;
		}
	}

	/*public class ScopeDecPair1 {
		LinkedList<Integer> scopeList;
		LinkedList<Dec> decList;
		int recentScope;

		public ScopeDecPair1() {
			// TODO Auto-generated constructor stub
			scopeList = new LinkedList<Integer>();
			decList = new LinkedList<Dec>();
			recentScope = 0;
		}

		public ScopeDecPair1(int scope, Dec dec) {
			scopeList = new LinkedList<Integer>();
			decList = new LinkedList<Dec>();
			put(scope, dec);
			recentScope = scope;
		}

		public void put(int scope, Dec dec) {
			scopeList.addFirst(scope);
			decList.addFirst(dec);
		}

		public void remove() {
			if (scopeList.size() != 0) {
				recentScope = scopeList.getFirst();
				scopeList.removeFirst();
				decList.removeFirst();
			}
		}

		public boolean lookup(int scope) {
			for (int i = 0; i < scopeList.size(); i++) {
				int j = scopeList.get(i);
				if (j == scope) {
					return true;
				}
			}
			return false;
		}
	}*/

}
