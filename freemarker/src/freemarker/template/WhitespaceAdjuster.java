/*
 * Copyright (c) 2008 The Visigoth Software Society. All rights
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

package freemarker.template;

import freemarker.core.ast.*;
import java.util.List;
import java.util.ArrayList;

public class WhitespaceAdjuster extends ASTVisitor {
	
	private Template template;
	
	public WhitespaceAdjuster(Template template) {
		this.template = template;
	}

	boolean ignoresSandwichedWhitespace(TemplateElement elem) {
		return (elem instanceof Macro) 
		       || (elem instanceof AssignmentInstruction) 
		       || (elem instanceof VarDirective)
		       || (elem instanceof LibraryLoad)
		       || (elem instanceof PropertySetting)
		       || (elem instanceof Comment);
 	}
	
	public void visit(MixedContent node) {
		boolean atTopLevel = node.getParent() == null;
		super.visit(node);
		List<TemplateElement> childElements = new ArrayList<TemplateElement>();
		TemplateElement prev = null;
		for (TemplateElement elem : node.getNestedElements()) {
			if (!elem.isIgnorable()) {
				childElements.add(elem);
			}
		}
		for (int i=0; i<childElements.size(); i++) {
			TemplateElement elem = childElements.get(i);
			TemplateElement previous = (i==0) ? null : childElements.get(i-1);
			TemplateElement next = (i == childElements.size() -1) ? null : childElements.get(i+1); 
			if (elem instanceof TextBlock) {
				TextBlock text = (TextBlock) elem;
				if (text.isWhitespace()) {
					if (ignoresSandwichedWhitespace(previous) && ignoresSandwichedWhitespace(next)) {
						text.setIgnore(true);
					}
					if (previous == null && atTopLevel  
							&& (node.getBeginColumn() != 1 || ignoresSandwichedWhitespace(next))) {
						text.setIgnore(true);
					}
					
					if (next == null && atTopLevel && ignoresSandwichedWhitespace(prev)) {
						text.setIgnore(true);
					}
				}
			}
		}
	}

	public void visit(TextBlock node) {
		int nodeType = node.getType();
		if (nodeType !=  TextBlock.OPENING_WS && nodeType != TextBlock.TRAILING_WS) return;
		int lineNumber = node.getBeginLine();
		boolean noTrim = template.lineSaysNoTrim(lineNumber);
		boolean inMacro = PostParseVisitor.getContainingMacro(node) != null;
		boolean ignorable = template.stripWhitespace && !template.isOutputtingLine(lineNumber, inMacro) && !noTrim; 
		if (nodeType == TextBlock.OPENING_WS) {
			boolean deliberateLeftTrim = template.lineSaysLeftTrim(lineNumber);
			if (ignorable || deliberateLeftTrim) {
				node.setIgnore(true);
			}
		}
		if (nodeType == TextBlock.TRAILING_WS) {
			boolean deliberateRightTrim = template.lineSaysRightTrim(lineNumber);
			if (ignorable || deliberateRightTrim) {
				node.setIgnore(true);
			}
		}
	}
}
