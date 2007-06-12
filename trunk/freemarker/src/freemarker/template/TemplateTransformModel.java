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

package freemarker.template;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Objects that implement this interface can be used in a <code>&lt;transform></code>
 * block to perform arbitrary transformations on a part of the template
 * processing output.
 * <P>Note that, as of FreeMarker 2.1, TemplateTransform Model
 * has changed. This is a more powerful implementation. 
 * There is a quick-and-dirty way to patch any legacy
 * TemplateTransformModel so that it implements the new API.
 * You simply add the following as your implementation 
 * of the getWriter() call:
 * <PRE>
 * 
 *    public Writer getWriter(final Writer out, 
 *                            Map args) 
 *    {
 *       final StringBuffer buf = new StringBuffer();
 *       return new Writer() {
 *           public void write(char cbuf[], int off, int len) {
 *               buf.append(cbuf, off, len);
 *           }
 *
 *           public void flush() throws IOException {
 *               out.flush();
 *           }
 * 
 *           public void close() throws IOException {
 *               StringReader sr = new StringReader(buf.toString());
 *               StringWriter sw = new StringWriter();
 *               transform(sr, sw);
 *               out.write(sw.toString());
 *           }
 *       };
 *   }
 *
 * 
 * </PRE>
 * 
 * <P>Implementions of <tt>TemplateTransformModel</tt> should be thread-safe.
 *
 * @version $Id: TemplateTransformModel.java,v 1.36 2003/04/11 20:57:32 revusky Exp $
 */
public interface TemplateTransformModel extends TemplateModel {

     /**
      * Returns a writer that will be used by the engine to feed the
      * transformation input to the transform. Each call to this method
      * must return a new instance of the writer so that the transformation
      * is thread-safe.
      * @param out the character stream to which to write the transformed output
      * @param args the arguments (if any) passed to the transformation as a 
      * map of key/value pairs where the keys are strings and the arguments are
      * TemplateModel instances. This is never null.
      * @return a writer to which the engine will feed the transformation 
      * input, or null if the transform does not support nested content (body).
      * The returned writer can implement the {@link TransformControl}
      * interface if it needs advanced control over the evaluation of the 
      * transformation body.
      */
     Writer getWriter(Writer out, Map<String, TemplateModel> args) 
         throws TemplateModelException, IOException;
}
