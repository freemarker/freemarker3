package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.core.parser.ast.Expression;
import freemarker.core.parser.ast.TemplateElement;

/**
 * Representation of the compile-time Escape directive.
 * @version $Id: EscapeBlock.java,v 1.1 2003/04/22 21:05:01 revusky Exp $
 * @author Attila Szegedi
 */
public class EscapeBlock extends TemplateElement {

    private String variable;
    private Expression expr;
    private Expression escapedExpr; // This will be the same as expr unless we are within another escape block.


    public EscapeBlock(String variable, Expression expr) {
        this.variable = variable;
        this.expr = expr;
        this.escapedExpr = expr;
    }
    
    public Expression getExpression() {
    	return expr;
    }
    
    public String getVariable() {
    	return variable;
    }
    
    public Expression getEscapedExpression() {
    	return escapedExpr;
    }
    
    /**
     * This is only used internally.
     */
    public void setEscapedExpression(Expression escapedExpr) {
    	this.escapedExpr = escapedExpr;
    }

    public void setContent(TemplateElement nestedBlock) {
        this.add(nestedBlock);
    }

    public void execute(Environment env) throws TemplateException, IOException {
        if (firstChildOfType(TemplateElement.class) != null) {
            env.render(firstChildOfType(TemplateElement.class));
        }
    }

    public Expression doEscape(Expression subst) {
        return escapedExpr.deepClone(variable, subst);
    }

    public String getDescription() {
        return "escape " + variable + " as " + expr.toString();
    }
}
