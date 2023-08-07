package freemarker.core.ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import freemarker.core.Environment;
import freemarker.core.TemplateNamespace;
import freemarker.ext.beans.ListModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateSequenceModel;

public class ListLiteral extends Expression {
    public ListLiteral() {}

    public ListLiteral(ArrayList<Expression> values) {
        addAll(values);
    }
    
    public List<Expression> getElements() {
        return childrenOfType(Expression.class);
    }
    
    public Object getAsTemplateModel(Environment env) {
        ListModel list = new ListModel();
        for (Expression exp: getElements()) {
            Object tm = exp.getAsTemplateModel(env);
            assertIsDefined(tm, exp, env);
            list.add(tm);
        }
        return list;
    }

    // A hacky routine used by VisitNode and RecurseNode
    
    TemplateSequenceModel evaluateStringsToNamespaces(Environment env) {
        TemplateSequenceModel val = (TemplateSequenceModel) getAsTemplateModel(env);
        ListModel result = new ListModel();
        for (Expression exp : getElements()) {
            if (exp instanceof StringLiteral) {
                String s = ((StringLiteral) exp).getAsString();
                try {
                    TemplateNamespace ns = env.importLib(s, null);
                    result.add(ns);
                } 
                catch (IOException ioe) {
                    throw new TemplateException("Could not import library '" + s + "', " + ioe.getMessage(), env); 
                }
            }
            else {
                result.add(exp);
            }
        }
        return result;
    }
    
    public Expression _deepClone(String name, Expression subst) {
    	ArrayList<Expression> clonedValues = new ArrayList<Expression>(size());
    	for (Expression exp : getElements()) {
    		clonedValues.add(exp.deepClone(name, subst));
    	}
        return new ListLiteral(clonedValues);
    }

}
