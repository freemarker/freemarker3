/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package freemarker.template;

import java.io.Serializable;
import java.util.*;

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
 * <p>This class is thread-safe. The returned <tt>TemplateModelIterator</tt>-s
 * are <em>not</em> thread-safe.
 *
 * @version $Id: SimpleCollection.java,v 1.13 2004/11/27 14:49:57 ddekany Exp $
 */
public class SimpleCollection extends WrappingTemplateModel
implements TemplateCollectionModel, Serializable {
    private static final long serialVersionUID = 1729171563434869856L;

    private boolean iteratorDirty;
    private Iterator iterator;
    private Collection collection;

    public SimpleCollection(Iterator iterator) {
        this.iterator = iterator;
    }

    public SimpleCollection(Collection collection) {
        this.collection = collection;
    }

    public SimpleCollection(Iterator iterator, ObjectWrapper wrapper) {
        super(wrapper);
        this.iterator = iterator;
    }

    public SimpleCollection(Collection collection, ObjectWrapper wrapper) {
        super(wrapper);
        this.collection = collection;
    }

    /**
     * Retrieves a template model iterator that is used to iterate over the elements in this collection.
     *  
     * <p>When you wrap an <tt>Iterator</tt> and you get <tt>TemplateModelIterator</tt> for multiple times,
     * only on of the returned <tt>TemplateModelIterator</tt> instances can be really used. When you have called a
     * method of a <tt>TemplateModelIterator</tt> instance, all other instance will throw a
     * <tt>TemplateModelException</tt> when you try to call their methods, since the wrapped <tt>Iterator</tt>
     * can't return the first element.
     */
    public TemplateModelIterator iterator() {
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
    private class SimpleTemplateModelIterator implements TemplateModelIterator {
        
        private Iterator iterator;
        private boolean iteratorShared;
            
        SimpleTemplateModelIterator(Iterator iterator, boolean iteratorShared) {
            this.iterator = iterator;
            this.iteratorShared = iteratorShared;
        }

        public TemplateModel next() throws TemplateModelException {
            if (iteratorShared) makeIteratorDirty();
            
            if (!iterator.hasNext()) {
                throw new TemplateModelException("The collection has no more elements.");
            }
            
            Object value  = iterator.next();
            if (value instanceof TemplateModel) {
                return (TemplateModel) value;
            } else {
                return wrap(value);
            }
        }

        public boolean hasNext() throws TemplateModelException {
            /* 
             * Theorically this should not make the iterator dirty,
             * but I met sync. problems if I don't do it here. :(
             */
            if (iteratorShared) makeIteratorDirty();
            return iterator.hasNext();
        }
        
        private void makeIteratorDirty() throws TemplateModelException {
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
