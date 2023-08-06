package freemarker.core.ast;

import freemarker.template.*;
import freemarker.core.Environment;
import freemarker.core.parser.*;
import java.io.*;

public class StringLiteral extends Expression implements TemplateScalarModel {
    
    private TemplateElement interpolatedOutput;
    private String value;
    private boolean raw;

    public StringLiteral() {}
    
    public StringLiteral(String value, boolean raw) {
        this.value = value;
        this.raw = raw;
    }
    
    public boolean isRaw() {
    	return raw;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }
    
    public String getValue() {
    	return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public void checkInterpolation() {
    	String src = this.source();
//    	String src = value;
        if (src.length() >5 && (src.indexOf("${") >= 0 || src.indexOf("#{") >= 0)) {
            FMLexer token_source = new FMLexer("input", value, FMLexer.LexicalState.DEFAULT, getBeginLine(), getBeginColumn() +1);
            token_source.setOnlyTextOutput(true);
            FMParser parser = new FMParser(token_source);
            parser.setTemplate(getTemplate());
            try {
                interpolatedOutput = parser.FreeMarkerText();
            }
            catch(ParseException e) {
                e.setTemplateName(getTemplate().getName());
                throw e;
            }
            this.constantValue = null;
        }
    }
    
    public TemplateModel _getAsTemplateModel(Environment env) {
        return new SimpleScalar(getStringValue(env));
    }

    public String getAsString() {
        return value;
    }
    
    public String getStringValue(Environment env) {
        if (interpolatedOutput == null) {
            return value;
        } 
        else {
            TemplateExceptionHandler teh = env.getTemplateExceptionHandler();
            env.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            try {
               return env.renderElementToString(interpolatedOutput);
            }
            catch (IOException ioe) {
                throw new TemplateException(ioe, env);
            }
            finally {
                env.setTemplateExceptionHandler(teh);
            }
        }
    }

    public boolean isLiteral() {
        return interpolatedOutput == null;
    }

    public Expression _deepClone(String name, Expression subst) {
        StringLiteral cloned = new StringLiteral(value, raw);
        cloned.interpolatedOutput = this.interpolatedOutput;
        return cloned;
    }
}
