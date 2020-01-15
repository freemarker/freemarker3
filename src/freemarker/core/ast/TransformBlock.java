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

import java.io.*;
import java.util.*;

import freemarker.core.Environment;
import freemarker.template.*;

/**
 * A template element that contains a nested block
 * that is transformed according to an instance of T
 * TemplateTransformModel
 */
public class TransformBlock extends TemplateElement {

    private Expression transformExpression;
    Map<String, Expression> namedArgs;

    /**
     * Creates new TransformBlock, with a given transformation
     */
    public TransformBlock(Expression transformExpression, 
                   Map<String,Expression> namedArgs,
                   TemplateElement nestedBlock) {
        this.transformExpression = transformExpression;
        this.namedArgs = namedArgs;
        this.nestedBlock = nestedBlock;
    }
    
    public Expression getTransformExpression() {
    	return transformExpression;
    }
    
    public Map getArgs() {
    	return namedArgs == null ? 
    			Collections.EMPTY_MAP :
    			Collections.unmodifiableMap(namedArgs);
    }

    public void execute(Environment env) throws TemplateException, IOException
    {
        TemplateTransformModel ttm = env.getTransform(transformExpression);
        if (ttm != null) {
            Map<String,TemplateModel> args = new HashMap<String,TemplateModel>();
            if (namedArgs != null && !namedArgs.isEmpty()) {
                for (Iterator it = namedArgs.entrySet().iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry) it.next();
                    String key = (String) entry.getKey();
                    Expression valueExp = (Expression) entry.getValue();
                    TemplateModel value = valueExp.getAsTemplateModel(env);
                    args.put(key, value);
                }
            } 
            env.render(nestedBlock, ttm, args);
        }
        else {
            TemplateModel tm = transformExpression.getAsTemplateModel(env);
            throw invalidTypeException(tm, transformExpression, env, "transform");
        }
    }

    public String getDescription() {
        return "transform " + transformExpression;
    }
}
