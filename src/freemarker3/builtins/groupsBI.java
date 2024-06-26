package freemarker3.builtins;

import freemarker3.core.Environment;
import freemarker3.core.nodes.generated.BuiltInExpression;
import freemarker3.core.nodes.generated.TemplateNode;
import freemarker3.builtins.StringFunctions.RegexMatchModel;
import static freemarker3.core.variables.Wrap.unwrap;

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
