package freemarker.core.ast;

import java.io.IOException;
import java.util.*;
import freemarker.core.parser.ast.Expression;
import freemarker.template.TemplateException;
import freemarker.core.*;

/**
 * An instruction that contains one or more assignments
 */
public class AssignmentInstruction extends TemplateElement {
	
	static public final int SET = 0;
	static public final int NAMESPACE = 1;
	static public final int GLOBAL = 2;
	static public final int LOCAL = 3;

    private int type; 
    private Expression namespaceExp;
    
    private ArrayList<String> varNames = new ArrayList<String>();
    private ArrayList<Expression> values = new ArrayList<Expression>();

    public AssignmentInstruction(int type) {
        this.type = type;
    }
    
    public int getBlockType() {
    	return type;
    }
    
    public List<String> getVarNames() {
    	return Collections.unmodifiableList(varNames);
    }
    
    public List<Expression> getValues() {
    	return Collections.unmodifiableList(values);
    }

    public void addAssignment(String var, Expression valueExp) {
    	varNames.add(var);
    	values.add(valueExp);
    }
    
    public void setNamespaceExp(Expression namespaceExp) {
        this.namespaceExp = namespaceExp;
    }
    
    public Expression getNamespaceExp() {
        return namespaceExp;
    }

    public void execute(Environment env) throws TemplateException, IOException {
    	Scope scope = null;
    	if (namespaceExp != null) {
    		try {
    			scope = (Scope) namespaceExp.evaluate(env); 
    		} catch (ClassCastException cce) {
                throw new InvalidReferenceException(getStartLocation() + "\nInvalid reference to namespace: " + namespaceExp, env);
    		}
    	}
    	else {
    		if (type == NAMESPACE) {
    			scope = env.getCurrentNamespace();
    		} else if (type == LOCAL) {
    			scope = env.getCurrentMacroContext();
    		} else if (type == GLOBAL) {
    			scope = env.getGlobalNamespace();
    		}
    	}
    	for (int i=0; i< varNames.size(); i++) {
    		String varname = varNames.get(i);
    		Expression valueExp = values.get(i);
    		Object value = valueExp.evaluate(env);
    		assertIsDefined(value, valueExp, env);
    		if (scope != null) {
    			scope.put(varname, value);
    		} else {
    			env.unqualifiedSet(varname, value);
    		}
    	}
    }
    
    public String getDescription() {
    	return "assignment instruction";
    }
}
