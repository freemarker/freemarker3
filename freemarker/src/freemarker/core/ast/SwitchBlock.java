/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
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

    public final Expression testExpression;

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
                } else if (cas.isDefault) {
                    defaultCase = cas;
                }
                else {
                    // Otherwise, if this case isn't the default, test it.
                    ComparisonExpression equalsOp = new ComparisonExpression(testExpression, cas.expression, "==");
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
