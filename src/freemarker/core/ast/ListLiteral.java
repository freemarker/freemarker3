package freemarker.core.ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import freemarker.core.Environment;
import freemarker.core.TemplateNamespace;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateSequenceModel;

public class ListLiteral extends Expression {

    private final ArrayList<Expression> values;

    public ListLiteral(ArrayList<Expression> values) {
        this.values = values;
        values.trimToSize();
        for (Expression value : values) {
        	value.setParent(this);
        }
    }
    
    public List<Expression> getElements() {
    	return Collections.unmodifiableList(values);
    }
    
    PositionalArgsList getAsArgsList() {
    	PositionalArgsList result = new PositionalArgsList();
    	for (Expression exp: values) {
    		result.addArg(exp);
    	}
    	return result;
    }

    public Object _getAsTemplateModel(Environment env) {
        SimpleSequence list = new SimpleSequence(values.size());
        for (Iterator it = values.iterator(); it.hasNext();) {
            Expression exp = (Expression) it.next();
            Object tm = exp.getAsTemplateModel(env);
            assertIsDefined(tm, exp, env);
            list.add(tm);
        }
        return list;
    }

    public boolean isLiteral() {
        if (constantValue != null) {
            return true;
        }
        for (int i = 0; i<values.size(); i++) {
            Expression exp = values.get(i);
            if (!exp.isLiteral()) {
                return false;
            }
        }
        return true;
    }
    
    

    // A hacky routine used by VisitNode and RecurseNode
    
    TemplateSequenceModel evaluateStringsToNamespaces(Environment env) {
        TemplateSequenceModel val = (TemplateSequenceModel) getAsTemplateModel(env);
        SimpleSequence result = new SimpleSequence(val.size());
        for (int i=0; i<values.size(); i++) {
            if (values.get(i) instanceof StringLiteral) {
                String s = ((StringLiteral) values.get(i)).getAsString();
                try {
                    TemplateNamespace ns = env.importLib(s, null);
                    result.add(ns);
                } 
                catch (IOException ioe) {
                    throw new TemplateException("Could not import library '" + s + "', " + ioe.getMessage(), env); 
                }
            }
            else {
                result.add(val.get(i));
            }
        }
        return result;
    }
    
    public Expression _deepClone(String name, Expression subst) {
    	ArrayList<Expression> clonedValues = new ArrayList<Expression>(values.size());
    	for (Expression exp : values) {
    		clonedValues.add(exp.deepClone(name, subst));
    	}
        return new ListLiteral(clonedValues);
    }

}
