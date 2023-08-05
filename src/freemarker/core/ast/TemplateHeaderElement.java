package freemarker.core.ast;

import java.util.*;

import freemarker.core.parser.ast.BaseNode;
import freemarker.template.*;
import static freemarker.template.utility.StringUtil.*;

public class TemplateHeaderElement extends BaseNode {

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
			Object tm = exp.getAsTemplateModel(null);
			values.put(name, tm);
			return tm;
		} catch (TemplateException te) {
			if (exp instanceof Identifier) {
				String s = ((Identifier) exp).getName();
				TemplateModel result = new SimpleScalar(s);
				values.put(name, result);
				return result;
			}
			values.put(name, null);
			return null;
		}
	}
	
	public String getStringParameter(String name) {
		Object tm = getParameter(name);
		if (tm instanceof TemplateScalarModel) {
			try {
				return ((TemplateScalarModel) tm).getAsString();
			} catch (TemplateModelException tme) {
				throw new IllegalArgumentException(tme);
			}
		} 
		throw new IllegalArgumentException("Parameter " + name + " is not a string.");
	}

	public boolean getBooleanParameter(String name) {
		Object tm = getParameter(name);
		if (tm == null) {
			throw new IllegalArgumentException("No parameter " + name);
		}
		if (tm instanceof TemplateBooleanModel) {
			try {
				return ((TemplateBooleanModel) tm).getAsBoolean();
			} catch (TemplateModelException te) {
				throw new IllegalArgumentException(te);
			}
		}
		if (tm instanceof TemplateScalarModel) {
			try {
				return getYesNo(((TemplateScalarModel) tm).getAsString());
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


