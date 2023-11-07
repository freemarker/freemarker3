package freemarker.builtins;

import freemarker.core.ArithmeticEngine;
import freemarker.core.Environment;

import static freemarker.core.variables.Wrap.*;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
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
        if(isNumber(left) && isNumber(right)) {
            return arithmeticEngine.compareNumbers(asNumber(left), asNumber(right)) == 0;
        }
        return left.equals(right);
    }
}