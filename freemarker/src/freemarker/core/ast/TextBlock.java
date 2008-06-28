/*
 * Copyright (C) 2003, 2008 The Visigoth Software Society. All rights
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
	static private String rightTrim(String s) {
		for (int i= s.length() -1; i>=0; i--) {
			char c = s.charAt(i);
			if (!isWhitespace(c)) {
				return s.substring(0, i+1);
			}
		}
		return "";
	}

	// We're using char[] instead of String for storing the text block because
	// Writer.write(String) involves copying the String contents to a char[] 
	// using String.getChars(), and then calling Writer.write(char[]). By
	// using Writer.write(char[]) directly, we avoid array copying on each 
	// write. 
	private char[] text;
	private int type;
	private boolean ignore;

	public static final int PRINTABLE_TEXT = 0;
	public static final int WHITE_SPACE = 1;
	public static final int OPENING_WS = 2;
	public static final int TRAILING_WS = 3;

	public TextBlock(String text) {
		this.text = text.toCharArray();
	}

	public void setLocation(Template template, int beginColumn, int beginLine, int endColumn, int endLine) {
		super.setLocation(template, beginColumn, beginLine, endColumn, endLine);
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

	public boolean isWhitespace() {
		if (text == null) return this.type != PRINTABLE_TEXT;
		for (char c : text) {
			if (!isWhitespace(c)) return false;
		}
		return true;
	}


	static private List<TextBlock> breakSingleLineIntoBlocks(String input, Template template, int column, int line) {
		List<TextBlock> result = new ArrayList<TextBlock>();
		char lastChar = input.charAt(input.length()-1);
		boolean spansRight = (lastChar == '\r' || lastChar == '\n');
		boolean spansLeft = (column == 1);
		boolean spansEntireLine = spansRight && spansLeft;
		boolean boundedBothSides = !spansRight && !spansLeft;
		if (spansEntireLine || boundedBothSides
				|| (spansLeft && !isWhitespace(input.charAt(0)))
		) 
		{
			TextBlock tb = new TextBlock(input);
			tb.setLocation(template, column, line, column + input.length() -1, line);
			result.add(tb);
			return result;
		}
		if (spansLeft) {
			String printablePart = leftTrim(input);
			String openingWS = input.substring(0, input.length() - printablePart.length());
			if (openingWS.length() >0) {
				TextBlock tb = new TextBlock(openingWS);
				tb.setLocation(template, column, line, openingWS.length(), line);
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
		String startingPart  = rightTrim(input);
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
	
	static public List<TextBlock> breakIntoBlocksLineByLine(String input, Template template, int beginColumn, int beginLine) throws ParseException {
		List<String> lines = lines(input);
		List<TextBlock> result = new ArrayList<TextBlock>();
		for (String line : lines) {
			String ltrim = leftTrim(line);
			int initWSLength = line.length() - ltrim.length();
			TextBlock tb = new TextBlock(line.substring(0, initWSLength));
			tb.setLocation(template, beginColumn, beginLine, beginColumn+initWSLength-1, beginLine);
			beginColumn += initWSLength;
			tb.type = TextBlock.OPENING_WS;
			result.add(tb);
			if (ltrim.length() >0) {
				String trimmed = rightTrim(ltrim);
				int trailingWSLength = ltrim.length() - trimmed.length();
				if (trimmed.length() >0) {
					tb = new TextBlock(trimmed);
					tb.setLocation(template, beginColumn, beginLine, beginColumn + trimmed.length() -1, beginLine);
					tb.type = TextBlock.PRINTABLE_TEXT;
					result.add(tb);
					beginColumn += trimmed.length();
				}
				if (trailingWSLength>0) {
					String trailingWS = ltrim.substring(trimmed.length());
					tb = new TextBlock(trailingWS);
					tb.setLocation(template, beginColumn, beginLine, beginColumn + trailingWSLength-1, beginLine); 
					tb.type = TextBlock.TRAILING_WS;
					result.add(tb);
				}
			}
			beginColumn = 1;
			beginLine++;
		}
		return result;
	}
	
	static public List<TextBlock> breakIntoBlocks(String input, Template template, int beginColumn, int beginLine) {
		int numLines = countLines(input);
		if (numLines == 1) {
			return breakSingleLineIntoBlocks(input, template, beginColumn, beginLine);
		}
		String firstLine = firstLine(input);
		String lastLine = lastLine(input);
		String middleLines = input.substring(firstLine.length(), input.length() - lastLine.length());

		List<TextBlock> result = new ArrayList<TextBlock>();

//		If the first line spans from column 1, we don't need to break it up. Otherwise we do.
		if (beginColumn == 1) {
			middleLines = input.substring(0, firstLine.length() + middleLines.length());
		}
		else {
			String firstPart = rightTrim(firstLine);
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
//		Now the middle lines, the ones in between the first and last line, are just all added as regular text
//		If the first line spans from the left, we prepend that.
//		If the last line spans to the end, we're cool. Also, if the last line has no opening whitespace, we are finished.			 
		boolean mergeLastLine = lastLine.endsWith("\n") || lastLine.endsWith("\r") || !isWhitespace(lastLine.charAt(0));
		if (mergeLastLine) {
			if (beginColumn ==1) {
				middleLines = input;
			} else {
				middleLines = input.substring(firstLine.length());
			}
		}
		if (middleLines.length() > 0) {
			TextBlock tb = new TextBlock(middleLines);
			int startingLine = beginLine;
			int startingColumn = 1;
			if (beginColumn != 1) ++startingLine;
			int endColumn = mergeLastLine ? lastLine.length() : lastLine(middleLines).length();
			int endingLine = beginLine + numLines -1;
			if (!mergeLastLine) endingLine--;
			tb.setLocation(template, 1, startingLine, endColumn, endingLine);
			result.add(tb);
		}
		if (!mergeLastLine) {
			String printablePart = leftTrim(lastLine);
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

	static private boolean isWhitespace(char c) {
		return c == '\n' || c == '\r' || c == '\t' || c == ' ';
	}

	static private int countLines(String input) {
		int result = 0;
		char lastChar = 0;
		for (int i=0; i<input.length();i++) {
			char c = input.charAt(i);
			if (c=='\r') ++result;
			else if (c=='\n'  && lastChar != '\r') ++result;
			lastChar = c;
		}
		if (lastChar != '\n' && lastChar != '\r') ++result;
		return result;
	}

	static private String firstLine(String input) {
		for (int i=0; i<input.length(); i++) {
			char c = input.charAt(i);
			if (c == '\n') {
				return input.substring(0, i+1);
			}
			if (c=='\r') {
				if (input.length() == i+1) {
					return input;
				}
				int endLine = (input.charAt(i+1) == '\n') ? i+1 : i;
				return input.substring(0, endLine +1);
			}
		}
		return input;
	}

	static private String lastLine(String input) {
		int lineLength = input.length();
		if (lineLength <2) return input;
		int startBackTrack = lineLength-1;
		char lastChar = input.charAt(startBackTrack);
		if (lastChar == '\r' || lastChar=='\n') startBackTrack--;
		if (lastChar =='\n') {
			if (input.charAt(startBackTrack) == '\r') startBackTrack--;
		}
		for (int i=startBackTrack; i>=0; i--) {
			char c = input.charAt(i);
			if (c == '\n' || c=='\r') {
				return input.substring(i+1);
			}
		}
		return input;
	}
	
	static private List<String> lines(String input) {
		List<String> result = new ArrayList<String>();
		while (input.length() >0) {
			String line = firstLine(input);
			result.add(line);
			input = input.substring(line.length());
		}
		return result;
	}


	static private String leftTrim(String s) {
		for (int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			if (!isWhitespace(c)) {
				return s.substring(i);
			}
		}
		return "";
	}
}
