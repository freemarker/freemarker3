package freemarker.testcase.models;

/**
 * @author Attila Szegedi
 * @version $Id: EnumTestClass.java,v 1.1 2005/11/03 08:49:19 szegedia Exp $
 */
public enum EnumTestClass
{
    ONE, 
    TWO, 
    THREE;
    
    @Override
    public String toString()
    {
        return name() + "x";
    }
}
