package freemarker.ext.beans;

import java.util.Set;

import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModelAdapter;

/**
 * @author Attila Szegedi
 * @version $Id: CollectionAdapter.java,v 1.2 2005/06/12 19:03:04 szegedia Exp $
 */
class SetAdapter extends CollectionAdapter implements Set, TemplateModelAdapter {
    SetAdapter(TemplateCollectionModel model, ObjectWrapper wrapper) {
        super(model, wrapper);
    }
}
