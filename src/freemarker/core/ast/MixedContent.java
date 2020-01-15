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
 * Encapsulates an array of <tt>TemplateElement</tt> objects. 
 */
public class MixedContent extends TemplateElement implements Iterable<TemplateElement>{

    public MixedContent()
    {
        nestedElements = new ArrayList<TemplateElement>();
    }

    public void addElement(TemplateElement element) {
        nestedElements.add(element);
    }
    
    void prependElement(TemplateElement element) {
        element.setParent(this);
        List<TemplateElement> newList = new ArrayList<TemplateElement>();
        newList.add(element);
        for (TemplateElement te : nestedElements) {
            newList.add(te);
        }
        this.nestedElements = newList;
    }

    public Iterator<TemplateElement> iterator() {
    	return nestedElements.iterator();
    }

    /**
     * Processes the contents of the internal <tt>TemplateElement</tt> list,
     * and outputs the resulting text.
     */
    public void execute(Environment env) 
        throws TemplateException, IOException 
    {
        for (int i=0; i<nestedElements.size(); i++) {
            TemplateElement element = nestedElements.get(i);
            env.render(element);
        }
    }

    public String getDescription() {
        if (parent == null) {
            return "root element";
        }
        return "content"; // MixedContent is uninteresting in a stack trace.
    }

    public boolean isIgnorable() {
        return nestedElements == null || nestedElements.size() == 0;
    }
}
