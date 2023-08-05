package freemarker.core.ast;

import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.parser.TemplateLocation;
import freemarker.core.helpers.DefaultReferenceChecker;
import freemarker.core.helpers.DefaultTreeDumper;


/**
 * Objects that represent instructions or expressions
 * in the compiled tree representation of the template
 * all descend from this abstract base class.
 */

public abstract class TemplateNode extends TemplateLocation {
	
	static private DefaultReferenceChecker referenceChecker = DefaultReferenceChecker.instance;
	static private DefaultTreeDumper canonicalTreeRenderer = new DefaultTreeDumper(false);
	
	TemplateNode parent;
	
	public String source() {
        if (template != null) {
            return template.source(getBeginColumn(), getBeginLine(), getEndColumn(), getEndLine());
        } else {
            return getCanonicalForm();
        }
    }

    public String toString() {
    	try {
    		return source();
    	} catch (Exception e) { // REVISIT: A bit of a hack? (JR)
    		return getCanonicalForm();
    	}
    }

    
    static public TemplateException invalidTypeException(Object model, Expression exp, Environment env, String expected)
    throws
        TemplateException
    {
        assertNonNull(model, exp, env);
        return new TemplateException(
            "Expected " + expected + ". " + 
            exp + " evaluated instead to " + 
            model.getClass().getName() + " " +
            exp.getStartLocation() + ".", env);
    }
    
    static public void assertNonNull(Object model, Expression exp, Environment env) throws InvalidReferenceException {
    	referenceChecker.assertNonNull(model, exp, env);
    }
    
    static public void assertIsDefined(Object model, Expression exp, Environment env) throws InvalidReferenceException {
    	referenceChecker.assertIsDefined(model, exp, env);
    }
    
    public final String getCanonicalForm() {
    	return canonicalTreeRenderer.render(this);
    }
    
    public TemplateNode getParentNode() {
    	return parent;
    }
}
