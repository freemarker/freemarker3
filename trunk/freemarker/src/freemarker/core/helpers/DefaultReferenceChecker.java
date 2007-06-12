package freemarker.core.helpers;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ast.Expression;
import freemarker.template.TemplateModel;
import java.util.*;

public class DefaultReferenceChecker {
	
	protected DefaultReferenceChecker() {}
	static public final DefaultReferenceChecker instance = new DefaultReferenceChecker();
	
	private Locale locale;
	
    public void assertNonNull(TemplateModel model, Expression exp, Environment env) throws InvalidReferenceException {
        if (model == null) {
            throw new InvalidReferenceException(
                "Expression " + exp + " is undefined " +
                exp.getStartLocation() + ".", env);
        }
        if (model == TemplateModel.JAVA_NULL) {
            throw new InvalidReferenceException(
                "Expression " + exp + " is null " +
                exp.getStartLocation() + ".", env);
        }
    }
    
    public void assertIsDefined(TemplateModel model, Expression exp, Environment env) throws InvalidReferenceException {
    	if (model == null) {
    		assertNonNull(null, exp, env);
    	}
    }
    
    public void setLocale(Locale locale) {
    	this.locale = locale;
    }
    
    public Locale getLocale() {
    	return this.locale;
    }

}
