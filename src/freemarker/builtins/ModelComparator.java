package freemarker.builtins;

import freemarker.core.ArithmeticEngine;
import freemarker.core.Environment;

import static freemarker.core.variables.Wrap.unwrap;

public class ModelComparator
{
    private final ArithmeticEngine arithmeticEngine;
    
    public ModelComparator(Environment env) {
        arithmeticEngine = env.getArithmeticEngine();
    }
    
    public boolean modelsEqual(Object left, Object right)
    {
        left = unwrap(left);
        right = unwrap(right);
        if(left instanceof Number && right instanceof Number) {
            return arithmeticEngine.compareNumbers((Number)left, (Number)right) == 0;
        }
        return left.equals(right);
    }
}