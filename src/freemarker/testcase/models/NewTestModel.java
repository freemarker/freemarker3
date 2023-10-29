package freemarker.testcase.models;

import freemarker.core.evaluation.WrappedString;

/**
 * Testcase to see how FreeMarker's ?new built-in deals with constructors.
 *
 * @version $Id: NewTestModel.java,v 1.4 2003/01/12 23:40:25 revusky Exp $
 */
public class NewTestModel implements WrappedString
{
    private final String string;
    
    public NewTestModel() {
        string = "default constructor";
    }

    public NewTestModel(String str) {
        string = str;
    }

    public NewTestModel(long i) {
        string = Long.toString(i);
    }

    public NewTestModel(Object o1, java.io.Serializable o2) {
        string = o1 + ":" + o2;
    }

    public String getAsString() {
        return string;
    }
}
