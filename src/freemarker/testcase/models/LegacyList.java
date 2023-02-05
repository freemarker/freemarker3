package freemarker.testcase.models;

import freemarker.template.*;
import java.util.*;

/**
 * A little bridge class that subclasses the new SimpleList
 * and still implements the deprecated TemplateListModel
 */
public class LegacyList extends SimpleSequence {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5113695891943970389L;
	private Iterator iterator;

    /**
     * Resets the cursor to the beginning of the list.
     */
    public synchronized void rewind() {
        iterator = null;
    }

    /**
     * @return true if the cursor is at the beginning of the list.
     */
    public synchronized boolean isRewound() {
        return (iterator == null);
    }

    /**
     * @return true if there is a next element.
     */
    public synchronized boolean hasNext() {
        if (iterator == null) {
            iterator = list.listIterator();
        }
        return iterator.hasNext();
    }

    /**
     * @return the next element in the list.
     */
    public synchronized TemplateModel next() {
        if (iterator == null) {
            iterator = list.listIterator();
        }
        if (iterator.hasNext()) {
            return (TemplateModel)iterator.next();
        } else {
            throw new TemplateModelException("No more elements.");
        }
    }
}
