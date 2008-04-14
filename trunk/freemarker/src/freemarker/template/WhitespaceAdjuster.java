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

public class WhitespaceAdjuster extends ASTVisitor {
	
	private Template template;
	private boolean stripWhitespace;
	
	public WhitespaceAdjuster(Template template) {
		this.template = template;
		stripWhitespace = template.stripWhitespace;
	}
	
	public void visit(TextBlock node) {
		
		int beginLine = node.getBeginLine();
		int beginColumn = node.getBeginColumn();
		int endLine = node.getEndLine();
		int endColumn = node.getEndColumn();
		String nodeText = node.getText();
		if (beginLine == endLine) {
			char lastChar = nodeText.charAt(nodeText.length()-1);
			if (lastChar == '\n' || lastChar == '\r') {
				if ((stripWhitespace && !template.lineDefinitelyProducesOutput(beginLine)) || template.lineSaysRightTrim(beginLine)) {
					int count = nodeText.length();
					while (count >0) {
						lastChar = nodeText.charAt(--count);
						if (!Character.isWhitespace(lastChar)) break;
					}
					nodeText = rightTrim(nodeText);
					node.setText(nodeText);
				}
			}
		}
		else {
			if ((stripWhitespace && !template.lineDefinitelyProducesOutput(endLine)) 
					|| template.lineSaysLeftTrim(endLine)) {
				String endLineText = template.getLine(endLine).substring(0, endColumn);
				if (node.unparsed) {
					int realEnd = endLineText.lastIndexOf('<');
					if (realEnd <0) realEnd = endLineText.lastIndexOf('[');
					endLineText = endLineText.substring(0, realEnd);
				}
				nodeText = nodeText.substring(0, nodeText.length() - endLineText.length());
				nodeText += leftTrim(endLineText);
				node.setText(nodeText);
			}
			if (nodeText.length() >0) {
				if ((stripWhitespace && !template.lineDefinitelyProducesOutput(beginLine)) 
						|| template.lineSaysRightTrim(beginLine)) {
					
					String firstLine = template.getLine(beginLine).substring(beginColumn -1);
					if (node.unparsed) {
						int realStart = firstLine.indexOf('>');
						if (realStart<0) realStart = firstLine.indexOf(']');
						firstLine = firstLine.substring(1+realStart);
					}
					nodeText = nodeText.substring(firstLine.length());
					nodeText = rightTrim(firstLine) + nodeText;
					node.setText(nodeText);
				}
			}
		}
		if (node.isIgnorable()) node.setText(""); //REVISIT
	}
	
	
	static private String rightTrim(String s) {
		int charsToStrip = 0;
		for (int i=s.length()-1; i>=0; i--) {
			if (Character.isWhitespace(s.charAt(i))) charsToStrip++;
			else break;
		}
		return s.substring(0, s.length() - charsToStrip);
	}
	
	static private String leftTrim(String s) {
		int charsToStrip = 0;
		for (int i=0; i<s.length(); i++) {
			if (Character.isWhitespace(s.charAt(i))) charsToStrip++;
			else break;
		}
		return s.substring(charsToStrip);
	}
}
