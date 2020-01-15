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

package freemarker.core;

import java.io.PrintWriter;
import java.io.PrintStream;

import freemarker.template.TemplateException;

/**
 * This exception is thrown when a &lt;stop&gt;
 * directive is encountered. 
 */

public class StopException extends TemplateException
{
    private static final long serialVersionUID = 5500173581027893102L;

    public StopException(Environment env) {
        super(env);
    }

    public StopException(Environment env, String s) {
        super(s, env);
    }

    public void printStackTrace(PrintWriter pw) {
        String msg = this.getMessage();
        pw.print("Encountered stop instruction");
        if (msg != null && !msg.equals("")) {
            pw.println("\nCause given: " + msg);
        } else pw.println();
        super.printStackTrace(pw);
    }

    public void printStackTrace(PrintStream ps) {
        String msg = this.getMessage();
        ps.print("Encountered stop instruction");
        if (msg != null && !msg.equals("")) {
            ps.println("\nCause given: " + msg);
        } else ps.println();
        super.printStackTrace(ps);
    }
}


