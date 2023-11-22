package freemarker.builtins;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import freemarker.core.Environment;
import freemarker.core.nodes.generated.BuiltInExpression;
import freemarker.core.variables.*;
import freemarker.template.TemplateException;

/**
 * Implementations of builtins for standard functions
 * that operate on sequences
 */
public class DateTime extends ExpressionEvaluatingBuiltIn {

    private final int dateType;

    public DateTime(int dateType) {
        this.dateType = dateType;
    }

    public Object get(Environment env, BuiltInExpression caller,
            Object model) {
        if (model instanceof WrappedDate) {
            WrappedDate dmodel = (WrappedDate) model;
            int dtype = dmodel.getDateType();
            // Any date model can be coerced into its own type
            if (dateType == dtype) {
                return dmodel;
            }
            // unknown and datetime can be coerced into any date type
            if (dtype == WrappedDate.UNKNOWN || dtype == WrappedDate.DATETIME) {
                return new DateWrapper(dmodel.getAsDate(), dateType);
            }
            throw new TemplateException(
                    "Cannot convert " + WrappedDate.TYPE_NAMES.get(dtype)
                            + " into " + WrappedDate.TYPE_NAMES.get(dateType),
                    env);
        } else {
            return new DateParser(model.toString(), dateType, caller, env);
        }
    }

    static class DateParser implements WrappedDate, WrappedMethod, Hash {
        private final String text;
        private final Environment env;
        private final DateFormat defaultFormat;
        private BuiltInExpression caller;
        private int dateType;
        private Date cachedValue;

        DateParser(String text, int dateType, BuiltInExpression caller, Environment env) {
            this.text = text;
            this.env = env;
            this.caller = caller;
            this.dateType = dateType;
            this.defaultFormat = env.getDateFormatObject(dateType);
        }

        public Date getAsDate() {
            if (cachedValue == null) {
                cachedValue = parse(defaultFormat);
            }
            return cachedValue;
        }

        public int getDateType() {
            return dateType;
        }

        public Object get(String pattern) {
            return new DateWrapper(
                    parse(env.getDateFormatObject(dateType, pattern)),
                    dateType);
        }

        public Object exec(Object... arguments) {
            if (arguments.length != 1) {
                throw new EvaluationException(
                        "string?" + caller.getName() + "(...) requires exactly 1 argument.");
            }
            return get((String) arguments[0]);
        }

        private Date parse(DateFormat df) {
            try {
                return df.parse(text);
            } catch (java.text.ParseException e) {
                String mess = "Error: " + caller.getLocation()
                        + "\nExpecting a date here, found: " + text;
                throw new EvaluationException(mess);
            }
        }
    }
}
