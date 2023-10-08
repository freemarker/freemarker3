package freemarker.testcase.models;

import freemarker.annotations.Parameters;
import freemarker.template.*;
import java.io.*;
import java.util.Map;

/**
 * A TemplateTransformModel that includes properties. These properties can be
 * set at model construction time, or, for the purposes of this demonstration,
 * can be passed in from a wrapper TemplateMethodModel.
 *
 * @version $Id: TransformModel1.java,v 1.21 2003/01/12 23:40:25 revusky Exp $
 */

@Parameters("")

public class TransformModel1 implements TemplateTransformModel {

    private boolean m_bAmpersands = false;
    private boolean m_bQuotes = false;
    private boolean m_bTags = false;
    private String  m_aComment = "";

    private static final int READER_BUFFER_SIZE = 4096;

    public Writer getWriter(final Writer out, 
                            final Map args) 
    {
        final StringBuilder buf = new StringBuilder();
        return new Writer(out) {
            public void write(char cbuf[], int off, int len) {
                buf.append(cbuf, off, len);
            }

            public void flush() {
            }

            public void close() throws IOException {
                StringReader sr = new StringReader(buf.toString());
                StringWriter sw = new StringWriter();
                transform(sr, sw);
                out.write(sw.toString());
            }
        };
    }


    /**
     * Indicates whether we escape ampersands. This property can be set either
     * while the model is being constructed, or via a property passed in through
     * a <code>TemplateMethodModel</code>.
     */
    public void setAmpersands( boolean bAmpersands ) {
        m_bAmpersands = bAmpersands;
    }

    /**
     * Indicates whether we escape quotes. This property can be set either
     * while the model is being constructed, or via a property passed in through
     * a <code>TemplateMethodModel</code>.
     */
    public void setQuotes( boolean bQuotes ) {
        m_bQuotes = bQuotes;
    }

    /**
     * Indicates whether we escape tags. This property can be set either
     * while the model is being constructed, or via a property passed in through
     * a <code>TemplateMethodModel</code>.
     */
    public void setTags( boolean bTags ) {
        m_bTags = bTags;
    }

    /**
     * Sets a comment for this transformation. This property can be set either
     * while the model is being constructed, or via a property passed in through
     * a <code>TemplateMethodModel</code>.
     */
    public void setComment( String aComment ) {
        m_aComment = aComment;
    }

    /**
     * Performs a transformation/filter on FreeMarker output.
     *
     * @param source the input to be transformed
     * @param output the destination of the transformation
     */
    public void transform(Reader source, Writer output)
    throws IOException 
    {
        boolean bCommentSent = false;
        char[]  aBuffer = new char[ READER_BUFFER_SIZE ];
        int i = source.read( aBuffer );
        while (i >= 0) {
            for ( int j = 0; j < i; j++ ) {
                char c = aBuffer[j];
                switch (c) {
                    case '&':
                        if ( m_bAmpersands ) {
                            output.write("&amp;");
                        } else {
                            output.write( c );
                        }
                        break;
                    case '<':
                        if ( m_bTags ) {
                            output.write("&lt;");
                        } else {
                            output.write( c );
                        }
                        break;
                    case '>':
                        if ( m_bTags ) {
                            output.write("&gt;");
                        } else {
                            output.write( c );
                        }
                        break;
                    case '"':
                        if ( m_bQuotes ) {
                            output.write("&quot;");
                        } else {
                            output.write( c );
                        }
                        break;
                    case '\'':
                        if ( m_bQuotes ) {
                            output.write("&apos;");
                        } else {
                            output.write( c );
                        }
                        break;
                    case '*':
                        if ( ! bCommentSent ) {
                            output.write( m_aComment );
                            bCommentSent = true;
                        } else {
                            output.write( c );
                        }
                        break;
                    default:
                        output.write(c);
                }
            }
            i = source.read( aBuffer );
        }
    }
}
