package freemarker.core.nodes;

import static freemarker.core.variables.Wrap.wrap;
import freemarker.core.variables.scope.Scope;
import freemarker.core.variables.Pojo;
import freemarker.core.variables.WrappedHash;
import freemarker.core.parser.Token;
import freemarker.core.Environment;
import freemarker.core.nodes.generated.Expression;
import freemarker.core.nodes.generated.TemplateNode;

import java.util.Map;

import freemarker.core.parser.*;
import java.util.*;
import freemarker.core.parser.Token.TokenType;
import static freemarker.core.parser.Token.TokenType.*;


public class DotVariable extends TemplateNode implements Expression {

    public Expression getTarget() {
        return (Expression) get(0);
    }

    public String getKey() {
        return get(2).toString();
    }

    public Object evaluate(Environment env) {
        Object lhs = getTarget().evaluate(env);
        if (lhs instanceof Map) {
            return wrap(((Map) lhs).get(getKey()));
        }
        if (lhs instanceof WrappedHash) {
            return wrap(((WrappedHash) lhs).get(getKey()));
        }
        if (lhs instanceof Scope) {
            return wrap(((Scope) lhs).get(getKey()));
        }
        if (lhs instanceof Pojo) {
            boolean looseSyntax = !this.getTemplate().strictVariableDeclaration();
            return wrap(((Pojo) lhs).get(getKey(), looseSyntax));
        }
        throw invalidTypeException(lhs, getTarget(), env, "hash");
    }

    public Expression _deepClone(String name, Expression subst) {
        Expression clonedTarget = getTarget().deepClone(name, subst);
        Token op = (Token) get(1);
        Token key = (Token) get(2);
        Expression result = new DotVariable();
        result.add(clonedTarget);
        result.add(op);
        result.add(key);
        return result;
    }

}


