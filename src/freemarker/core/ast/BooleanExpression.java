package freemarker.core.ast;

import freemarker.core.Environment;

abstract class BooleanExpression extends Expression {

    public Object evaluate(Environment env)
    {
        return isTrue(env);
    }
}
