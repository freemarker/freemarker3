package freemarker.testcase.models;

import freemarker.core.variables.*;
import java.util.*;
import static freemarker.core.variables.Wrap.asString;

/**
 * Testcase to see how FreeMarker deals with multiple Template models.
 *
 * @version $Id: MultiModel2.java,v 1.15 2004/01/06 17:06:44 szegedia Exp $
 */
public class MultiModel2 implements  WrappedMethod {

    /**
     * Returns the scalar's value as a String.
     *
     * @return the String value of this scalar.
     */
    public String toString() {
        return "Model2 is alive!";
    }

    /**
     * Executes a method call.
     *
     * @param arguments a <tt>List</tt> of objects containing the values
     * of the arguments passed to the method.
     * @return t Strin that just concatenates the string representations
     * of all the arguments
     */
    @Override
    public String exec(List arguments) {
        StringBuilder  aResults = new StringBuilder( "Arguments are:<br />" );
        for (Object arg : arguments) {
            aResults.append(arg);
            aResults.append( "<br />" );
        }
        return aResults.toString();
    }
}
