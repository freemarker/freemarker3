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

package freemarker.testcase.models;

import java.util.List;
import freemarker.template.*;

/**
 * A simple method model used as a test bed.
 *
 * @version $Id: SimpleTestMethod.java,v 1.12 2004/01/06 17:06:44 szegedia Exp $
 */
public class SimpleTestMethod implements TemplateMethodModel {

    /**
     * Executes a method call.
     *
     * @param arguments a <tt>List</tt> of <tt>String</tt> objects containing
     * the values of the arguments passed to the method.
     * @return the <tt>TemplateModel</tt> produced by the method, or null.
     */
    public Object exec(List arguments) {
        if( arguments.size() == 0 ) {
            return new SimpleScalar( "Empty list provided" );
        } else if( arguments.size() > 1 ) {
            return new SimpleScalar( "Argument size is: " + arguments.size() );
        } else {
            return new SimpleScalar( "Single argument value is: " + arguments.get(0) );
        }
    }
}
