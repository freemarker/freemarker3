package freemarker.template;

import java.util.List;
import java.util.ArrayList;

/**
 * Singleton object representing nothing, used by ?if_exists built-in.
 * It is meant to be interpreted in the most sensible way possible in various contexts.
 * This can be returned to avoid exceptions.
 *
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */

final class GeneralPurposeNothing
implements TemplateBooleanModel, TemplateScalarModel, TemplateSequenceModel, TemplateHashModelEx, TemplateMethodModelEx {

    private static final TemplateModel instance = new GeneralPurposeNothing();
      
    private static final TemplateCollectionModel EMPTY_COLLECTION = new SimpleCollection(new ArrayList(0));

    private GeneralPurposeNothing() {
    }

    static TemplateModel getInstance()  {
        return instance;
    }

    public String getAsString() {
        return "";
    }

    public boolean getAsBoolean() {
        return false;
    }

    public boolean isEmpty() {
        return true;
    }

    public int size() {
        return 0;
    }

    public TemplateModel get(int i) throws TemplateModelException {
        throw new TemplateModelException("Empty list");
    }

    public TemplateModel get(String key) {
        return null;
    }

    public Object exec(List args) {
        return null;
    }
    
    public TemplateCollectionModel keys() {
        return EMPTY_COLLECTION;
    }

    public TemplateCollectionModel values() {
        return EMPTY_COLLECTION;
    }
}
