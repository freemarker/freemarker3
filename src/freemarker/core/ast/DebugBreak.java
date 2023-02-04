package freemarker.core.ast;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.core.StopException;
import freemarker.debug.impl.DebuggerService;
import freemarker.template.TemplateException;

/**
 * @author Attila Szegedi
 * @version $Id: DebugBreak.java,v 1.2 2004/09/11 13:11:14 stephanmueller Exp $
 */
public class DebugBreak extends TemplateElement
{
    public DebugBreak(TemplateElement nestedBlock)
    {
        this.nestedBlock = nestedBlock;
        nestedBlock.setParent(this);
        copyLocationFrom(nestedBlock);
    }
    
    public void execute(Environment env) throws TemplateException, IOException
    {
        if(!DebuggerService.suspendEnvironment(env, nestedBlock.getBeginLine()))
        {
            nestedBlock.execute(env);
        }
        else
        {
            throw new StopException(env, "Stopped by debugger");        
        }
    }

    public String getDescription()
    {
        return nestedBlock.getDescription();
    }
}
