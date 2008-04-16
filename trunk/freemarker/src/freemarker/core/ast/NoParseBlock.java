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

package freemarker.core.ast;

import java.io.IOException;
import java.io.Writer;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.Template;

public class NoParseBlock extends TemplateElement {
	
	private char[] text;
	private int textBeginColumn, textBeginLine, textEndColumn, textEndLine;
	private String startTag, endTag;

	public NoParseBlock(String startTag, String endTag, String text) {
		this.text = text.toCharArray();
		this.startTag = startTag;
		this.endTag = endTag;
	}
	
	public String getStartTag() {
		return startTag;
	}
	
	public String getEndTag() {
		return endTag;
	}

	public void setLocation(Template template, int beginColumn, int beginLine, int endColumn, int endLine) {
		super.setLocation(template, beginColumn, beginLine, endColumn, endLine);
		textBeginColumn = beginColumn;
		textBeginLine = beginLine;
		textEndColumn = endColumn;
		textEndLine = endLine;
		char lastChar = 0;
		for (char c : startTag.toCharArray()) {
			switch (c) {
			    case '\r' : 
			    	textBeginLine++; 
			    	textBeginColumn = 1;
			    	break;
				case '\n' : 
					if (lastChar!='\r') {
						textBeginLine++;
						textBeginColumn = 1;
					}
					break;
				default : textBeginColumn++;

			}
			lastChar = c;
		}
		lastChar = 0;
		for (int i=endTag.length() -1; i>=0; i--) {
			char c = endTag.charAt(i);
			switch (c) {
			   case '\n' : 
				   textEndLine--;
				   textEndColumn = template.getLine(textEndLine).length();
				   break;
			   case '\r' : 
				   if (lastChar != '\r') {
					   textEndLine--;
					   textEndColumn = template.getLine(textEndLine).length();
				   }
				   break;
			   default : textEndColumn--;

			}
			lastChar = c;
		}
		this.text = null;
	}
	
	public String getText() {
		if (text != null) return new String(text);
		return template.getSource(textBeginColumn, textBeginLine, textEndColumn, textEndLine);
	}
	
	
	public void execute(Environment env) throws TemplateException, IOException {
		Writer out = env.getOut();
		if (text != null) {
			out.write(text);
		} else {
			template.writeTextAt(out, textBeginColumn, textBeginLine, textEndColumn, textEndLine);
		}
	}
	
	public boolean isIgnorable() {
		return getText().trim().length() == 0;
	}
}
