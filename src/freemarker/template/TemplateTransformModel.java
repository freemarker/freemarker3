package freemarker.template;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import freemarker.template.utility.DeepUnwrap;

/**
 * Objects that implement this interface can be used as user-defined directives 
 * (much like macros).
 *
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
 *       final StringBuilder buf = new StringBuilder();
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
 * @deprecated Use {@link TemplateDirectiveModel} instead.
 * @author Attila Szegedi
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
      * TemplateModel instances. This is never null. If you need to convert the
      * template models to POJOs, you can use the utility methods in the 
      * {@link DeepUnwrap} class.
      * @return a writer to which the engine will feed the transformation 
      * input, or null if the transform does not support nested content (body).
      * The returned writer can implement the {@link TransformControl}
      * interface if it needs advanced control over the evaluation of the 
      * transformation body.
      */
     Writer getWriter(Writer out, Map<String, Object> args) 
         throws IOException;
}
