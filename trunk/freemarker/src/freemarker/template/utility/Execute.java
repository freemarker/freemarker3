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
import java.io.*;
import java.util.*;

/**
 * <p>Gives FreeMarker the the ability to execute external commands. Will fork
 * a process, and inline anything that process sends to stdout in the template.
 * Based on a patch submitted by Peter Molettiere.</p>
 *
 * <p>BE CAREFUL! this tag, depending on use, may allow you
 * to set something up so that users of your web
 * application could run arbitrary code on your server.
 * This can only happen if you allow unchecked GET/POST
 * submissions to be used as the command string in the
 * exec tag.</p>
 *
 * <p>Usage:<br />
 * From java:</p>
 * <pre>
 * SimpleHash root = new SimpleHash();
 *
 * root.put( "exec", new freemarker.template.utility.Execute() );
 *
 * ...
 * </pre>
 *
 * <p>From your FreeMarker template:</p>
 * <pre>
 *
 * The following is executed:
 * ${exec( "/usr/bin/ls" )}
 *
 * ...
 * </pre>
 *
 * @version $Id: Execute.java,v 1.14 2003/10/13 11:57:18 szegedia Exp $
 */
public class Execute implements freemarker.template.TemplateMethodModel {

    private final static int OUTPUT_BUFFER_SIZE = 1024;

    /**
     * Executes a method call.
     *
     * @param arguments a <tt>List</tt> of <tt>String</tt> objects containing the values
     * of the arguments passed to the method.
     * @return the <tt>TemplateModel</tt> produced by the method, or null.
     */
    public Object exec (List arguments) throws TemplateModelException {
        String aExecute;
        StringBuilder    aOutputBuffer = new StringBuilder();

        if( arguments.size() < 1 ) {
            throw new TemplateModelException( "Need an argument to execute" );
        }

        aExecute = (String)(arguments.get(0));

        try {
            Process exec = Runtime.getRuntime().exec( aExecute );

            // stdout from the process comes in here
            InputStream execOut = exec.getInputStream();
            try {
                Reader execReader = new InputStreamReader( execOut );
    
                char[] buffer = new char[ OUTPUT_BUFFER_SIZE ];
                int bytes_read = execReader.read( buffer );
    
                while( bytes_read > 0 ) {
                    aOutputBuffer.append( buffer, 0, bytes_read );
                    bytes_read = execReader.read( buffer );
                }
            }
            finally {
                execOut.close();
            }
        }
        catch( IOException ioe ) {
            throw new TemplateModelException( ioe.getMessage() );
        }
        return aOutputBuffer.toString();
    }
}
