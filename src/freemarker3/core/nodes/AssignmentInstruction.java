package freemarker3.core.nodes;

import java.io.IOException;
import java.util.*;
import java.lang.reflect.Array;
import freemarker3.template.TemplateException;
import freemarker3.core.Environment;
import freemarker3.core.variables.EvaluationException;
import freemarker3.core.variables.InvalidReferenceException;
import freemarker3.core.nodes.generated.Expression;
import freemarker3.core.nodes.generated.ParentheticalExpression;
import freemarker3.core.nodes.generated.StringLiteral;
import freemarker3.core.nodes.generated.TemplateElement;
import freemarker3.core.nodes.generated.TemplateNode;
import freemarker3.core.parser.Node;
import freemarker3.core.parser.Token;
import freemarker3.core.variables.ReflectionCode;
import static freemarker3.core.parser.Token.TokenType.*;

@SuppressWarnings("unchecked")
public class AssignmentInstruction extends TemplateNode implements TemplateElement {
    public List<String> getVarNames() {
        List<String> result = new ArrayList<>();
        List<Node> equalsToks = childrenOfType(EQUALS);
        for (Node tok : equalsToks) {
            Node varExp = tok.previousSibling();
            if (varExp instanceof StringLiteral) {
                result.add(((StringLiteral)varExp).getAsString());
            }
            else if (varExp.getType() == ID) {
                result.add(varExp.toString());
            }
        }
        return result;
    }

    public List<Expression> getTargetExpressions() {
        return childrenOfType(Expression.class, exp->exp.nextSibling().getType() == EQUALS);
    }

    public Expression getNamespaceExp() {
        Node inToken = firstChildOfType(IN);
        if (inToken != null) {
            return (Expression) inToken.nextSibling();
        }
        return null;
    }

    public void execute(Environment env) throws IOException {
    	Map<String,Object> scope = null;
        Expression namespaceExp = getNamespaceExp();
    	if (namespaceExp != null) {
    		try {
    			scope = (Map<String,Object>) namespaceExp.evaluate(env); 
    		} catch (ClassCastException cce) {
                throw new InvalidReferenceException(getLocation() + "\nInvalid reference to namespace: " + namespaceExp, env);
    		}
    	}
    	else {
    		if (get(0).getType() == ASSIGN) {
    			scope = env.getCurrentNamespace();
    		} else if (get(0).getType() == LOCALASSIGN) {
    			scope = env.getCurrentMacroContext();
    		} else if (get(0).getType() == GLOBALASSIGN) {
    			//scope = env.getGlobalNamespace();
                scope = env;
    		}
    	}
        for (Expression exp : childrenOfType(Expression.class)) {
            if (exp.nextSibling().getType() != EQUALS) continue;
            Expression valueExp = (Expression) exp.nextSibling().nextSibling();
            Object value = valueExp.evaluate(env);
            set(exp, value, env, scope);
        }
    }

    public static void set(Expression lhs, Object value, Environment env, Map scope) {
        while (lhs instanceof ParentheticalExpression) {
            lhs = ((ParentheticalExpression)lhs).getNested();
        }
        if (lhs instanceof Token) {
            String varName = lhs.toString();
            if (lhs instanceof StringLiteral) {
                varName = ((StringLiteral)lhs).getAsString();
            }
            if (scope != null) {
                scope.put(varName, value);
            }
            else {
                env.unqualifiedSet(varName, value);
            }
            return;
        }
        Expression targetExp = (Expression) lhs.get(0);
        Expression keyExp = (Expression) lhs.get(2);
        Object target = targetExp.evaluate(env);
        Object key = lhs instanceof DynamicKeyName ? keyExp.evaluate(env) : keyExp.toString();
        if (key instanceof Number && (target instanceof List || target.getClass().isArray())) {
            int index = ((Number)key).intValue();
            if (target instanceof List) {
                ((List<Object>)target).set(index, value);
            } else try {
                Array.set(target, index, value);
            } catch (Exception e) {
                throw new EvaluationException(e);
            }
            return;
        }
        if (target instanceof Map) {
            ((Map<Object,Object>)target).put(key, value);
            return;
        }
        if (key instanceof String && ReflectionCode.setProperty(target, (String) key, value)) {
            return;
        }
        // TODO: check for the beans setter setXXX method
        // TODO: improve error message a bit
        throw new EvaluationException("Could not set " + lhs);
    }

    public String getDescription() {
    	return "assignment instruction";
    }
}
