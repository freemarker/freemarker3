package freemarker.template.utility;

import freemarker.core.ast.*;

/**
 * A ASTVisitor that escapes all interpolations
 * as exp?html`
 * @author revusky
 */

public class HTMLEncodingASTVisitor extends ASTVisitor {
	
	public void visit(Interpolation interpolation) {
		Expression exp = interpolation.getEscapedExpression();
		if (exp instanceof BuiltInExpression) {
			BuiltInExpression bi = (BuiltInExpression) exp;
			if (bi.getName() == "html") {
				// If this is an expression that ends in ?html we 
				// jump out to avoid double escaping.
				// This is not airtight, since it doesn't catch
				// things like x?html?upper_case
				// but it's good enough for this example. :-)
				return;
			}
		}
		interpolation.setEscapedExpression(new BuiltInExpression(exp, "html"));
	}
}
