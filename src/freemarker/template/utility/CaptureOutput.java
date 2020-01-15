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

import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.core.Scope;

import java.io.*;
import java.util.Map;

/**
 * A transform that captures the output of a block of FTL code and stores that in a variable.
 *
 * <p>As this transform is initially present in the shared variable set, you can always
 * access it from the templates:</p>
 *
 * <pre>
 * &lt;@capture_output var="captured">
 *   ...
 * &lt;/@capture_output>
 * </pre>
 *
 * <p>And later in the template you can use the captured output:</p>
 *
 * ${captured}
 *
 * <p>This transform requires one of three parameters: <code>var</code>, <code>local</code>, or <code>global</code>.
 * Each of them specifies the name of the variable that stores the captured output, but the first creates a
 * variable in a name-space (as &lt;#assign>), the second creates a macro-local variable (as &lt;#local>),
 * and the last creates a global variable (as &lt;#global>).
 * </p>
 * <p>In the case of an assignment within a namespace, there is an optional parameter
 * <code>namespace</code> that indicates in which namespace to do the assignment.
 * if this is omitted, the current namespace is used, and this will be, by far, the most
 * common usage pattern.</p>
 *
 * @deprecated Use block-assignments instead, as <code>&lt;assign x>...&lt;/assign></code>.
 *
 * @version $Id: CaptureOutput.java,v 1.31 2004/01/06 17:06:43 szegedia Exp $
 */
public class CaptureOutput implements TemplateTransformModel {

    public Writer getWriter(final Writer out, final Map args) throws TemplateModelException {
        String errmsg = "Must specify the name of the variable in "
                + "which to capture the output with the 'var' or 'local' or 'global' parameter.";
        if (args == null) throw new TemplateModelException(errmsg);

        boolean local = false, global=false;
        final TemplateModel nsModel = (TemplateModel) args.get("namespace");
        Object varNameModel = args.get("var");
        if (varNameModel == null) {
            varNameModel = args.get("local");
            if (varNameModel == null) {
                varNameModel = args.get("global");
                global = true;
            } else {
                local = true;
            }
            if (varNameModel == null) {
                throw new TemplateModelException(errmsg);
            }
        }
        if (args.size()==2) {
            if (nsModel == null) {
                throw new TemplateModelException("Second parameter can only be namespace");
            }
            if (local) {
                throw new TemplateModelException("Cannot specify namespace for a local assignment");
            }
            if (global) {
                throw new TemplateModelException("Cannot specify namespace for a global assignment");
            }
            if (!(nsModel instanceof Scope)) {
                throw new TemplateModelException("namespace parameter does not specify a namespace. It is a " + nsModel.getClass().getName());
            }
        }
        else if (args.size() != 1) throw new TemplateModelException(
                "Bad parameters. Use only one of 'var' or 'local' or 'global' parameters.");

        if(!(varNameModel instanceof TemplateScalarModel)) {
            throw new TemplateModelException("'var' or 'local' or 'global' parameter doesn't evaluate to a string");
        }
        final String varName = ((TemplateScalarModel) varNameModel).getAsString();
        if(varName == null) {
            throw new TemplateModelException("'var' or 'local' or 'global' parameter evaluates to null string");
        }

        final StringBuilder buf = new StringBuilder();
        final Environment env = Environment.getCurrentEnvironment();
        final boolean localVar = local;
        final boolean globalVar = global;

        return new Writer() {

            public void write(char cbuf[], int off, int len) {
                buf.append(cbuf, off, len);
            }

            public void flush() throws IOException {
                out.flush();
            }

            public void close() throws IOException {
                SimpleScalar result = new SimpleScalar(buf.toString());
                try {
                    if (localVar) {
                        env.setLocalVariable(varName, result);
                    } else if (globalVar) {
                        env.setGlobalVariable(varName, result);
                    }
                    else {
                        if (nsModel == null) {
                            env.setVariable(varName, result);
                        } else {
                            ((Scope) nsModel).put(varName, result);
                        }
                    }
                } catch (java.lang.IllegalStateException ise) { // if somebody uses 'local' outside a macro
                    throw new IOException("Could not set variable " + varName + ": " + ise.getMessage());
                }
            }
        };
    }
}
