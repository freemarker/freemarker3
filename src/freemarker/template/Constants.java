package freemarker.template;

import java.util.Iterator;

/**
 * Frequently used constant {@link TemplateModel} values.
 * 
 * <p>These constants should be stored in the {@link TemplateModel}
 * sub-interfaces, but for bacward compatibility they are stored here instead.
 * Starting from FreeMarker 2.4 they should be copyed (not moved!) into the
 * {@link TemplateModel} sub-interfaces, and this class should be marked as
 * deprecated.</p>
 * 
 * @version $Id: Constants.java,v 1.2 2004/11/28 12:58:34 ddekany Exp $
 */
public class Constants {

    public static class InvalidExpressionModel implements TemplateModel {
    	InvalidExpressionModel() {}
    }

    /**
     * The type of the {@link JAVA_NULL} object. Using a named 
     * class instead of an anonymous one, as it is easier to figure out what's 
     * wrong from an error message when the reported class name is 
     * "TemplateModel$JavaNull" than when it is "TemplateModel$1", also 
     * implements serialization singleton.
     * @author Attila Szegedi
     * @version $Id: $
     */
    public static class JavaNull implements TemplateModel {
    
        JavaNull() {}
    }

    public static final TemplateBooleanModel TRUE = TemplateBooleanModel.TRUE;

    public static final TemplateBooleanModel FALSE = TemplateBooleanModel.FALSE;
    
    public static final Iterator<Object> EMPTY_ITERATOR = new Iterator<Object>() {

        public TemplateModel next() {
            throw new TemplateModelException("The collection has no more elements.");
        }

        public boolean hasNext() {
            return false;
        }
        
    };

    public static final Iterable EMPTY_COLLECTION = new Iterable() {

        public Iterator<Object> iterator() {
            return EMPTY_ITERATOR;
        }
        
    };
    
    public static final TemplateSequenceModel EMPTY_SEQUENCE
            = new TemplateSequenceModel() {
    
        public TemplateModel get(int index) {
            return null;
        }
    
        public int size() {
            return 0;
        }
        
    };
    
    public static final TemplateHashModelEx EMPTY_HASH = new TemplateHashModelEx() {

        public int size() {
            return 0;
        }

        public Iterable keys() {
            return EMPTY_COLLECTION;
        }

        public Iterable values() {
            return EMPTY_COLLECTION;
        }

        public TemplateModel get(String key) {
            return null;
        }

        public boolean isEmpty() {
            return true;
        }
        
    };

    /**
     * A general-purpose object to represent nothing. It acts as
     * an empty string, false, empty sequence, empty hash, and
     * null-returning method model. It is useful if you want
     * to simulate typical loose scripting language sorts of 
     * behaviors in your templates. 
     * @deprecated Try not to use this.
     */
    public static final TemplateModel NOTHING = GeneralPurposeNothing.getInstance();

    /**
     * A singleton value used to represent a java null
     * which comes from a wrapped Java API, for example, i.e.
     * is intentional. A null that comes from a generic container
     * like a map is assumed to be unintentional and a 
     * result of programming error.
     */
    public static final TemplateModel JAVA_NULL = new Constants.JavaNull();

    /**
     * A singleton value used to represent the result of an 
     * invalid expression, such as 1 - "3"
     */
    
    public static final TemplateModel INVALID_EXPRESSION = new Constants.InvalidExpressionModel();
    
}
