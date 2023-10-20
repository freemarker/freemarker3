package freemarker.core.ast;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import freemarker.template.Template;
import freemarker.core.Environment;
import freemarker.core.parser.ast.TemplateElement;

/**
 * A TemplateElement representing a block of plain text.
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

	private String text="";
	private int textType;
	private boolean ignore;

	public static final int PRINTABLE_TEXT = 0;
	public static final int WHITE_SPACE = 1;
	public static final int OPENING_WS = 2;
	public static final int TRAILING_WS = 3;

	public TextBlock() {}

	public TextBlock(String text) {
		this.text = text;
	}

	static boolean nonWS(int c) {
		return c != ' ' && c!='\t' && c!='\r' && c!='\n';
	}

	public void setLocation(Template template, int beginColumn, int beginLine, int endColumn, int endLine) {
        setTemplate(template);
		setTokenSource(template.getTokenSource());
		setBeginOffset(getOffset(beginLine, beginColumn));
		setEndOffset(getOffset(endLine, endColumn));
		if (text.chars().anyMatch(TextBlock::nonWS)) {
			this.textType = PRINTABLE_TEXT;
		}
		else {
			char lastChar = text.charAt(text.length()-1);
			boolean containsEOL = (lastChar == '\n' || lastChar == '\r');
			boolean containsStart = (beginColumn == 1);
			if (containsEOL && containsStart) this.textType = WHITE_SPACE;
			else if (!containsEOL && !containsStart) this.textType = WHITE_SPACE;
			else if (containsEOL) this.textType = TRAILING_WS;
			else this.textType = OPENING_WS;
		}
		this.text = null; // Now that we have location info, we don't need this. :-)
	}

	private int getOffset(int line, int column) {
		return getTokenSource().getLineStartOffset(line) + column -1;
	}
	
	public String getText() {
		return getSource();
	}

	public int getBlockType() {
		return textType;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	/**
	 * Simply outputs the text.
	 */
	public void execute(Environment env) throws IOException
	{
		if (this.ignore) return;
		Writer out = env.getOut();
	    String output = getTokenSource().getText(getBeginOffset(), getEndOffset()+1);
		out.write(output);
	}

	public String getDescription() {
		String s = getText().trim();
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
		if (text == null) return this.textType != PRINTABLE_TEXT;
		return text.chars().anyMatch(TextBlock::nonWS);
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
