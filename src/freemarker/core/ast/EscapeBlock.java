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

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.*;

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
        this.nestedBlock = nestedBlock;
    }

    public void execute(Environment env) throws TemplateException, IOException {
        if (nestedBlock != null) {
            env.render(nestedBlock);
        }
    }

    public Expression doEscape(Expression subst) {
        return escapedExpr.deepClone(variable, subst);
    }

    public String getDescription() {
        return "escape " + variable + " as " + expr.toString();
    }
}
