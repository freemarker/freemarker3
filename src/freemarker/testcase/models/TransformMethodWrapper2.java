package freemarker.testcase.models;

import java.util.*;
import freemarker.template.*;
import static freemarker.ext.beans.ObjectWrapper.asString;

/**
 * Another test of the interaction between MethodModels and TransformModels.
 *
 * @version $Id: TransformMethodWrapper2.java,v 1.12 2004/01/06 17:06:44 szegedia Exp $
 */
public class TransformMethodWrapper2 implements TemplateMethodModel {

    /**
     * Executes a method call.
     *
     * @param arguments a <tt>List</tt> of <tt>String</tt> objects containing
     * the values of the arguments passed to the method.
     * @return the <tt>WrappedVariable</tt> produced by the method, or null.
     */
    public Object exec(List arguments) {
        TransformModel1 cTransformer = new TransformModel1();
        Iterator    iArgument = arguments.iterator();

        // Sets up properties of the Transform model based on the arguments
        // passed into this method

        while( iArgument.hasNext() ) {
            String  aArgument = asString(iArgument.next());

            if( aArgument.equals( "quote" )) {
                cTransformer.setQuotes( true );
            } else if( aArgument.equals( "tag" )) {
                cTransformer.setTags( true );
            } else if( aArgument.equals( "ampersand" )) {
                cTransformer.setAmpersands( true );
            } else {
                cTransformer.setComment( aArgument );
            }
        }

        // Now return the transform class.
        return cTransformer;
    }
}
