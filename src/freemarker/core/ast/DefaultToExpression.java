package freemarker.core.ast;


import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.template.*;

public class DefaultToExpression extends Expression {
	
    private static final TemplateCollectionModel EMPTY_COLLECTION = new SimpleCollection(new java.util.ArrayList(0));
    
	static private class EmptyStringAndSequence 
	  implements TemplateScalarModel, TemplateSequenceModel, TemplateHashModelEx {
		public String getAsString() {
			return "";
		}
		public TemplateModel get(int i) {
			return null;
		}
		public TemplateModel get(String s) {
			return null;
		}
		public int size() {
			return 0;
		}
		public boolean isEmpty() {
			return true;
		}
		public TemplateCollectionModel keys() {
			return EMPTY_COLLECTION;
		}
		public TemplateCollectionModel values() {
			return EMPTY_COLLECTION;
		}
		
	}
	
	static final TemplateModel EMPTY_STRING_AND_SEQUENCE = new EmptyStringAndSequence();
	
	private Expression lhs, rhs;
	
	public DefaultToExpression(Expression lhs, Expression rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		lhs.parent = this;
		if (rhs != null) rhs.parent = this;
	}
	
	public Expression getLeft() {
		return lhs;
	}
	
	public Expression getRight() {
		return rhs;
	}

	Object _getAsTemplateModel(Environment env) {
		Object left = null;		
		try {
			left = lhs.getAsTemplateModel(env);
		} catch (InvalidReferenceException ire) {
			if (!(lhs instanceof ParentheticalExpression)) {
				throw ire;
			}
		}
		if (left != null && left != Constants.JAVA_NULL) return left;
		if (rhs == null) return EMPTY_STRING_AND_SEQUENCE;
		return rhs.getAsTemplateModel(env);
	}

	boolean isLiteral() {
		return false;
	}

	Expression _deepClone(String name, Expression subst) {
		if (rhs == null) {
			return new DefaultToExpression(lhs.deepClone(name, subst), null);
		}
		return new DefaultToExpression(lhs.deepClone(name, subst), rhs.deepClone(name, subst));
	}
}
