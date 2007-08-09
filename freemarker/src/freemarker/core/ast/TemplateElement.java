/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */

package freemarker.core.ast;

import java.util.*;
import java.io.IOException;
import freemarker.template.*;
import freemarker.core.*;
import freemarker.core.parser.ParseException;



/**
 * Objects that represent elements in the compiled 
 * tree representation of the template necessarily 
 * descend from this abstract class.
 */
abstract public class TemplateElement extends TemplateNode {
	
    protected TemplateElement parent;

// Only one of nestedBlock and nestedElements can be non-null.

    protected TemplateElement nestedBlock;

    protected List<TemplateElement> nestedElements;
    
    // The scoped variables defined in this element.
    
    HashSet<String> declaredVariables;
    
    /**
     * Processes the contents of this <tt>TemplateElement</tt> and
     * outputs the resulting text
     *
     * @param env The runtime environment
     */
    abstract public void execute(Environment env) throws TemplateException, IOException;

    public Scope createLocalScope(Scope enclosingScope) {
    	return new BlockContext(this, enclosingScope);
    }
    
    public boolean declaresVariable(String name) {
    	return declaredVariables != null && declaredVariables.contains(name);
    }
    
    public void declareVariable(String varName) {
    	if (declaredVariables == null) declaredVariables = new HashSet<String>();
    	declaredVariables.add(varName);
    }
    
    public TemplateElement getNestedBlock() {
    	return nestedBlock;
    }
    
    public void setNestedBlock(TemplateElement nestedBlock) {
    	this.nestedBlock = nestedBlock;
    }
    
    public void setParent(TemplateElement parent) {
    	this.parent = parent;
    }
    

    public TemplateSequenceModel getChildNodes() {
        if (nestedElements != null) {
            return new SimpleSequence(nestedElements);
        }
        SimpleSequence result = null;
        if (nestedBlock != null) {
            result.add(nestedBlock);
        } 
        return result;
    }
    
    public TemplateElement getParent() {
        return parent;
    }

    // Walk the tree and set the parent field in all the nested elements recursively.

    public void setParentRecursively(TemplateElement parent) {
        this.parent = parent;
        int nestedSize = nestedElements == null ? 0 : nestedElements.size();
        for (int i = 0; i < nestedSize; i++) {
        	nestedElements.get(i).setParentRecursively(this);
        }
        if (nestedBlock != null) {
            nestedBlock.setParentRecursively(this);
        }
    }


    boolean isIgnorable() {
        return false;
    }
    
    public void removeIgnorableChildren() {
    	if (nestedElements != null) {
    		Iterator<TemplateElement> it = nestedElements.iterator();
    		while (it.hasNext()) {
    			TemplateElement child = it.next();
    			if (child.isIgnorable()) it.remove();
    		}
    		if (nestedElements instanceof ArrayList) {
    			((ArrayList) nestedElements).trimToSize();
    		}
    	}
    	else if (nestedBlock != null) {
    		if (nestedBlock.isIgnorable()) {
    			nestedBlock = null;
    		}
    	}
    }

// The following methods exist to support some fancier tree-walking 
// and were introduced to support the whitespace cleanup feature in 2.2

    TemplateElement prevTerminalNode() {
        TemplateElement prev = previousSibling();
        if (prev != null) {
            return prev.getLastLeaf();
        }
        else if (parent != null) {
            return parent.prevTerminalNode();
        }
        return null;
    }

    protected TemplateElement nextTerminalNode() {
        TemplateElement next = nextSibling();
        if (next != null) {
            return next.getFirstLeaf();
        }
        else if (parent != null) {
            return parent.nextTerminalNode();
        }
        return null;
    }



    protected TemplateElement previousSibling() {
        if (parent == null) {
            return null;
        }
        List siblings = parent.nestedElements;
        if (siblings == null) {
            return null;
        }
        for (int i = siblings.size() - 1; i>=0; i--) {
            if (siblings.get(i) == this) {
                return(i >0) ? (TemplateElement) siblings.get(i-1) : null;
            }
        }
        return null;
    }

    protected TemplateElement nextSibling() {
        if (parent == null) {
            return null;
        }
        List siblings = parent.nestedElements;
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

    private TemplateElement getFirstChild() {
        if (nestedBlock != null) {
            return nestedBlock;
        }
        if (nestedElements != null && nestedElements.size() >0) {
            return nestedElements.get(0);
        }
        return null;
    }

    private TemplateElement getLastChild() {
        if (nestedBlock != null) {
            return nestedBlock;
        }
        if (nestedElements != null && nestedElements.size() >0) {
            return nestedElements.get(nestedElements.size() -1);
        }
        return null;
    }
    
    private boolean isLeaf() {
    	return nestedBlock == null && (nestedElements == null || nestedElements.isEmpty());
    }
    

    public int getIndex(TemplateElement node) {
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

    public Enumeration children() {
        if (nestedBlock instanceof MixedContent) {
            return nestedBlock.children();
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
        if(nestedBlock instanceof MixedContent) {
            nestedBlock.setChildAt(index, element);
        }
        else if(nestedBlock != null) {
            if(index == 0) {
                nestedBlock = element;
                element.parent = this;
            }
            else {
                throw new IndexOutOfBoundsException("invalid index");
            }
        }
        else if(nestedElements != null) {
            nestedElements.set(index, element);
            element.parent = this;
        }
        else {
            throw new IndexOutOfBoundsException("element has no children");
        }
    }    


    private TemplateElement getFirstLeaf() {
        TemplateElement te = this;
        while (!te.isLeaf() && !(te instanceof Macro) && !(te instanceof BlockAssignment)) {
             // A macro or macro invocation is treated as a leaf here for special reasons
            te = te.getFirstChild();
        }
        return te;
    }

    private TemplateElement getLastLeaf() {
        TemplateElement te = this;
        while (!te.isLeaf() && !(te instanceof Macro) && !(te instanceof BlockAssignment)) {
            // A macro or macro invocation is treated as a leaf here for special reasons
            te = te.getLastChild();
        }
        return te;
    }

    /**
     * determines whether this element's presence on a line 
     * indicates that we should not strip opening whitespace
     * in the post-parse whitespace gobbling step.
     */
    boolean heedsOpeningWhitespace() {
        return false;
    }

    /**
     * determines whether this element's presence on a line 
     * indicates that we should not strip trailing whitespace
     * in the post-parse whitespace gobbling step.
     */
    boolean heedsTrailingWhitespace() {
        return false;
    }
    
    public boolean createsScope() {
    	return declaredVariables != null && !declaredVariables.isEmpty();
    }
    
    public Macro getEnclosingMacro() {
        TemplateElement parent = this;
        while (parent != null) {
            parent = parent.getParent();
            if (parent instanceof Macro) {
                return (Macro) parent;
            }
        }
        return null;
    }
    
    /**
     * Replace the child element prev with the current
     * @param prev
     * @param current
     */
    
    public void replace(TemplateElement prev, TemplateElement current) {
    	if (nestedBlock != null) {
    		if (prev == nestedBlock) {
    			nestedBlock = current;
    		}
    		current.parent = this;
    	} 
    	else if (nestedElements != null) {
    		for (int i=0; i<nestedElements.size(); i++) {
    			TemplateElement nestedElement = nestedElements.get(i);
    			if (nestedElement == prev) {
    				nestedElements.set(i, current);
    				current.parent = this;
    			}
    		}
    	}
    }
}
