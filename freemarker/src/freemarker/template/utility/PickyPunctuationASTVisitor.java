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

package freemarker.template.utility;

import freemarker.core.ast.*;
import java.util.StringTokenizer;
import java.util.regex.*;

/**
 * A visitor that runs over the text blocks in a template
 * and replaces certain characters by HTML entities. 
 * It replaces -- with a proper em dash &mdash; 
 * and replaces single and double quotes with their proper
 * opening and closing curly quotes (&ldquo; &rdquo; &lsquo; &rsquo;
 * Note that this ASTVisitor implementation implements Cloneable.
 * That is to indicate that it is not threadsafe and that it is necessary
 * to clone a new one rather than use an existing one when you are
 * in a multithreaded environment.
 * 
 *  The reason this class needs to maintain state (and hence, not be threadsafe)
 *  is because it needs internal variables to keep track of whether we are in
 *  a singlequote or doublequote. This is necessary because a double quote or
 *  quote can open in one textblock and be closed in another, for example
 *  in the fragment:
 *  
 *  He told me "My name is ${name}." 
 *  <P>the quote is opened and then closed in a separate TextBlock instance.

 * @author Jonathan Revusky
 */

public class PickyPunctuationASTVisitor extends ASTVisitor implements Cloneable {
	
	private boolean inSingleQuote, inDoubleQuote;
	static private Pattern posessivePattern = Pattern.compile("\\w+'\\w+");
	
	public void visit(TextBlock block) {
		String text = block.getText();
		text = dealWithPossessives(text);
		text = text.replace("--", "&mdash;");
		text = text.replace("<==>", "&darr");
		text = text.replace("==>", "&rarr");
		text = text.replace("<==", "&larr;");
		text = useProperQuotationMarks(text);
		block.setText(text);
	}
	
	private String dealWithPossessives(String input) {
		StringBuilder buf = new StringBuilder();
		String[] seq = posessivePattern.split(input);
		for (int i= 0; i<seq.length; i++) {
			String s = seq[i];
			buf.append(s);
			if (i != seq.length -1) {
				buf.append("&rsquo;");
			}
		}
		return buf.toString();
	}
	
	private String useProperQuotationMarks(String input) {
		StringTokenizer st = new StringTokenizer(input, "\'\"", true);
		StringBuilder buf = new StringBuilder();
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.equals("\"")) {
				if (inDoubleQuote) {
					buf.append("&rdquo;"); // Apparently in older browsers this is &#147; but I don't know how old...
				} else {
					buf.append("&ldquo;"); 
				}
				inDoubleQuote = !inDoubleQuote;
			}
			else if (tok.equals("'")) {
				if (inDoubleQuote) {
					buf.append("&rsquo;");
				} else {
					buf.append("&lsquo;");
				}
				inSingleQuote = !inSingleQuote;
			}
			else buf.append(tok);
		}
		return buf.toString();
	}
}
