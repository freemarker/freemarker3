package freemarker.testcase.models;

import freemarker.template.*;

/**
 * A template that always throws an exception whenever we call getAsString()
 *
 * @version $Id: ExceptionModel.java,v 1.13 2003/01/12 23:40:25 revusky Exp $
 */
public class ExceptionModel implements TemplateScalarModel {

    /**
     * Returns the scalar's value as a String.
     *
     * @return the String value of this scalar.
     */
    public String getAsString () {
        throw new TemplateModelException( "Throwing from ExceptionModel!" );
    }
}
