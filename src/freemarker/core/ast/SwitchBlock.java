package freemarker.core.ast;

import java.util.*;
import java.io.IOException;

import freemarker.core.Environment;
import freemarker.core.BreakException;
import freemarker.template.*;

/**
 * An instruction representing a switch-case structure.
 */
public class SwitchBlock extends TemplateElement {

    private Expression testExpression;

    /**
     * @param testExpression the expression to be tested.
     */
    public SwitchBlock(Expression testExpression) {
        this.testExpression = testExpression;
    }

    /**
     * @param cas a Case element.
     */
    public void addCase(Case cas) {
        add(cas);
    }
    
    public List<Case> getCases() {
    	return childrenOfType(Case.class);
    }
    
    public Expression getTestExpression() {
    	return testExpression;
    }
    
    public void execute(Environment env) 
        throws TemplateException, IOException 
    {
        boolean processedCase = false;
        Iterator<TemplateElement> iterator = childrenOfType(TemplateElement.class).iterator();
        try {
            Case defaultCase = null;
            while (iterator.hasNext()) {
                Case cas = (Case)iterator.next();
                boolean processCase = false;

                // Fall through if a previous case tested true.
                if (processedCase) {
                    processCase = true;
                } else if (cas.isDefault()) {
                    defaultCase = cas;
                }
                else {
                    // Otherwise, if this case isn't the default, test it.
                    ComparisonExpression equalsOp = new ComparisonExpression(testExpression, cas.getExpression(), "==");
                    processCase = equalsOp.isTrue(env);
                }
                if (processCase) {
                    env.render(cas);
                    processedCase = true;
                }
            }

            // If we didn't process any nestedElements, and we have a default,
            // process it.
            if (!processedCase && defaultCase != null) {
                env.render(defaultCase);
            }
        }
        catch (BreakException br) {}
    }

    public String getDescription() {
        return "switch " + testExpression;
    }
}
