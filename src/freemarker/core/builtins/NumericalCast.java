package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.nodes.generated.TemplateNode;
import freemarker.template.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static freemarker.core.evaluation.ObjectWrapper.*;

/**
 * Implementation of ?byte, ?int, ?double, ?float,
 * ?short and ?long built-ins 
 */

public class NumericalCast extends ExpressionEvaluatingBuiltIn {
    private static final BigDecimal half = new BigDecimal("0.5");
    private static final MathContext mc = new MathContext(0, RoundingMode.FLOOR);

    @Override
    public Object get(Environment env, BuiltInExpression caller, Object model) 
    {
        try {
            return getNumber(asNumber(model), caller.getName());
        } catch (ClassCastException cce) {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "number");
        } catch (NullPointerException npe) {
            throw new InvalidReferenceException("Undefined number", env);
        }
    }

    private Number getNumber(Number num, String builtInName) {
        if (builtInName == "int") {
            return num.intValue();
        }
        else if (builtInName == "double") {
            return num.doubleValue();
        }
        else if (builtInName == "long") {
            return Long.valueOf(num.longValue());
        }
        else if (builtInName == "float") {
            return num.floatValue();
        }
        else if (builtInName == "byte") {
            return num.byteValue();
        }
        else if (builtInName == "short") {
            return num.shortValue();
        }
        else if (builtInName == "floor") {
            return (BigDecimal.valueOf(num.doubleValue()).divide(BigDecimal.ONE, 0, RoundingMode.FLOOR));
        }
        else if (builtInName == "ceiling") {
            return (BigDecimal.valueOf(num.doubleValue()).divide(BigDecimal.ONE, 0, RoundingMode.CEILING));
        }
        else if (builtInName == "round") {
            return (BigDecimal.valueOf(num.doubleValue()).add(half, mc).divide(BigDecimal.ONE, 0, RoundingMode.FLOOR));
        }
        else {
            throw new InternalError("The only numerical cast built-ins available are ?int, ?long, ?short, ?byte, ?float, ?double, ?floor, ?ceiling, and ?round.");
        }
    }
}
