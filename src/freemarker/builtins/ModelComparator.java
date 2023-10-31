package freemarker.builtins;

import java.text.Collator;
import java.util.Date;

import freemarker.core.Environment;
import freemarker.core.ArithmeticEngine;
import freemarker.core.variables.WrappedDate;
import freemarker.core.variables.EvaluationException;
import freemarker.template.TemplateException;

import static freemarker.core.variables.Wrap.*;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public class ModelComparator
{
    private final ArithmeticEngine arithmeticEngine;
    private final Collator collator;
    
    public ModelComparator(Environment env) {
        arithmeticEngine = env.getArithmeticEngine();
        collator = env.getCollator();
    }
    
    /*
     * WARNING! This algorithm is duplication of ComparisonExpression.isTrue(...).
     * Thus, if you update this method, then you have to update that too!
     */
    public boolean modelsEqual(Object model1, Object model2)
    {
        if(isNumber(model1) && isNumber(model2)) {
            try {
                return arithmeticEngine.compareNumbers(
                        asNumber(model1), 
                        asNumber(model2)) == 0;
            } catch (TemplateException ex) {
                throw new EvaluationException(ex);
            }
        }
        
        if(model1 instanceof WrappedDate && model2 instanceof WrappedDate) {
            WrappedDate ltdm = (WrappedDate)model1;
            WrappedDate rtdm = (WrappedDate)model2;
            int ltype = ltdm.getDateType();
            int rtype = rtdm.getDateType();
            if(ltype != rtype) {
                throw new EvaluationException(
                        "Can not compare dates of different type. Left date is of "
                        + WrappedDate.TYPE_NAMES.get(ltype)
                        + " type, right date is of "
                        + WrappedDate.TYPE_NAMES.get(rtype) + " type.");
            }
            if(ltype == WrappedDate.UNKNOWN) {
                throw new EvaluationException(
                "Left date is of UNKNOWN type, and can not be compared.");
            }
            if(rtype == WrappedDate.UNKNOWN) {
                throw new EvaluationException(
                "Right date is of UNKNOWN type, and can not be compared.");
            }
            Date first = ltdm.getAsDate();
            Date second = rtdm.getAsDate();
            return first.compareTo(second) == 0;
        }
        
        if (isString(model1) && isString(model2)) {
            return collator.compare( asString(model1), asString(model2)) == 0;
        }
        
        if(isBoolean(model1) && isBoolean(model2)) {
            return asBoolean(model1) == asBoolean(model2);
        }
        return false;
    }
}