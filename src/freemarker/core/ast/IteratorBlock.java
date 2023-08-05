package freemarker.core.ast;

import java.io.IOException;

import freemarker.template.*;
import freemarker.core.*;

/**
 * An instruction that processes a list or foreach block
 */
public class IteratorBlock extends TemplateElement {

    private String indexName;
    private Expression listExpression;
    private boolean isForEach;

    /**
     * @param listExpression a variable referring to a sequence or collection
     * @param indexName an arbitrary index variable name
     * @param nestedBlock the nestedBlock to iterate over
     */
    public IteratorBlock(Expression listExpression,
                          String indexName,
                          TemplateElement nestedBlock,
                          boolean isForEach) 
    {
        this.listExpression = listExpression;
        this.indexName = indexName;
        this.isForEach = isForEach;
        this.setNestedBlock(nestedBlock);
    }
    
    public String getIndexName() {
    	return indexName;
    }
    
    public Expression getListExpression() {
    	return listExpression;
    }

    public void execute(Environment env) throws TemplateException, IOException 
    {
        Object baseModel = listExpression.getAsTemplateModel(env);
        assertNonNull(baseModel, listExpression, env);
        env.process(new LoopContext(this, env.getCurrentScope(), baseModel)); // REVISIT
    }

    public String getDescription() {
        if (isForEach) {
            return "foreach " + indexName + " in " + listExpression; 

        }
        else {
            return "list " + listExpression + " as " + indexName;
        }
    }
}
