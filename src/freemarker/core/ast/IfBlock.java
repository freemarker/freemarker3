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
import freemarker.template.TemplateException;
import freemarker.core.Environment;

/**
 * A instruction that handles if-elseif-else blocks.
 * @author <A HREF="mailto:jon@revusky.com">Jonathan Revusky</A>
 */

public class IfBlock extends TemplateElement {

    public IfBlock(ConditionalBlock block)
    {
        nestedElements = new ArrayList<TemplateElement>();
        addBlock(block);
    }

    public void addBlock(ConditionalBlock block) {
        nestedElements.add(block);
    }
    
    public List<TemplateElement> getSubBlocks() {
    	return Collections.unmodifiableList(nestedElements);
    }
    
    public void execute(Environment env) throws TemplateException, IOException {
    	
        for (TemplateNode te : nestedElements) {
            ConditionalBlock cblock = (ConditionalBlock) te;
            Expression condition = cblock.getCondition();
            if (condition == null || condition.isTrue(env)) {
                if (cblock.getNestedBlock() != null) {
                    env.render(cblock);
                }
                return;
            }
        }
    }

    public String getDescription() {
        return "if-else ";
    }
}
