package freemarker.core.ast;

import freemarker.template.*;

/**
 * A class that represents a Range between two integers.
 * inclusive of the end-points. It can be ascending or
 * descending. 
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */
class NumericalRange implements TemplateSequenceModel, java.io.Serializable {
    private static final long serialVersionUID = 8329795189999011437L;

    private int lower, upper;
    private boolean descending, norhs; // if norhs is true, then we have a half-range, like n..
    
    
    /**
     * Constructor for half-range, i.e. n..
     */
    public NumericalRange(int lower) {
        this.norhs = true;
        this.lower = lower;
    }

    public NumericalRange(int left, int right) {
        lower = Math.min(left, right);
        upper = Math.max(left, right);
        descending = (left != lower);
    }

    public TemplateModel get(int i) {
        int index = descending ? (upper -i) : (lower + i);
        if ((norhs && index > upper) || index <lower) {
            throw new TemplateModelException("out of bounds of range");
        }
        return new SimpleNumber(index);
    }

    public int size() {
        return 1 + upper - lower;
    }
    
    boolean hasRhs() {
        return !norhs;
    }
}

