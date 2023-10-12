package freemarker.core.ast;

import java.io.IOException;
import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.core.parser.ast.Expression;
import freemarker.core.parser.ast.TemplateElement;


/**
 * Represents a case in a switch statement.
 */
public class Case extends TemplateElement {


    // might as well just make these package-visible 
    // so the Switch can use them, no need to be too anal-retentive
    private boolean isDefault;
    private Expression expression;

    public Case(Expression expression, TemplateElement nestedBlock, boolean isDefault) 
    {
        this.expression = expression;
        this.add(nestedBlock);
        this.isDefault = isDefault;
    }
    
    public Expression getExpression() {
    	return expression;
    }
    
    public boolean isDefault() {
    	return isDefault;
    }

    public void execute(Environment env) 
        throws TemplateException, IOException 
    {
        if (firstChildOfType(TemplateElement.class) != null) {
            env.render(firstChildOfType(TemplateElement.class));
        }
    }

    public String getDescription() {
        if (isDefault) {
            return "default case";
        } 
        return "case " + expression;
    }
/*    
    public boolean isDefault() {
    	return isDefault;
    }*/
}
