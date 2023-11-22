package freemarker.testcase.models;

import freemarker.core.variables.VarArgsFunction;

public class SimpleTestMethod implements VarArgsFunction {

    public Object apply(Object... arguments) {
        if( arguments.length == 0 ) {
            return "Empty list provided";
        } else if( arguments.length > 1 ) {
            return "Argument size is: " + arguments.length;
        } else {
            return "Single argument value is: " + arguments[0];
        }
    }
}
