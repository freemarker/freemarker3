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

package freemarker.template.utility;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import freemarker.template.*;
import freemarker.core.Environment;

/**
 * <p>A filter that compresses each sequence of consecutive whitespace
 * to a single line break (if the sequence contains a line break) or a
 * single space. In addition, leading and trailing whitespace is
 * completely removed.</p>
 * 
 * <p>Specify the transform parameter <code>single_line = true</code>
 * to always compress to a single space instead of a line break.</p>
 * 
 * <p>The default buffer size can be overridden by specifying a
 * <code>buffer_size</code> transform parameter (in bytes).</p>
 *
 * <p><b>Note:</b> The compress tag is implemented using this filter</p>
 * 
 * <p>Usage:<br />
 * From java:</p>
 * <pre>
 * SimpleHash root = new SimpleHash();
 *
 * root.put( "standardCompress", new freemarker.template.utility.StandardCompress() );
 *
 * ...
 * </pre>
 *
 * <p>From your FreeMarker template:</p>
 * <pre>
 * &lt;transform standardCompress&gt;
 *   &lt;p&gt;This    paragraph will have
 *       extraneous
 *
 * whitespace removed.&lt;/p&gt;
 * &lt;/transform&gt;
 * </pre>
 *
 * <p>Output:</p>
 * <pre>
 * &lt;p&gt;This paragraph will have
 * extraneous
 * whitespace removed.&lt;/p&gt;
 * </pre>
 * 
 * @version $Id: StandardCompress.java,v 1.14 2004/01/06 17:06:43 szegedia Exp $
 */

@Parameters("buffer_size=2048 single_line=false")

public class StandardCompress implements TemplateTransformModel, TemplateDirectiveModel {
    private static final String BUFFER_SIZE_KEY = "buffer_size";
    private static final String SINGLE_LINE_KEY = "single_line";
    private int defaultBufferSize;

    public static final StandardCompress INSTANCE = new StandardCompress();
    
    public StandardCompress()
    {
        this(2048);
    }
    
    /**
     * @param defaultBufferSize the default amount of characters to buffer
     */
    
    public StandardCompress(int defaultBufferSize)
    {
        this.defaultBufferSize = defaultBufferSize;
    }

    public void execute(Environment env, Map<String, TemplateModel> args, TemplateModel[] bodyVars, TemplateDirectiveBody body) 
    throws TemplateException, IOException {
    	if (body == null) return;
        int bufferSize = defaultBufferSize;
        boolean singleLine = false;
        if (args != null) {
            try {
                TemplateNumberModel num = (TemplateNumberModel)args.get(BUFFER_SIZE_KEY);
                if (num != null)
                    bufferSize = num.getAsNumber().intValue();
            } catch (ClassCastException e) {
                throw new TemplateModelException("Expecting numerical argument to " + BUFFER_SIZE_KEY);
            }
            try {
                TemplateBooleanModel flag = (TemplateBooleanModel)args.get(SINGLE_LINE_KEY);
                if (flag != null)
                    singleLine = flag.getAsBoolean();
            } catch (ClassCastException e) {
                throw new TemplateModelException("Expecting boolean argument to " + SINGLE_LINE_KEY);
            }
        }
        Writer compressWriter = new StandardCompressWriter(env.getOut(), bufferSize, singleLine);
        try {
            body.render(compressWriter);
        }
        finally {
            compressWriter.close();
        }
    }

    public Writer getWriter(final Writer out, Map<String, TemplateModel> args)
    throws TemplateModelException
    {
        int bufferSize = defaultBufferSize;
        boolean singleLine = false;
        if (args != null) {
            try {
                TemplateNumberModel num = (TemplateNumberModel)args.get(BUFFER_SIZE_KEY);
                if (num != null)
                    bufferSize = num.getAsNumber().intValue();
            } catch (ClassCastException e) {
                throw new TemplateModelException("Expecting numerical argument to " + BUFFER_SIZE_KEY);
            }
            try {
                TemplateBooleanModel flag = (TemplateBooleanModel)args.get(SINGLE_LINE_KEY);
                if (flag != null)
                    singleLine = flag.getAsBoolean();
            } catch (ClassCastException e) {
                throw new TemplateModelException("Expecting boolean argument to " + SINGLE_LINE_KEY);
            }
        }
        return new StandardCompressWriter(out, bufferSize, singleLine);
    }

    private static class StandardCompressWriter extends Writer
    {
        private static final int MAX_EOL_LENGTH = 2; // CRLF is two bytes
        
        private static final int AT_BEGINNING = 0;
        private static final int SINGLE_LINE = 1;
        private static final int INIT = 2;
        private static final int SAW_CR = 3;
        private static final int LINEBREAK_CR = 4;
        private static final int LINEBREAK_CRLF = 5;
        private static final int LINEBREAK_LF = 6;

        private final Writer out;
        private final char[] buf;
        private final boolean singleLine;
    
        private int pos = 0;
        private boolean inWhitespace = true;
        private int lineBreakState = AT_BEGINNING;

        StandardCompressWriter(Writer out, int bufSize, boolean singleLine) {
            this.out = out;
            this.singleLine = singleLine;
            buf = new char[bufSize];
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            for (;;) {
                // Need to reserve space for the EOL potentially left in the state machine
                int room = buf.length - pos - MAX_EOL_LENGTH; 
                if (room >= len) {
                    writeHelper(cbuf, off, len);
                    break;
                } else if (room <= 0) {
                    flushInternal();
                } else {
                    writeHelper(cbuf, off, room);
                    flushInternal();
                    off += room;
                    len -= room;
                }
            }
        }

        private void writeHelper(char[] cbuf, int off, int len) {
            for (int i = off, end = off + len; i < end; i++) {
                char c = cbuf[i];
                if (Character.isWhitespace(c)) {
                    inWhitespace = true;
                    updateLineBreakState(c);
                } else if (inWhitespace) {
                    inWhitespace = false;
                    writeLineBreakOrSpace();
                    buf[pos++] = c;
                } else {
                    buf[pos++] = c;
                }
            }
        }

        /*
          \r\n    => CRLF
          \r[^\n] => CR
          \r$     => CR
          [^\r]\n => LF
          ^\n     => LF
        */
        private void updateLineBreakState(char c)
        {
            switch (lineBreakState) {
            case INIT:
                if (c == '\r') {
                    lineBreakState = SAW_CR;
                } else if (c == '\n') {
                    lineBreakState = LINEBREAK_LF;
                }
                break;
            case SAW_CR:
                if (c == '\n') {
                    lineBreakState = LINEBREAK_CRLF;
                } else {
                    lineBreakState = LINEBREAK_CR;
                }
            }
        }

        private void writeLineBreakOrSpace()
        {
            switch (lineBreakState) {
            case SAW_CR:
                // whitespace ended with CR, fall through
            case LINEBREAK_CR:
                buf[pos++] = '\r';
                break;
            case LINEBREAK_CRLF:
                buf[pos++] = '\r';
                // fall through
            case LINEBREAK_LF:
                buf[pos++] = '\n';
                break;
            case AT_BEGINNING:
                // ignore leading whitespace
                break;
            case INIT:
            case SINGLE_LINE:
                buf[pos++] = ' ';
            }
            lineBreakState = (singleLine) ? SINGLE_LINE : INIT;
        }

        private void flushInternal() throws IOException {
            out.write(buf, 0, pos);
            pos = 0;
        }

        public void flush() throws IOException {
            flushInternal();
            out.flush();
        }

        public void close() throws IOException {
            flushInternal();
        }
    }
}
