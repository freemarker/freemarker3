package freemarker.core.ast;

import java.util.*;
import java.io.IOException;
import freemarker.template.*;
import freemarker.core.*;
import freemarker.core.parser.Node;
import freemarker.core.parser.ast.TemplateNode;

/**
 * Objects that represent elements in the compiled 
 * tree representation of the template necessarily 
 * descend from this abstract class.
 */
abstract public class TemplateElement extends TemplateNode {
	
    // The scoped variables defined in this element.
    
    private HashSet<String> declaredVariables;

    public Set<String> getDeclaredVariables() {
        return declaredVariables;
    }

    /**
     * Processes the contents of this <tt>TemplateElement</tt> and
     * outputs the resulting text
     *
     * @param env The runtime environment
     */
    abstract public void execute(Environment env) throws TemplateException, IOException;

    public boolean declaresVariable(String name) {
    	return declaredVariables != null && declaredVariables.contains(name);
    }
    
    public void declareVariable(String varName) {
    	if (declaredVariables == null) declaredVariables = new HashSet<String>();
    	declaredVariables.add(varName);
    }
    
    public TemplateElement getNestedBlock() {
        return firstChildOfType(TemplateElement.class);
    }
    
    public void setNestedBlock(TemplateElement nestedBlock) {
        add(nestedBlock);
    }
    
    public List<TemplateElement> getNestedElements() {
        return childrenOfType(TemplateElement.class);
    }
    
    public TemplateSequenceModel getChildNodes() {
        return new SimpleSequence(childrenOfType(TemplateElement.class));
    }
    
    public void setParentRecursively(TemplateElement parent) {
        this.setParent(parent);
        for (TemplateElement te : childrenOfType(TemplateElement.class)) {
        	te.setParentRecursively(this);
        }
    }

    public boolean isIgnorable() {
        return false;
    }
    
// The following methods exist to support some fancier tree-walking 
// and were introduced to support the whitespace cleanup feature in 2.2

    TemplateElement prevTerminalNode() {
        TemplateElement prev = previousSib();
        if (prev != null) {
            return prev.getLastLeaf();
        }
        else if (getParent() != null) {
            return ((TemplateElement)getParent()).prevTerminalNode();
        }
        return null;
    }

    protected TemplateElement nextTerminalNode() {
        TemplateElement next = nextSib();
        if (next != null) {
            return next.getFirstLeaf();
        }
        else if (getParent() != null) {
            return ((TemplateElement)getParent()).nextTerminalNode();
        }
        return null;
    }



    protected TemplateElement previousSib() {
        return (TemplateElement) previousSibling();
    }

    protected TemplateElement nextSib() {
        return (TemplateElement) nextSibling();
    }

    private TemplateElement firstChild() {
        return size() ==0 ? null : (TemplateElement) get(0);
    }

    private TemplateElement lastChild() {
        List<TemplateElement> elements = childrenOfType(TemplateElement.class);
        return elements.isEmpty() ? null : elements.get(elements.size()-1);
    }
    
    private boolean isLeaf() {
    	return size() == 0;
    }
    

    public int getIndex(TemplateElement node) {
        TemplateElement nestedBlock = getNestedBlock();
        if (nestedBlock instanceof MixedContent) {
            return nestedBlock.getIndex(node);
        }
        return indexOf(node);
    }

    public int getChildCount() {
        TemplateElement nestedBlock = getNestedBlock();
        if (nestedBlock instanceof MixedContent) {
            return nestedBlock.getChildCount();
        }
        return size();
    }
    
    static final Enumeration EMPTY_ENUMERATION = new Enumeration() {
    	public boolean hasMoreElements() {
    		return false;
    	}
    	
    	public Object nextElement() {
    		throw new NoSuchElementException();
    	}
    };

    public TemplateElement getChildAt(int index) {
        TemplateElement nestedBlock = getNestedBlock();
        if (nestedBlock instanceof MixedContent) {
            return nestedBlock.getChildAt(index);
        }
        if (nestedBlock != null) {
            if (index == 0) {
                return nestedBlock;
            }
            throw new ArrayIndexOutOfBoundsException("invalid index");
        }
        return (TemplateElement) get(index);
    }

    public void setChildAt(int index, TemplateElement element) {
        TemplateElement nestedBlock = getNestedBlock();
        if(nestedBlock instanceof MixedContent) {
            nestedBlock.setChildAt(index, element);
        }
        else if(nestedBlock != null) {
            if(index == 0) {
                nestedBlock = element;
                element.setParent(this);
            }
            else {
                throw new IndexOutOfBoundsException("invalid index");
            }
        }
        else {
            set(index, element);
            element.setParent(this);
        }
    }    


    private TemplateElement getFirstLeaf() {
        TemplateElement te = this;
        while (!te.isLeaf() && !(te instanceof Macro) && !(te instanceof BlockAssignment)) {
             // A macro or macro invocation is treated as a leaf here for special reasons
            te = te.firstChild();
        }
        return te;
    }

    private TemplateElement getLastLeaf() {
        TemplateElement te = this;
        while (!te.isLeaf() && !(te instanceof Macro) && !(te instanceof BlockAssignment)) {
            // A macro or macro invocation is treated as a leaf here for special reasons
            te = te.lastChild();
        }
        return te;
    }

    public boolean createsScope() {
    	return declaredVariables != null && !declaredVariables.isEmpty();
    }
    
    public Macro getEnclosingMacro() {
        Node parent = this;
        while (parent != null) {
            parent = parent.getParent();
            if (parent instanceof Macro) {
                return (Macro) parent;
            }
        }
        return null;
    }
}
