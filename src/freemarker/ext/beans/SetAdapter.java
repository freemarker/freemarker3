package freemarker.ext.beans;

import java.util.Set;

/**
 * @author Attila Szegedi
 * @version $Id: CollectionAdapter.java,v 1.2 2005/06/12 19:03:04 szegedia Exp $
 */
class SetAdapter extends CollectionAdapter implements Set {
    SetAdapter(Iterable model, ObjectWrapper wrapper) {
        super(model, wrapper);
    }
}
