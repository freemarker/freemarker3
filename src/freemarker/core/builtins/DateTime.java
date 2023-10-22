package freemarker.core.builtins;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import freemarker.core.Environment;
import freemarker.core.parser.ast.BuiltInExpression;
import freemarker.core.parser.ast.TemplateNode;
import freemarker.ext.beans.DateModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

import static freemarker.ext.beans.ObjectWrapper.*;

/**
 * Implementations of builtins for standard functions
 * that operate on sequences
 * @version $Id: $
 */
public class DateTime extends ExpressionEvaluatingBuiltIn {

    private final int dateType;
    
    public DateTime(int dateType) {
        this.dateType = dateType;
    }

    public Object get(Environment env, BuiltInExpression caller, 
            Object model) 
    {
        if (model instanceof TemplateDateModel) {
            TemplateDateModel dmodel = (TemplateDateModel) model;
            int dtype = dmodel.getDateType();
            // Any date model can be coerced into its own type
            if(dateType == dtype) {
                return dmodel;
            }
            // unknown and datetime can be coerced into any date type
            if(dtype == TemplateDateModel.UNKNOWN || dtype == TemplateDateModel.DATETIME) {
                return new DateModel(dmodel.getAsDate(), dateType);
            }
            throw new TemplateException(
                    "Cannot convert " + TemplateDateModel.TYPE_NAMES.get(dtype)
                    + " into " + TemplateDateModel.TYPE_NAMES.get(dateType), env);
        }
        else if (isString(model)) {
            return new DateParser(asString(model), dateType, caller,  env);
        }
        else {
            throw TemplateNode.invalidTypeException(model, caller, env, 
                    "time/date or string");
        }
    }

    static class DateParser
    implements
    TemplateDateModel,
    TemplateMethodModel,
    TemplateHashModel
    {
        private final String text;
        private final Environment env;
        private final DateFormat defaultFormat;
        private BuiltInExpression caller;
        private int dateType;
        private Date cachedValue;

        DateParser(String text, int dateType, BuiltInExpression caller, Environment env)
        {
            this.text = text;
            this.env = env;
            this.caller = caller;
            this.dateType = dateType;
            this.defaultFormat = env.getDateFormatObject(dateType);
        }

        public Date getAsDate() {
            if(cachedValue == null) {
                cachedValue = parse(defaultFormat);
            }
            return cachedValue;
        }

        public int getDateType() {
            return dateType;
        }

        public Object get(String pattern) {
            return new DateModel(
                    parse(env.getDateFormatObject(dateType, pattern)),
                    dateType);
        }

        public Object exec(List arguments)
        {
            if (arguments.size() != 1) {
                throw new TemplateModelException(
                        "string?" + caller.getName() + "(...) requires exactly 1 argument.");
            }
            return get((String) arguments.get(0));
        }

        public boolean isEmpty()
        {
            return false;
        }

        private Date parse(DateFormat df) 
        {
            try {
                return df.parse(text);
            }
            catch(java.text.ParseException e) {
                String mess = "Error: " + caller.getLocation()
                + "\nExpecting a date here, found: " + text;
                throw new TemplateModelException(mess);
            }
        }
    }
}
