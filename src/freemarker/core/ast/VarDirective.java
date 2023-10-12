package freemarker.core.ast;

import java.util.*;
import freemarker.template.*;
import freemarker.core.*;
import freemarker.core.parser.ast.Expression;
import freemarker.core.parser.ast.StringLiteral;
import freemarker.core.parser.ast.TemplateElement;

/**
 * @author Jonathan Revusky
 */
public class VarDirective extends TemplateElement {
    private Map<String, Expression> vars = new LinkedHashMap<String, Expression>();

    public void execute(Environment env) {
        for (Map.Entry<String, Expression> entry : vars.entrySet()) {
            String varname = entry.getKey();
            Expression exp = entry.getValue();
            Scope scope = env.getCurrentScope();
            if (exp == null) {
                if (scope.get(varname) == null) {
                    scope.put(varname, Constants.JAVA_NULL);
                }
            } 
            else {
                Object tm = exp.evaluate(env);
                assertIsDefined(tm, exp, env);
                scope.put(varname, tm);
            }
        }
    }

    public Map<String, Expression> getVariables() {
        return Collections.unmodifiableMap(vars);
    }

    public void addVar(Expression name, Expression value) {
        String varname = name.toString();
        if (name instanceof StringLiteral) {
            varname = ((StringLiteral) name).getAsString();
        }
        vars.put(varname, value);
    }
    
    public void addVar(String name) {
        vars.put(name, null);
    }

    public String getDescription() {
        return "variable declaration";
    }
}