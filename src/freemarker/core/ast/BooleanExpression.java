package freemarker.core.ast;

import freemarker.core.Environment;
import freemarker.template.*;

abstract class BooleanExpression extends Expression {

    Object _getAsTemplateModel(Environment env) throws TemplateException 
    {
        return isTrue(env) ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
    }
}
