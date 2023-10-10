package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.core.parser.ast.Expression;

/**
 * An instruction that outputs the value of an <tt>Expression</tt>.
 */
public class Interpolation extends TemplateElement {

    private Expression expression;
    private Expression escapedExpression; // This will be the same as the expression if we are not within an escape block.

    public Interpolation(Expression expression) {
        this.expression = expression;
        this.escapedExpression = expression;
    }
    
    public void setEscapedExpression(Expression escapedExpression) {
    	this.escapedExpression = escapedExpression;
    }
    
    public Expression getEscapedExpression() {
    	return this.escapedExpression;
    }
    
    public Expression getExpression() {
    	return expression;
    }

    /**
     * Outputs the string value of the enclosed expression.
     */
    public void execute(Environment env) throws TemplateException, IOException {
        env.getOut().write(escapedExpression.getStringValue(env));
    }

    public String getDescription() {
        return this.getSource()  +
        (expression == escapedExpression 
            ? "" 
            : " escaped ${" + escapedExpression.getDescription() + "}");
    }
}
