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

import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.parser.TemplateLocation;
import freemarker.core.helpers.DefaultReferenceChecker;
import freemarker.core.helpers.DefaultTreeDumper;


/**
 * Objects that represent instructions or expressions
 * in the compiled tree representation of the template
 * all descend from this abstract base class.
 */

public abstract class TemplateNode extends TemplateLocation {
	
	static private DefaultReferenceChecker referenceChecker = DefaultReferenceChecker.instance;
	static private DefaultTreeDumper canonicalTreeRenderer = new DefaultTreeDumper(false);
	
	TemplateNode parent;
	
	public String getSource() {
        if (template != null) {
            return template.getSource(getBeginColumn(), getBeginLine(), getEndColumn(), getEndLine());
        } else {
            return getCanonicalForm();
        }
    }

    public String toString() {
    	try {
    		return getSource();
    	} catch (Exception e) { // REVISIT: A bit of a hack? (JR)
    		return getCanonicalForm();
    	}
    }

    
    static public TemplateException invalidTypeException(TemplateModel model, Expression exp, Environment env, String expected)
    throws
        TemplateException
    {
        assertNonNull(model, exp, env);
        return new TemplateException(
            "Expected " + expected + ". " + 
            exp + " evaluated instead to " + 
            model.getClass().getName() + " " +
            exp.getStartLocation() + ".", env);
    }
    
    static public void assertNonNull(TemplateModel model, Expression exp, Environment env) throws InvalidReferenceException {
    	referenceChecker.assertNonNull(model, exp, env);
    }
    
    static public void assertIsDefined(TemplateModel model, Expression exp, Environment env) throws InvalidReferenceException {
    	referenceChecker.assertIsDefined(model, exp, env);
    }
    
    public final String getCanonicalForm() {
    	return canonicalTreeRenderer.render(this);
    }
    
    public TemplateNode getParentNode() {
    	return parent;
    }
}
