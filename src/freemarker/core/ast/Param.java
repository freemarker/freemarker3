package freemarker.core.ast;

import java.io.IOException;
import java.util.*;

import freemarker.core.Environment;
import freemarker.template.*;

public class Param extends TemplateElement {
	
    private String name;
    private ParameterList params;
    private List<Param> subParams;
    
 	public Param(String name, ParameterList params, TemplateElement block) {
 		this.name = name;
 		this.params = params;
 		this.nestedBlock = block;
 	}
 	
 	public String getName() {
 		return name;
 	}

	@Override
	public void execute(Environment env) throws TemplateException, IOException {
		env.unqualifiedSet(name, new Model());
	}
	
	class Expr extends Expression {
		public Expression _deepClone(String s, Expression e){
			return this;
		}
		
		public boolean isLiteral() {
			return false;
		}
		
		public TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
			return Param.this.new Model();
		}
		
	}
	
	public class Model implements TemplateModel {}
	
	static class MultiParam extends Expression {
		
		List<Param> params = new ArrayList<Param>();
		
		public Expression _deepClone(String s, Expression e) {
			return this;
		}
		
		public boolean isLiteral() {
			return false;
		}
		
		public TemplateModel _getAsTemplateModel(Environment env) throws TemplateException {
			SimpleSequence result = new SimpleSequence();
			for (Param param : params) {
				result.add(param.new Model());
			}
			return result;
		}
		
	}

}
