/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
        nestedElements = new LinkedList<TemplateElement>();
    }

    /**
     * @param cas a Case element.
     */
    public void addCase(Case cas) {
        nestedElements.add(cas);
    }
    
    public List<TemplateElement> getCases() {
    	return nestedElements;
    }
    
    public Expression getTestExpression() {
    	return testExpression;
    }
    
    public void execute(Environment env) 
        throws TemplateException, IOException 
    {
        boolean processedCase = false;
        Iterator iterator = nestedElements.iterator();
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
