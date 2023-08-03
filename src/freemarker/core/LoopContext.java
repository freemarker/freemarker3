package freemarker.core;

import java.io.IOException;
import java.util.Iterator;

import freemarker.core.ast.*;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateSequenceModel;

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
    	Environment env = getEnvironment();
        if (list instanceof TemplateCollectionModel) {
            TemplateCollectionModel baseListModel = (TemplateCollectionModel) list;
            Iterator<Object> it = baseListModel.iterator();
            hasNext = it.hasNext();
            while (hasNext) {
            	clear();
                loopVar = it.next();
                hasNext = it.hasNext();
                put(iteratorBlock.getIndexName(), loopVar);
                TemplateBooleanModel hasNextModel = hasNext ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
                put(iteratorBlock.getIndexName() + "_has_next", hasNextModel);
                put(iteratorBlock.getIndexName() + "_index", new SimpleNumber(index));
                TemplateElement nestedBlock = iteratorBlock.getNestedBlock();
                if (nestedBlock != null) {
                    env.render(nestedBlock);
                }
                index++;
            }
        }
        else if (list instanceof TemplateSequenceModel) {
            TemplateSequenceModel tsm = (TemplateSequenceModel) list;
            int size = tsm.size();
            for (index =0; index <size; index++) {
            	clear();
                loopVar = tsm.get(index);
                put(iteratorBlock.getIndexName(), loopVar);
                hasNext = (size > index + 1);
                TemplateBooleanModel hasNextModel = (size > index +1) ?  TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
                put(iteratorBlock.getIndexName() + "_has_next", hasNextModel);
                put(iteratorBlock.getIndexName() + "_index", new SimpleNumber(index));
                TemplateElement nestedBlock = iteratorBlock.getNestedBlock();
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
