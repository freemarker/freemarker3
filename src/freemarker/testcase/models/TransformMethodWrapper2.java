package freemarker.testcase.models;

import java.util.Arrays;
import java.util.Iterator;
import freemarker.core.variables.VarArgsFunction;

/**
 * Another test of the interaction between MethodModels and TransformModels.
 *
 * @version $Id: TransformMethodWrapper2.java,v 1.12 2004/01/06 17:06:44 szegedia Exp $
 */
public class TransformMethodWrapper2 implements VarArgsFunction {

    public Object apply(Object... arguments) {
        TransformModel1 cTransformer = new TransformModel1();
        Iterator<Object> iArgument = Arrays.asList(arguments).iterator();
        while( iArgument.hasNext() ) {
            String aArgument = iArgument.next().toString();
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
        return cTransformer;
    }
}
