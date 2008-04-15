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

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import freemarker.template.Template;
import freemarker.template.utility.StringUtil;
import freemarker.core.Environment;
import freemarker.core.parser.ParseException;

/**
 * A TemplateElement representing a block of plain text.
 * @version $Id: TextBlock.java,v 1.17 2004/01/06 17:06:42 szegedia Exp $
 */
public final class TextBlock extends TemplateElement {
	static public final TextBlock EMPTY_BLOCK = new TextBlock("");
	static {
		EMPTY_BLOCK.ignore = true;
	}
	// We're using char[] instead of String for storing the text block because
	// Writer.write(String) involves copying the String contents to a char[] 
	// using String.getChars(), and then calling Writer.write(char[]). By
	// using Writer.write(char[]) directly, we avoid array copying on each 
	// write. 
	private char[] text;
	private int type;
	private boolean ignore;
	public final boolean unparsed;

	public static final int PRINTABLE_TEXT = 0;
	public static final int WHITE_SPACE = 1;
	public static final int OPENING_WS = 2;
	public static final int TRAILING_WS = 3;

	public TextBlock(String text) {
		this.text = text.toCharArray();
		this.unparsed = false;
	}

	public TextBlock(String text, boolean unparsed) {
		this.text = text.toCharArray();
		this.unparsed = unparsed;
	}
	
	public void setLocation(Template template, int beginColumn, int beginLine, int endColumn, int endLine) {
		super.setLocation(template, beginColumn, beginLine, endColumn, endLine);
		if (!unparsed) { // REVISIT, deal with this case later
			boolean printable = false;
			for (char c : text){
				if (c != ' ' && c!='\t' && c!='\r' && c!='\n') printable = true;
			}
			if (printable) {
				this.type = PRINTABLE_TEXT;
			}
			else {
				char lastChar = text[text.length -1];
				boolean containsEOL = (lastChar == '\n' || lastChar == '\r');
				boolean containsStart = (beginColumn == 1);
				if (containsEOL && containsStart) this.type = WHITE_SPACE;
				else if (!containsEOL && !containsStart) this.type = WHITE_SPACE;
				else if (containsEOL) this.type = TRAILING_WS;
				else this.type = OPENING_WS;
			}
			this.text = null; // Now that we have location info, we don't need this. :-)
		}
	}

	public String getText() {
		return text != null ? new String(text) : getSource();
	}

	public void setText(String text) {
		this.text = text.toCharArray();
	}
	
	public int getType() {
		return type;
	}
	
	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	/**
	 * Simply outputs the text.
	 */
	public void execute(Environment env) 
  	throws IOException
	{
		 if (this.ignore) return;
		 Writer out = env.getOut();
		 if (text != null) {
			 out.write(text);
		 }
		 else {
			 template.writeTextAt(out, beginColumn, beginLine, endColumn, endLine);
		 }
	}

	 public String getDescription() {
		 String s = new String(getText()).trim();
		 if (s.length() == 0) {
			 return "whitespace";
		 }
		 if (s.length() > 20) {
			 s = s.substring(0,20) + "...";
			 s = s.replace('\n', ' ');
			 s = s.replace('\r', ' ');
		 }
		 return "text block (" + s + ")";
	 }

	 public boolean isIgnorable() {
		 return ignore;
	 }


	 private boolean nonOutputtingType(TemplateNode element) {
		 return (element instanceof Macro ||
				 element instanceof AssignmentInstruction ||
				 element instanceof VarDirective ||
				 element instanceof PropertySetting ||
				 element instanceof LibraryLoad ||
				 element instanceof Comment);
	 }

	 public boolean isWhitespace() {
		 if (text == null) return this.type != PRINTABLE_TEXT;
		 for (char c : text) {
			 if (!isWhitespace(c)) return false;
		 }
		 return true;
	 }
	 
	 static public boolean isWhitespace(char c) {
		 return c == '\n' || c == '\r' || c == '\t' || c == ' ';
	 }


	 static private List<String> breakIntoLines(String input) {
		 List<String> result = new ArrayList<String>();
		 StringTokenizer st = new StringTokenizer(input, "\r\n", true);
		 String lastToken = "";
		 String currentLine = "";
		 while (st.hasMoreTokens()) {
			 String tok = st.nextToken();
			 if (tok.equals("\r")) {
				 currentLine += tok;
				 result.add(currentLine);
				 currentLine = "";
			 } 
			 else if (tok.equals("\n")) {
				 if (lastToken.equals("\r")) {
					 String line = result.get(result.size() -1) + tok;
					 result.set(result.size() -1, line);
				 } else {
					 currentLine += tok;
					 result.add(currentLine);
				 }
				 currentLine = "";
			 }
			 else {
				 currentLine = tok;
				 if (!st.hasMoreTokens()) result.add(currentLine);
			 }
			 lastToken = tok;
		 }
		 return result;
	 }
	 
