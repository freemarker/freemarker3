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

/**
 * 
 */
package freemarker.ext.jsp;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.jsp.JspWriter;

import freemarker.template.utility.SecurityUtilities;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class JspWriterAdapter extends JspWriter {
    static final char[] NEWLINE = SecurityUtilities.getSystemProperty("line.separator").toCharArray();

    private final Writer out;
    
    JspWriterAdapter(Writer out) {
        super(0, true);
        this.out = out;
    }
    
    public String toString() {
        return "JspWriterAdapter wrapping a " + out.toString();
    }
    
    public void clear() throws IOException {
        throw new IOException("Can't clear");
    }

    public void clearBuffer() throws IOException {
        throw new IOException("Can't clear");
    }

    public void close() throws IOException {
        throw new IOException("Close not permitted.");
    }

    public void flush() throws IOException {
        out.flush();
    }

    public int getRemaining() {
        return 0;
    }

    public void newLine() throws IOException {
        out.write(NEWLINE);
    }

    public void print(boolean arg0) throws IOException {
        out.write(arg0 ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
    }

    public void print(char arg0) throws IOException
    {
        out.write(arg0);
    }

    public void print(char[] arg0) throws IOException
    {
        out.write(arg0);
    }

    public void print(double arg0) throws IOException
    {
        out.write(Double.toString(arg0));
    }

    public void print(float arg0) throws IOException
    {
        out.write(Float.toString(arg0));
    }

    public void print(int arg0) throws IOException
    {
        out.write(Integer.toString(arg0));
    }

    public void print(long arg0) throws IOException
    {
        out.write(Long.toString(arg0));
    }

    public void print(Object arg0) throws IOException
    {
        out.write(arg0 == null ? "null" : arg0.toString());
    }

    public void print(String arg0) throws IOException
    {
        out.write(arg0);
    }

    public void println() throws IOException
    {
        newLine();
    }

    public void println(boolean arg0) throws IOException
    {
        print(arg0);
        newLine();
    }

    public void println(char arg0) throws IOException
    {
        print(arg0);
        newLine();
    }

    public void println(char[] arg0) throws IOException
    {
        print(arg0);
        newLine();
    }

    public void println(double arg0) throws IOException
    {
        print(arg0);
        newLine();
    }

    public void println(float arg0) throws IOException
    {
        print(arg0);
        newLine();
    }

    public void println(int arg0) throws IOException
    {
        print(arg0);
        newLine();
    }

    public void println(long arg0) throws IOException
    {
        print(arg0);
        newLine();
    }

    public void println(Object arg0) throws IOException
    {
        print(arg0);
        newLine();
    }

    public void println(String arg0) throws IOException
    {
        print(arg0);
        newLine();
    }

    public void write(int c) throws IOException
    {
        out.write(c);
    }
    
    public void write(char[] arg0, int arg1, int arg2)
        throws IOException
    {
        out.write(arg0, arg1, arg2);
    }
}