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
