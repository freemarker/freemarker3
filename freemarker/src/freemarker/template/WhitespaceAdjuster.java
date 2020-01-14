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
		if (nodeType !=  TextBlock.OPENING_WS && nodeType != TextBlock.TRAILING_WS) {
			return;
		}
		int lineNumber = node.getBeginLine();
		if (template.lineSaysNoTrim(lineNumber)) {
			return;
		}
		boolean inMacro = PostParseVisitor.getContainingMacro(node) != null;
		boolean ignorable = template.stripWhitespace && !template.isOutputtingLine(lineNumber, inMacro);
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
