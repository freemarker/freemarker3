package freemarker.core.ast;

import java.io.IOException;
import java.util.*;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.core.ooparam.*;

public class OOParamElement extends TemplateElement {
	
    private String name;
    private ParameterList params;
    private List<OOParamElement> subParams;
    private Expression exp;
    
 	public OOParamElement(String name, ParameterList params, TemplateElement block) {
 		this.name = name;
 		this.params = params;
 		this.nestedBlock = block;
 	}
 	
 	public String getName() {
 		return name;
 	}

	@Override
	public void execute(Environment env) throws TemplateException, IOException {
		env.unqualifiedSet(name, new OOParamModel(this));
	}
	
	public Expression asExp() {
		if (exp == null) {
			exp = new Expr();
		}
		return exp;
	}
	
	class Expr extends Expression {
		
		public Expr() {
			this.copyLocationFrom(OOParamElement.this);
		}
		
		
		public Expression _deepClone(String s, Expression e){
			return this;
		}
		
		public boolean isLiteral() {
			return false;
		}
		
		public TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
			return new OOParamModel(OOParamElement.this);
		}
		
	}
	
	static class MultiParam extends Expression {
		
		List<OOParamElement> params = new ArrayList<OOParamElement>();
		
		public Expression _deepClone(String s, Expression e) {
			return this;
		}
		
		public boolean isLiteral() {
			return false;
		}
		
		public TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
			SimpleSequence result = new SimpleSequence();
			for (OOParamElement param : params) {
				result.add(new OOParamModel(param));
			}
			return result;
		}
		
	}

}
