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

    public static final TemplateBooleanModel TRUE = TemplateBooleanModel.TRUE;

    public static final TemplateBooleanModel FALSE = TemplateBooleanModel.FALSE;
    
    public static final TemplateScalarModel EMPTY_STRING = (TemplateScalarModel) TemplateScalarModel.EMPTY_STRING;

    public static final TemplateNumberModel ZERO = new SimpleNumber(0);
    
    public static final TemplateNumberModel ONE = new SimpleNumber(1);
    
    public static final TemplateNumberModel MINUS_ONE = new SimpleNumber(-1);
    
    public static final Iterator<Object> EMPTY_ITERATOR = new Iterator<Object>() {

        public TemplateModel next() {
            throw new TemplateModelException("The collection has no more elements.");
        }

        public boolean hasNext() {
            return false;
        }
        
    };

    public static final TemplateCollectionModel EMPTY_COLLECTION = new TemplateCollectionModel() {

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

        public TemplateCollectionModel keys() {
            return EMPTY_COLLECTION;
        }

        public TemplateCollectionModel values() {
            return EMPTY_COLLECTION;
        }

        public TemplateModel get(String key) {
            return null;
        }

        public boolean isEmpty() {
            return true;
        }
        
    };
    
}
