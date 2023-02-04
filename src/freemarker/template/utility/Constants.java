package freemarker.template.utility;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

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
    
    public static final TemplateModelIterator EMPTY_ITERATOR = new TemplateModelIterator() {

        public TemplateModel next() throws TemplateModelException {
            throw new TemplateModelException("The collection has no more elements.");
        }

        public boolean hasNext() throws TemplateModelException {
            return false;
        }
        
    };

    public static final TemplateCollectionModel EMPTY_COLLECTION = new TemplateCollectionModel() {

        public TemplateModelIterator iterator() throws TemplateModelException {
            return EMPTY_ITERATOR;
        }
        
    };
    
    public static final TemplateSequenceModel EMPTY_SEQUENCE
            = new TemplateSequenceModel() {
    
        public TemplateModel get(int index) throws TemplateModelException {
            return null;
        }
    
        public int size() throws TemplateModelException {
            return 0;
        }
        
    };
    
    public static final TemplateHashModelEx EMPTY_HASH = new TemplateHashModelEx() {

        public int size() throws TemplateModelException {
            return 0;
        }

        public TemplateCollectionModel keys() throws TemplateModelException {
            return EMPTY_COLLECTION;
        }

        public TemplateCollectionModel values() throws TemplateModelException {
            return EMPTY_COLLECTION;
        }

        public TemplateModel get(String key) throws TemplateModelException {
            return null;
        }

        public boolean isEmpty() throws TemplateModelException {
            return true;
        }
        
    };
    
}
