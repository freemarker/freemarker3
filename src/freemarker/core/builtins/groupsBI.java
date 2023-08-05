package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;
import freemarker.core.parser.ast.TemplateNode;
import freemarker.core.builtins.StringFunctions.RegexMatchModel;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public class groupsBI extends ExpressionEvaluatingBuiltIn
{
    @Override
    public Object get(Environment env, BuiltInExpression caller,
            Object model) {
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
