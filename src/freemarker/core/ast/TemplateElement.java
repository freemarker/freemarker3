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
	
    List<TemplateElement> nestedElements;
    
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
        return nestedElements;
    }
    
    public TemplateSequenceModel getChildNodes() {
        if (nestedElements != null) {
            return new SimpleSequence(nestedElements);
        }
        SimpleSequence result = new SimpleSequence();
        if (getNestedBlock() != null) {
            result.add(getNestedBlock());
        } 
        return result;
    }
    
    public void setParentRecursively(TemplateElement parent) {
        this.setParent(parent);
        int nestedSize = nestedElements == null ? 0 : nestedElements.size();
        for (int i = 0; i < nestedSize; i++) {
        	nestedElements.get(i).setParentRecursively(this);
        }
        if (getNestedBlock() != null) {
            getNestedBlock().setParentRecursively(this);
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
        if (getParent() == null) {
            return null;
        }
        TemplateElement parentElement = (TemplateElement) this.getParent();
        List<TemplateElement> siblings = parentElement.nestedElements;
        if (siblings == null) {
            return null;
        }
        for (int i = siblings.size() - 1; i>=0; i--) {
            if (siblings.get(i) == this) {
                return(i >0) ? siblings.get(i-1) : null;
            }
        }
        return null;
    }

    protected TemplateElement nextSib() {
        if (getParent() == null) {
            return null;
        }
        TemplateElement parent = (TemplateElement) this.getParent();
        List<TemplateElement> siblings = parent.nestedElements;
        if (siblings == null) {
            return null;
        }
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i) == this) {
                return (i+1) < siblings.size() ? (TemplateElement) siblings.get(i+1) : null;
            }
        }
        return null;
    }

    private TemplateElement firstChild() {
        if (getNestedBlock() != null) {
            return getNestedBlock();
        }
        if (nestedElements != null && nestedElements.size() >0) {
            return nestedElements.get(0);
        }
        return null;
    }

    private TemplateElement lastChild() {
        if (getNestedBlock() != null) {
            return getNestedBlock();
        }
        if (nestedElements != null && nestedElements.size() >0) {
            return nestedElements.get(nestedElements.size() -1);
        }
        return null;
    }
    
    private boolean isLeaf() {
    	return getNestedBlock() == null && (nestedElements == null || nestedElements.isEmpty());
    }
    

    public int getIndex(TemplateElement node) {
        TemplateElement nestedBlock = getNestedBlock();
        if (nestedBlock instanceof MixedContent) {
            return nestedBlock.getIndex(node);
        }
        if (nestedBlock != null) {
            if (node == nestedBlock) {
                return 0;
            }
        }
        else if (nestedElements != null) {
            return nestedElements.indexOf(node);
        }
        return -1;
    }

    public int getChildCount() {
        TemplateElement nestedBlock = getNestedBlock();
        if (nestedBlock instanceof MixedContent) {
            return nestedBlock.getChildCount();
        }
        if (nestedBlock != null) {
            return 1;
        }
        else if (nestedElements != null) {
            return nestedElements.size();
        }
        return 0;
    }
    
    static final Enumeration EMPTY_ENUMERATION = new Enumeration() {
    	public boolean hasMoreElements() {
    		return false;
    	}
    	
    	public Object nextElement() {
    		throw new NoSuchElementException();
    	}
    };

    public Enumeration childrenE() {
        TemplateElement nestedBlock = getNestedBlock();
        if (nestedBlock instanceof MixedContent) {
            return nestedBlock.childrenE();
        }
        if (nestedBlock != null) {
            return Collections.enumeration(Collections.singletonList(nestedBlock));
        }
        else if (nestedElements != null) {
            return Collections.enumeration(nestedElements);
        }
        return EMPTY_ENUMERATION;
    }

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
        else if (nestedElements != null) {
            return nestedElements.get(index);
        }
        throw new ArrayIndexOutOfBoundsException("element has no children");
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
        else if(nestedElements != null) {
            nestedElements.set(index, element);
            element.setParent(this);
        }
        else {
            throw new IndexOutOfBoundsException("element has no children");
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
