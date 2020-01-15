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
 * An element that represents a conditionally executed block.
 * @author <A HREF="mailto:jon@revusky.com">Jonathan Revusky</A>
 */

public class ConditionalBlock extends TemplateElement {

    private Expression condition;
    private boolean isFirst;
    boolean isSimple;

    public ConditionalBlock(Expression condition, TemplateElement nestedBlock, boolean isFirst)
    {
        this.condition = condition;
        this.nestedBlock = nestedBlock;
        this.isFirst = isFirst;
    }
    
    public Expression getCondition() {
    	return condition;
    }
    
    public boolean isFirst() {
    	return isFirst;
    }
    
    public boolean isSimple() {
    	return isSimple;
    }

    public void execute(Environment env) throws TemplateException, IOException {
        if (isSimple && condition != null && !condition.isTrue(env)) {
        	return;
        }
        env.render(nestedBlock);
    }
    
    public boolean isLoneIfBlock() {
    	return isSimple;
    }
    
    public void setIsSimple(boolean isSimple) {
    	this.isSimple = isSimple;
    }

    public String getDescription() {
        String s = "if ";
        if (condition == null) {
            s = "else ";
        } 
        else if (!isFirst) {
            s = "elseif ";
        }
        String cond = condition != null ? condition.toString() : "";
        return s + cond;
    }
}
