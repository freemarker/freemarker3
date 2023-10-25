package freemarker.testcase.models;

import java.util.*;
import freemarker.template.*;
import freemarker.template.utility.*;

/**
 * Simple test of the interaction between MethodModels and TransformModels.
 *
 * @version $Id: TransformMethodWrapper1.java,v 1.12 2004/01/06 17:06:44 szegedia Exp $
 */
public class TransformMethodWrapper1 extends Object implements TemplateMethodModel {

    /**
     * Executes a method call.
     *
     * @param arguments a <tt>List</tt> of <tt>String</tt> objects containing
     * the values of the arguments passed to the method.
     * @return the <tt>WrappedVariable</tt> produced by the method, or null.
     */
    public Object exec(List arguments) {

        if(( arguments.size() > 0 ) && ( arguments.get( 0 ).toString().equals( "xml" ))) {
            return new XmlEscape();
        } else {
            return new HtmlEscape();
        }
    }
}