	 static private List<TextBlock> breakSingleLineIntoBlocks(String input, Template template, int column, int line) throws ParseException {
		 List<TextBlock> result = new ArrayList<TextBlock>();
		 char lastChar = input.charAt(input.length()-1);
		 boolean spansRight = (lastChar == '\r' || lastChar == '\n');
		 boolean spansLeft = (column == 1);
		 boolean spansEntireLine = spansRight && spansLeft;
		 boolean boundedBothSides = !spansRight && !spansLeft;
		 if (spansEntireLine || boundedBothSides
				 || (spansLeft && !Character.isWhitespace(input.charAt(0)))
		 ) 
		 {
			 TextBlock tb = new TextBlock(input);
			 tb.setLocation(template, column, line, column + input.length() -1, line);
			 result.add(tb);
			 return result;
		 }
		 if (spansLeft) {
			 String printablePart = StringUtil.leftTrim(input);
			 String openingWS = input.substring(0, input.length() - printablePart.length());
			 if (openingWS.length() >0) {
				 TextBlock tb = new TextBlock(openingWS);
				 tb.setLocation(template, column, line, openingWS.length()-1, line);
				 result.add(tb);
			 }
			 if (printablePart.length() >0) {
				 TextBlock tb = new TextBlock(printablePart);
				 tb.setLocation(template, column + openingWS.length(), line, column + input.length() -1, line);
				 result.add(tb);
			 }
			 return result;
		 }
		 // Remaining case is a line that has trailing WS.
		 String startingPart  = StringUtil.rightTrim(input);
		 String trailingWS = input.substring(startingPart.length());
		 if (startingPart.length() >0) {
			 TextBlock tb = new TextBlock(startingPart);
			 tb.setLocation(template, column, line, column + startingPart.length() -1, line);
			 result.add(tb);
		 }
		 if (trailingWS.length()>0) {
			 TextBlock tb = new TextBlock(trailingWS);
			 tb.setLocation(template, column + startingPart.length(), line, column + input.length() -1, line);
			 result.add(tb);
		 }
		 return result;
	 }

	 static public List<TextBlock> breakIntoBlocks(String input, Template template, int beginColumn, int beginLine) throws ParseException {
		 List<String> lines = breakIntoLines(input);
		 int numLines = lines.size(); 
		 String firstLine = lines.get(0);
		 String lastLine = lines.get(numLines-1);

		 List<TextBlock> result = new ArrayList<TextBlock>();

		 // Deal with single line.
		 if (numLines == 1) {
			 return breakSingleLineIntoBlocks(firstLine, template, beginColumn, beginLine);
		 }
		 else { // Now deal with multiline case:
// If the first line spans from column 1, we don't need to break it up. Otherwise we do.
			 if (beginColumn > 1 ) {
				 String firstPart = StringUtil.rightTrim(firstLine);
				 String trailingWS = firstLine.substring(firstPart.length());
				 if (firstPart.length() >0) {
					 TextBlock tb = new TextBlock(firstPart);
					 int type = firstPart.trim().length() == 0 ? WHITE_SPACE : PRINTABLE_TEXT;
					 tb.setLocation(template, beginColumn, beginLine, beginColumn + firstPart.length() -1, beginLine);
					 result.add(tb);
				 }
				 if (trailingWS.length() >0) {
					 TextBlock tb = new TextBlock(trailingWS);
					 tb.setLocation(template, beginColumn + firstPart.length(), beginLine, beginColumn + firstLine.length() -1, beginLine);
					 result.add(tb);
				 }
			 }
// Now the middle lines, the ones in between the first and last line, are just all added as regular text
			 StringBuilder middleLines = new StringBuilder();
// If the first line spans from the left, we prepend that.
			 if (beginColumn == 1) {
				 middleLines.append(firstLine);
			 }
// Now we append the middle lines. Of course, if numLines is 2, then this is a no-op.			 
			 for (int i=1; i< numLines -1; i++) {
				 middleLines.append(lines.get(i));
			 }
// Now the last line:
// If the last line spans to the end, we're cool. Also, if the last line has no opening whitespace, we are finished.			 
			 boolean mergeLastLine = lastLine.endsWith("\n") || 
			                    lastLine.endsWith("\r") || 
			                    !Character.isWhitespace(lastLine.charAt(0));
			 if (mergeLastLine) { 
				 middleLines.append(lastLine);
			 }
			 if (middleLines.length() > 0) {
				 TextBlock tb = new TextBlock(middleLines.toString());
				 int startingLine = beginLine;
				 int startingColumn = 1;
				 if (beginColumn != 1) ++startingLine;
				 int endColumn = mergeLastLine ? lastLine.length() : lines.get(lines.size() -2).length();
				 int endingLine = beginLine + numLines -1;
				 if (!mergeLastLine) endingLine--;
				 tb.setLocation(template, 1, startingLine, endColumn, endingLine);
				 result.add(tb);
			 }
			 if (!mergeLastLine) {
				 String printablePart = StringUtil.leftTrim(lastLine);
				 String openingWS= lastLine.substring(0, lastLine.length() - printablePart.length());
				 if (openingWS.length() >0) {
					 TextBlock tb = new TextBlock(openingWS);
					 tb.setLocation(template, 1, beginLine + numLines -1, openingWS.length(), beginLine + numLines -1);
					 result.add(tb);
				 }
				 if (printablePart.length()>0) {
					 TextBlock tb = new TextBlock(printablePart);
					 tb.setLocation(template, 1 + openingWS.length(), beginLine + numLines -1, lastLine.length(), beginLine + numLines -1);
					 result.add(tb);
				 }
			 } // Now we really are finished!
			 return result;
		 }
	 }
}
