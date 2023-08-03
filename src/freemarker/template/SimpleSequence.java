package freemarker.template;

import java.util.*;

import freemarker.ext.beans.ObjectWrapper;

/**
 * <p>A convenient implementation of a list. This
 * object implements {@link TemplateSequenceModel}, using an underlying 
 * <tt>java.util.List</tt> implementation.</p>
 *
 * <p>A <tt>SimpleSequence</tt> can act as a cache for a
 * <tt>TemplateCollectionModel</tt>, e.g. one that gets data from a
 * database.  When passed a <tt>TemplateCollectionModel</tt> as an
 * argument to its constructor, the <tt>SimpleSequence</tt> immediately 
 * copies all the elements and discards the <tt>TemplateCollectionModel</tt>.</p>
 *
 * <p>This class is thread-safe if you don't call the <tt>add</tt> method after you
 * have made the object available for multiple threads.
 *
 * <p><b>Note:</b><br />
 * As of 2.0, this class is unsynchronized by default.
 * To obtain a synchronized wrapper, call the {@link #synchronizedWrapper} method.</p>
 *
 * @version $Id: SimpleSequence.java,v 1.53 2005/06/21 18:17:54 ddekany Exp $
 * @see SimpleHash
 * @see SimpleScalar
 */
public class SimpleSequence implements TemplateSequenceModel {

    protected final List<Object> list;
    private List<Object> unwrappedList;

    public SimpleSequence() {
        list = new ArrayList<Object>();
    }

    /**
     * Constructs an empty simple sequence with preallocated capacity and using
     * the default object wrapper set in 
     * {@link WrappingTemplateModel#setDefaultObjectWrapper(ObjectWrapper)}.
     */
    public SimpleSequence(int capacity) {
        list = new ArrayList<Object>(capacity);
    }

    /**
     * Constructs a simple sequence that will contain the elements
     * from the specified {@link Collection} and will use the the default 
     * object wrapper set in 
     * {@link WrappingTemplateModel#setDefaultObjectWrapper(ObjectWrapper)}.
     * @param collection the collection containing initial values. Note that a
     * copy of the collection is made for internal use.
     */
    public SimpleSequence(Collection collection) {
        list = new ArrayList<Object>(collection);
    }
    
    /**
     * Constructs a simple sequence from the passed collection model using the
     * default object wrapper set in 
     * {@link WrappingTemplateModel#setDefaultObjectWrapper(ObjectWrapper)}.
     */
    public SimpleSequence(TemplateCollectionModel tcm) {
        ArrayList<Object> alist = new ArrayList<Object>();
        for (Iterator<Object> it = tcm.iterator(); it.hasNext();) {
            alist.add(it.next());
        }
        alist.trimToSize();
        list = alist;
    }

    /**
     * Adds an arbitrary object to the end of this <tt>SimpleSequence</tt>.
     * If the object itself does not implement the {@link TemplateModel} 
     * interface, it will be wrapped into an appropriate adapter on the first 
     * call to {@link #get(int)}.
     *
     * @param obj the boolean to be added.
     */
    public void add(Object obj) {
        list.add(obj);
        unwrappedList = null;
    }

    /**
     * Adds a boolean to the end of this <tt>SimpleSequence</tt>, by 
     * coercing the boolean into {@link TemplateBooleanModel#TRUE} or 
     * {@link TemplateBooleanModel#FALSE}.
     *
     * @param b the boolean to be added.
     */
    public void add(boolean b) {
        if (b) {
            add(TemplateBooleanModel.TRUE);
        } 
        else {
            add(TemplateBooleanModel.FALSE);
        }
    }
    
    /**
     * Note that this method creates and returns a deep-copy of the underlying list used
     * internally. This could be a gotcha for some people
     * at some point who want to alter something in the data model,
     * but we should maintain our immutability semantics (at least using default SimpleXXX wrappers) 
     * for the data model. It will recursively unwrap the stuff in the underlying container. 
     */
    public List toList() {
        if (unwrappedList == null) {
            Class<? extends List> listClass = list.getClass();
            List result = null;
            try {
                result = listClass.newInstance();
            } catch (Exception e) {
                throw new TemplateModelException("Error instantiating an object of type " + listClass.getName() + "\n" + e.getMessage());
            }
            ObjectWrapper bw = ObjectWrapper.instance();
            for (int i=0; i<list.size(); i++) {
                Object elem = list.get(i);
                if (elem instanceof TemplateModel) {
                    elem = bw.unwrap((TemplateModel) elem);
                }
                result.add(elem);
            }
            unwrappedList = result;
        }
        return unwrappedList;
    }
    
    /**
     * @return the specified index in the list
     */
    public Object get(int i) {
        try {
            Object value = list.get(i);
            if (value instanceof TemplateModel) {
                return (TemplateModel) value;
            }
            Object tm = ObjectWrapper.instance().wrap(value);
            list.set(i, tm);
            return tm;
        }
        catch(IndexOutOfBoundsException e) {
            return null;
//            throw new TemplateModelException(i + " out of bounds [0, " + list.size() + ")");
        }
    }

    public int size() {
        return list.size();
    }

}