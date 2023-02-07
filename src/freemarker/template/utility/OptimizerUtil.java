package freemarker.template.utility;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version $Id: OptimizerUtil.java,v 1.9 2003/02/25 11:52:58 szegedia Exp $
 * @author Attila Szegedi
 */
public class OptimizerUtil
{
    private static final BigInteger INTEGER_MIN = new BigInteger(Integer.toString(Integer.MIN_VALUE));
    private static final BigInteger INTEGER_MAX = new BigInteger(Integer.toString(Integer.MAX_VALUE));
    private static final BigInteger LONG_MIN = new BigInteger(Long.toString(Long.MIN_VALUE));
    private static final BigInteger LONG_MAX = new BigInteger(Long.toString(Long.MAX_VALUE));

    private OptimizerUtil()
    {
    }
    
    public static List optimizeListStorage(List list)
    {
        switch(list.size())
        {
            case 0:
            {
                return Collections.EMPTY_LIST;
            }
            case 1:
            {
                return Collections.singletonList(list.get(0));
            }
            default:
            {
                if(list instanceof ArrayList)
                {
                    ((ArrayList)list).trimToSize();
                }
                return list;
            }
        }
    }
    
    /**
     * This is needed to reverse the extreme conversions in arithmetic 
     * operations so that numbers can be meaningfully used with models that
     * don't know what to do with a BigDecimal. Of course, this will make
     * impossible for these models to receive a BigDecimal even if 
     * it was originally placed as such in the data model. However, since 
     * arithmetic operations aggressively erase the information regarding the 
     * original number type, we have no other choice to ensure expected operation
     * in majority of cases.
     */
    public static Number optimizeNumberRepresentation(Number number)
    {
        if(number instanceof BigDecimal)
        {
            BigDecimal bd = (BigDecimal) number;
            if(bd.scale() == 0)
            {
                // BigDecimal -> BigInteger
                number = bd.unscaledValue();
            }
            else
            {
                double d = bd.doubleValue();
                if(d != Double.POSITIVE_INFINITY && d != Double.NEGATIVE_INFINITY)
                {
                    // BigDecimal -> Double
                    return d;
                }
            }
        }
        if(number instanceof BigInteger)
        {
            BigInteger bi = (BigInteger)number;
            if(bi.compareTo(INTEGER_MAX) <= 0 && bi.compareTo(INTEGER_MIN) >= 0)
            {
                // BigInteger -> Integer
                return Integer.valueOf(bi.intValue());
            }
            if(bi.compareTo(LONG_MAX) <= 0 && bi.compareTo(LONG_MIN) >= 0)
            {
                // BigInteger -> Long
                return Long.valueOf(bi.longValue());
            }
        }
        return number;
    }
}
