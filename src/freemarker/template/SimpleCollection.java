package freemarker.template;

import java.util.*;

import freemarker.ext.beans.ObjectWrapper;

/**
 * A simple implementation of {@link TemplateCollectionModel}.
 * It's able to wrap <tt>java.util.Iterator</tt>-s and <tt>java.util.Collection</tt>-s.
 * If you wrap an <tt>Iterator</tt>, the variable can be &lt;list>-ed (&lt;forach>-ed) only once!
 *
 * <p>Consider using {@link SimpleSequence} instead of this class if you want to wrap <tt>Iterator</tt>s.
 * <tt>SimpleSequence</tt> will read all elements of the <tt>Iterator</tt>, and store them in a <tt>List</tt>
 * (this may cause too high resource consumption in some applications), so you can list the variable
 * for unlimited times. Also, if you want to wrap <tt>Collection</tt>s, and then list the resulting
 * variable for many times, <tt>SimpleSequence</tt> may gives better performance, as the
 * wrapping of non-<tt>TemplateModel</tt> objects happens only once.
 *
 * <p>This class is thread-safe. The returned Iterators 
 * are <em>not</em> thread-safe.
 *
 * @version $Id: SimpleCollection.java,v 1.13 2004/11/27 14:49:57 ddekany Exp $
 */
public class SimpleCollection implements TemplateCollectionModel {

    private boolean iteratorDirty;
    private Iterator iterator;
    private Collection collection;

    public SimpleCollection(Iterator iterator) {
        this.iterator = iterator;
    }

    public SimpleCollection(Collection collection) {
        this.collection = collection;
    }

    /**
     * Retrieves a template model iterator that is used to iterate over the elements in this collection.
     *  
     * <p>When you wrap an <tt>Iterator</tt> and you get Iterator for multiple times,
     * only on of the returned Iterator instances can be really used. When you have called a
     * method of an Iterator instance, all other instance will throw a
     * <tt>TemplateModelException</tt> when you try to call their methods, since the wrapped <tt>Iterator</tt>
     * can't return the first element.
     */
    public Iterator<Object> iterator() {
        if (iterator != null) {
            return new SimpleTemplateModelIterator(iterator, true);
        } else {
            synchronized (collection) {
                return new SimpleTemplateModelIterator(collection.iterator(), false);
            }
        }
    }
    
    /*
     * An instance of this class must be accessed only from a single thread.
     * The encapsulated Iterator may accessible from multiple threads (as multiple
     * SimpleTemplateModelIterator instance can wrap the same Iterator instance),
     * but the first thread which uses the shared Iterator will monopolize that.
     */
    private class SimpleTemplateModelIterator implements Iterator<Object> {
        
        private Iterator iterator;
        private boolean iteratorShared;
            
        SimpleTemplateModelIterator(Iterator iterator, boolean iteratorShared) {
            this.iterator = iterator;
            this.iteratorShared = iteratorShared;
        }

        public Object next() {
            if (iteratorShared) makeIteratorDirty();
            
            if (!iterator.hasNext()) {
                throw new TemplateModelException("The collection has no more elements.");
            }
            
            Object value  = iterator.next();
            return ObjectWrapper.instance().wrap(value);
        }

        public boolean hasNext() {
            /* 
             * Theorically this should not make the iterator dirty,
             * but I met sync. problems if I don't do it here. :(
             */
            if (iteratorShared) makeIteratorDirty();
            return iterator.hasNext();
        }
        
        private void makeIteratorDirty() {
            synchronized (SimpleCollection.this) {
                if (iteratorDirty) {
                    throw new TemplateModelException(
                            "This collection variable wraps a java.util.Iterator, "
                            + "thus it can be <list>-ed or <foreach>-ed only once");
                } else {
                    iteratorDirty = true;
                    iteratorShared = false;
                }
            }
        }
    }
}
