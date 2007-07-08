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
import freemarker.template.Template;
import freemarker.core.Environment;

/**
 * A TemplateElement representing a block of plain text.
 * @version $Id: TextBlock.java,v 1.17 2004/01/06 17:06:42 szegedia Exp $
 */
public final class TextBlock extends TemplateElement {
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];
    static public final TextBlock EMPTY_BLOCK = new TextBlock(EMPTY_CHAR_ARRAY, false);
    // We're using char[] instead of String for storing the text block because
    // Writer.write(String) involves copying the String contents to a char[] 
    // using String.getChars(), and then calling Writer.write(char[]). By
    // using Writer.write(char[]) directly, we avoid array copying on each 
    // write. 
    private char[] text;
    public final boolean unparsed;
    
    private int originalBeginLine=-1, originalBeginColumn, originalEndLine, originalEndColumn;
    
    public TextBlock(String text) {
        this(text, false);
    }

    public TextBlock(String text, boolean unparsed) {
        this(text.toCharArray(), unparsed);
    }

    private TextBlock(char[] text, boolean unparsed) {
        this.text = text;
        this.unparsed = unparsed;
    }
    
    public String getText() {
    	return new String(text);
    }
    
    /**
     * Simply outputs the text.
     */
    public void execute(Environment env) 
    throws IOException
    {
        env.getOut().write(text);
    }

    public String getDescription() {
        String s = new String(text).trim();
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
    
    public String getSource() {
    	Template template = getTemplate();
    	if (originalBeginLine == -1 || template == null) {
    		return super.getSource();
    	} else {
    		return template.getSource(originalBeginColumn, originalBeginLine, originalEndColumn, originalEndLine);
    	}
    }
    
    public void postParseCleanup(boolean stripWhitespace) {
        if (text.length == 0) return;
    	this.originalBeginColumn = this.beginColumn;
    	this.originalBeginLine = this.beginLine;
    	this.originalEndColumn = this.endColumn;
    	this.originalEndLine = this.endLine;
        int openingCharsToStrip = 0, trailingCharsToStrip=0;
        boolean deliberateLeftTrim = deliberateLeftTrim();
        boolean deliberateRightTrim = deliberateRightTrim();
        if (!stripWhitespace || text.length == 0 ) {
            return;
        }
        if (parent.getParent() == null && previousSibling() == null) return;
        if (!deliberateLeftTrim) {
            trailingCharsToStrip = trailingCharsToStrip();
        }
        if (!deliberateRightTrim) {
            openingCharsToStrip = openingCharsToStrip();
        }
        if (openingCharsToStrip == 0 && trailingCharsToStrip == 0) {
            return;
        }
        this.text = substring(text, openingCharsToStrip, text.length - trailingCharsToStrip);
        if (openingCharsToStrip > 0) {
            this.beginLine++;
            this.beginColumn = 1;
        }
        if (trailingCharsToStrip >0) {
            this.endColumn = 0;
        }
    }
    
    /**
     * Scans forward the nodes on the same line to see whether there is a 
     * deliberate left trim in effect. Returns true if the left trim was present.
     */
    private boolean deliberateLeftTrim() {
        boolean result = false;
        for (TemplateElement elem = this.nextTerminalNode(); 
             elem != null && elem.getBeginLine() == this.getEndLine();
             elem = elem.nextTerminalNode()) 
        {
            if (elem instanceof TrimInstruction) {
                TrimInstruction ti = (TrimInstruction) elem;
                if (!ti.left && !ti.right) {
                    result = true;
                }
                if (ti.left) {
                    result = true;
                    int lastNewLineIndex = lastNewLineIndex();
                    if (lastNewLineIndex >=0  || beginColumn == 1) {
                        char[] firstPart = substring(text, 0, lastNewLineIndex + 1);
                        char[] lastLine = substring(text, 1+lastNewLineIndex); 
                        if (trim(lastLine).length == 0) {
                            this.text = firstPart;
                            this.endColumn = 0;
                        } else {
                            int i =0;
                            while (Character.isWhitespace(lastLine[i])) {
                                i++;
                            }
                            char[] printablePart = substring(lastLine, i);
                            this.text = concat(firstPart, printablePart);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Checks for the presence of a t or rt directive on the 
     * same line. Returns true if the right trim directive was present.
     */
    private boolean deliberateRightTrim() {
        boolean result = false;
        for (TemplateElement elem = this.prevTerminalNode(); 
             elem != null && elem.endLine == this.beginLine;
             elem = elem.prevTerminalNode())
        {
            if (elem instanceof TrimInstruction) {
                TrimInstruction ti = (TrimInstruction) elem;
                if (!ti.left && !ti.right) {
                    result = true;
                }
                if (ti.right) {
                    result = true;
                    int firstLineIndex = firstNewLineIndex() +1;
                    if (firstLineIndex == 0) {
                        return false;
                    }
                    if (text.length > firstLineIndex 
                        && text[firstLineIndex-1] == '\r' 
                        && text[firstLineIndex] == '\n') 
                    {
                        firstLineIndex++;
                    }
                    char[] trailingPart = substring(text, firstLineIndex);
                    char[] openingPart = substring(text, 0, firstLineIndex);
                    if (trim(openingPart).length ==0) {
                        this.text = trailingPart;
                        this.beginLine++;
                        this.beginColumn=1;
                    } else {
                        int lastNonWS = openingPart.length -1;
                        while (Character.isWhitespace(text[lastNonWS])) {
                            lastNonWS--;
                        }
                        char[] printablePart = substring(text, 0, lastNonWS+1);
                        if (trim(trailingPart).length == 0) {
                        // THIS BLOCK IS HEINOUS! THERE MUST BE A BETTER WAY! REVISIT (JR)
                            boolean trimTrailingPart = true;
                            for (TemplateElement te = this.nextTerminalNode(); 
                                 te != null && te.beginLine == this.endLine;
                                 te = te.nextTerminalNode()) 
                            {
                                if (te.heedsOpeningWhitespace()) 
                                {
                                    trimTrailingPart = false;
                                }
                                if (te instanceof TrimInstruction && ((TrimInstruction) te).left) {
                                    trimTrailingPart = true;
                                    break;
                                }
                            }
                            if (trimTrailingPart) trailingPart = EMPTY_CHAR_ARRAY;
                        }
                        this.text = concat(printablePart, trailingPart);
                    }
                }
            }
        }
        return result;
    }
/*
    private String leftTrim(String s) {
        int i =0;
        while (i<s.length()) {
            if (!Character.isWhitespace(s.charAt(i)))
                break;
            ++i;
        }
        return s.substring(i);
    }
*/
    private int firstNewLineIndex() {
        String content = new String(text);
        int newlineIndex1 = content.indexOf('\n');
        int newlineIndex2 = content.indexOf('\r');
        int result = newlineIndex1 >=0 ? newlineIndex1 : newlineIndex2;
        if (newlineIndex1 >=0 && newlineIndex2 >=0) {
            result = Math.min(newlineIndex1, newlineIndex2);
        }
        return result;
    }

    private int lastNewLineIndex() {
        String content = new String(text);
        return Math.max(content.lastIndexOf('\r'), content.lastIndexOf('\n'));
    }

    /**
     * figures out how many opening whitespace characters to strip
     * in the post-parse cleanup phase.
     */
    private int openingCharsToStrip() {
        int newlineIndex = firstNewLineIndex();
        if (newlineIndex == -1 && beginColumn != 1) {
            return 0;
        }
        ++newlineIndex;
        if (text.length > newlineIndex) {
            if (newlineIndex >0 && text[newlineIndex-1] == '\r' && text[newlineIndex] == '\n') {
                ++newlineIndex;
            }
        }
        if (new String(text).substring(0, newlineIndex).trim().length() >0) {
            return 0;
        }
// We look at the preceding elements on the line to see if we should
// strip the opening newline and any whitespace preceding it.
        for (TemplateElement elem = this.prevTerminalNode(); 
             elem != null && elem.endLine == this.beginLine;
             elem = elem.prevTerminalNode())
        {
            if (elem.heedsOpeningWhitespace())
            {
                return 0;
            }
        }
        return newlineIndex;
    }

    /**
     * figures out how many trailing whitespace characters to strip
     * in the post-parse cleanup phase.
     */
    private int trailingCharsToStrip() {
        String content = new String(text);
        int lastNewlineIndex = lastNewLineIndex();
        if (lastNewlineIndex == -1 && beginColumn != 1) {
            return 0;
        }
        String substring = content.substring(lastNewlineIndex +1);
        if (substring.trim().length() >0) {
            return 0;
        }
// We look at the elements afterward on the same line to see if we should
// strip any whitespace after the last newline
        for (TemplateElement elem = this.nextTerminalNode(); 
             elem != null && elem.beginLine == this.endLine;
             elem = elem.nextTerminalNode()) 
        {
            if (elem.heedsTrailingWhitespace()) 
            {
                return 0;
            }
        }
        return substring.length();
    }

    boolean heedsTrailingWhitespace() {
        if (isIgnorable()) {
            return false;
        }
        for (int i=0; i<text.length; i++) {
            char c = text[i];
            if (c=='\n' || c=='\r') {
                return false;
            }
            if (!Character.isWhitespace(c)) {
                return true;
            }
        }
        return true;
    }

    boolean heedsOpeningWhitespace() {
        if (isIgnorable()) {
            return false;
        }
        for (int i = text.length -1; i>=0; i--) {
            char c = text[i];
            if (c == '\n' || c == '\r') {
                return false;
            }
            if (!Character.isWhitespace(c)) {
                return true;
            }
        }
        return true;
    }

    boolean isIgnorable() {
        if (text == null || text.length == 0) {
            return true;
        }
        if (!isWhitespace()) {
            return false;
        }
        boolean atTopLevel = (getParent().getParent() == null);
        TemplateElement prevSibling = previousSibling();
        TemplateElement nextSibling = nextSibling();
        return ((prevSibling == null && atTopLevel) || nonOutputtingType(prevSibling))
              && ((nextSibling == null && atTopLevel) || nonOutputtingType(nextSibling));
    }
    

    private boolean nonOutputtingType(TemplateElement element) {
        return (element instanceof Macro ||
                element instanceof AssignmentInstruction ||
                element instanceof PropertySetting ||
                element instanceof LibraryLoad ||
                element instanceof Comment);
    }

    private static char[] substring(char[] c, int from, int to) {
        char[] c2 = new char[to - from];
        System.arraycopy(c, from, c2, 0, c2.length);
        return c2;
    }
    
    private static char[] substring(char[] c, int from) {
        return substring(c, from, c.length);
    }
    
    private static char[] trim(char[] c) {
        if (c.length == 0) {
            return c;
        }
        return new String(c).trim().toCharArray();
    }
    
    private static char[] concat(char[] c1, char[] c2) {
        char[] c = new char[c1.length + c2.length];
        System.arraycopy(c1, 0, c, 0, c1.length);
        System.arraycopy(c2, 0, c, c1.length, c2.length);
        return c;
    }
    
    boolean isWhitespace() {
        return text == null || trim(text).length == 0;
    }
}
