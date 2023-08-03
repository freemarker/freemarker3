package freemarker.template;

/**
 * This is a trivial subclass that exists for backward compatibility
 * with the SimpleList from FreeMarker Classic.
 *
 * <p>This class is thread-safe.
 *
 * @deprecated Use SimpleSequence instead.
 * @see SimpleSequence
 */

public class SimpleList extends SimpleSequence {

    public SimpleList() {
    }

    public SimpleList(java.util.List list) {
        super(list);
    }
}

