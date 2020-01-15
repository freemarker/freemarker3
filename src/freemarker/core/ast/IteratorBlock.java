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

import freemarker.template.*;
import freemarker.core.*;

/**
 * An instruction that processes a list or foreach block
 */
public class IteratorBlock extends TemplateElement {

    private String indexName;
    private Expression listExpression;
    private boolean isForEach;

    /**
     * @param listExpression a variable referring to a sequence or collection
     * @param indexName an arbitrary index variable name
     * @param nestedBlock the nestedBlock to iterate over
     */
    public IteratorBlock(Expression listExpression,
                          String indexName,
                          TemplateElement nestedBlock,
                          boolean isForEach) 
    {
        this.listExpression = listExpression;
        this.indexName = indexName;
        this.isForEach = isForEach;
        this.nestedBlock = nestedBlock;
    }
    
    public String getIndexName() {
    	return indexName;
    }
    
    public Expression getListExpression() {
    	return listExpression;
    }

    public void execute(Environment env) throws TemplateException, IOException 
    {
        TemplateModel baseModel = listExpression.getAsTemplateModel(env);
        assertNonNull(baseModel, listExpression, env);
        env.process(new LoopContext(this, env.getCurrentScope(), baseModel)); // REVISIT
    }

    public String getDescription() {
        if (isForEach) {
            return "foreach " + indexName + " in " + listExpression; 

        }
        else {
            return "list " + listExpression + " as " + indexName;
        }
    }
}
