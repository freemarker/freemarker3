package freemarker.builtins;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.nodes.generated.TemplateNode;
import freemarker.builtins.StringFunctions.RegexMatchModel;
import static freemarker.core.variables.Wrap.unwrap;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
public class groupsBI extends ExpressionEvaluatingBuiltIn
{
    @Override
    public Object get(Environment env, BuiltInExpression caller,
            Object model) {
        model = unwrap(model);
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
