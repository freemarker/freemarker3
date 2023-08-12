package freemarker.core.ast;

import java.util.*;

import freemarker.core.parser.ast.Identifier;
import freemarker.core.parser.ast.TemplateNode;
import freemarker.template.*;
import static freemarker.template.utility.StringUtil.*;
import static freemarker.ext.beans.ObjectWrapper.*;

public class TemplateHeaderElement extends TemplateNode {

	private Map<String,Expression> params;
	private Map<String,Object> values = new HashMap<String, Object>();
	protected TemplateElement parent;
	
	public TemplateHeaderElement(Map<String,Expression> params) {
		this.params = params;
	}
	
	public Map<String,Expression> getParams() {
		return Collections.unmodifiableMap(params);
	}
	
	public boolean hasParameter(String name) {
		return params.containsKey(name);
	}
	
	public void addParameter(String name, Expression exp) {
		params.put(name, exp);
		values.remove(name);
	}
	
	public Object getParameter(String name) {
		if (values.containsKey(name)) {
			return values.get(name);
		}
		Expression exp = params.get(name);
		try {
			Object tm = exp.evaluate(null);
			values.put(name, tm);
			return tm;
		} catch (TemplateException te) {
			if (exp instanceof Identifier) {
				String s = ((Identifier) exp).getName();
				values.put(name, s);
                return s;
			}
			values.put(name, null);
			return null;
		}
	}
	
	public String getStringParameter(String name) {
		Object tm = getParameter(name);
		try {
			return asString(tm);
		} catch (ClassCastException cce) {
		    throw new IllegalArgumentException("Parameter " + name + " is not a string.");
		}
	}

	public boolean getBooleanParameter(String name) {
		Object tm = getParameter(name);
		if (tm == null) {
			throw new IllegalArgumentException("No parameter " + name);
		}
		if (isBoolean(tm)) {
			return asBoolean(tm);
		}
		if (isString(tm)) {
			try {
				return getYesNo(asString(tm));
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		}
		throw new IllegalArgumentException("Parameter " + name + " is not a boolean type.");
	}

	public TemplateElement getParent() {
	    return parent;
	}
}


