package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.ast.TemplateNode;
import freemarker.core.builtins.StringFunctions.RegexMatchModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public class groupsBI extends ExpressionEvaluatingBuiltIn
{
    @Override
    public TemplateModel get(Environment env, BuiltInExpression caller,
            TemplateModel model) {
        if (model instanceof RegexMatchModel) {
            return ((RegexMatchModel) model).getGroups();
        }
        if (model instanceof RegexMatchModel.Match) {
            return ((RegexMatchModel.Match) model).subs;
        }
        else {
            throw TemplateNode.invalidTypeException(model, caller.getTarget(), env, "regular expression matcher");
        }
    }
}
