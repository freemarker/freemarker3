package freemarker.core;

import java.io.IOException;
import java.util.Iterator;

import freemarker.core.nodes.generated.IteratorBlock;
import freemarker.core.nodes.generated.TemplateElement;
import freemarker.core.nodes.generated.TemplateNode;

import freemarker.template.TemplateException;
import freemarker.template.WrappedSequence;

import static freemarker.ext.beans.ObjectWrapper.isIterable;
import static freemarker.ext.beans.ObjectWrapper.asIterator;
import static freemarker.ext.beans.ObjectWrapper.wrap;

/**
 * Represents the local context of an iterator block  
 */

public class LoopContext extends BlockScope {
    private boolean hasNext;
    private Object loopVar;
    private int index;
    private Object list;
    
    public LoopContext(IteratorBlock iteratorBlock, Scope enclosingScope, Object list) {
    	super(iteratorBlock, enclosingScope);
        this.list = list;
    }
    
    public void runLoop() throws TemplateException, IOException {
    	IteratorBlock iteratorBlock = (IteratorBlock) block;
        TemplateElement nestedBlock = iteratorBlock.firstChildOfType(TemplateElement.class);
    	Environment env = getEnvironment();
        if (isIterable(list)) {
            Iterator<?> it = asIterator(list);
            hasNext = it.hasNext();
            while (hasNext) {
            	clear();
                loopVar = it.next();
                hasNext = it.hasNext();
                put(iteratorBlock.getIndexName(), wrap(loopVar));
                //put(iteratorBlock.getIndexName(), loopVar);
                put(iteratorBlock.getIndexName() + "_has_next", hasNext);
                put(iteratorBlock.getIndexName() + "_index", index);
                if (nestedBlock != null) {
                    env.render(nestedBlock);
                }
                index++;
            }
        }
        else if (list instanceof WrappedSequence) {
            WrappedSequence tsm = (WrappedSequence) list;
            int size = tsm.size();
            for (index =0; index <size; index++) {
            	clear();
                loopVar = tsm.get(index);
                put(iteratorBlock.getIndexName(), loopVar);
                hasNext = (size > index + 1);
                put(iteratorBlock.getIndexName() + "_has_next", hasNext);
                put(iteratorBlock.getIndexName() + "_index", index);
                if (nestedBlock != null) {
                    env.render(nestedBlock);
                }
            }
        }
        else {
            throw TemplateNode.invalidTypeException(list, iteratorBlock.getListExpression(), env, "collection or sequence");
        }
    }
}
