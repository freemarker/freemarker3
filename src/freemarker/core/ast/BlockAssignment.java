package freemarker.core.ast;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.Scope;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateException;
import freemarker.template.TemplateTransformModel;

/**
 * @version $Id: BlockAssignment.java,v 1.4 2004/07/07 21:11:12 szegedia Exp $
 */
@SuppressWarnings("deprecation")
public class BlockAssignment extends TemplateElement {

    private String varName;
    private Expression namespaceExp;
    private int type;

    public BlockAssignment(TemplateElement nestedBlock, String varName, int type, Expression namespaceExp) {
        this.add(nestedBlock);
        this.varName = varName;
        this.namespaceExp = namespaceExp;
        this.type = type;
    }
    
    public Expression getNamespaceExpression() {
    	return namespaceExp;
    }
    
    public String getVarName() {
    	return varName;
    }
    
    public int getBlockType() {
    	return type;
    }

    public void execute(Environment env) throws TemplateException, IOException {
    	Scope scope = null;
    	if (namespaceExp != null) {
    		try {
    			scope = (Scope) namespaceExp.getAsTemplateModel(env); 
    		} catch (ClassCastException cce) {
                throw new InvalidReferenceException(getStartLocation() + "\nInvalid reference to namespace: " + namespaceExp, env);
    		}
    	}
    	else {
    		if (type == AssignmentInstruction.NAMESPACE) {
    			scope = env.getCurrentNamespace();
    		} else if (type == AssignmentInstruction.LOCAL) {
    			scope = env.getCurrentMacroContext();
    		} else if (type == AssignmentInstruction.GLOBAL) {
    			scope = env.getGlobalNamespace();
    		} 
    	}
    	CaptureOutput filter = new CaptureOutput();
        if (firstChildOfType(TemplateElement.class) != null) {
            env.render(firstChildOfType(TemplateElement.class), filter, null);
        }
        String text = filter.capturedText;
    	if (scope != null) {
    		scope.put(varName, new SimpleScalar(text));
    	} else {
    		env.unqualifiedSet(varName, new SimpleScalar(text));
    	}
    }
    
    private static class CaptureOutput implements TemplateTransformModel {
        String capturedText = ""; 
        
        public Writer getWriter(Writer out, Map<String, Object> args) {
            return new StringWriter() {
                public void close() {
                	capturedText = this.toString();
                }
            };
        }
    }
    
    public String getDescription() {
        return "block assignment to variable: " + varName;
    }

}

