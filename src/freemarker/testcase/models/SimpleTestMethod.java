package freemarker.testcase.models;

import java.util.List;

import freemarker.ext.beans.StringModel;
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
            return new StringModel( "Empty list provided" );
        } else if( arguments.size() > 1 ) {
            return new StringModel( "Argument size is: " + arguments.size() );
        } else {
            return new StringModel( "Single argument value is: " + arguments.get(0) );
        }
    }
}
