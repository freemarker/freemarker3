/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package freemarker.core.ast;

import java.util.*;
import freemarker.template.*;
import static freemarker.template.utility.StringUtil.*;

public class TemplateHeaderElement extends TemplateNode {

	private Map<String,Expression> params;
	private Map<String,TemplateModel> values = new HashMap<String, TemplateModel>();
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
	
	public TemplateModel getParameter(String name) {
		if (values.containsKey(name)) {
			return values.get(name);
		}
		Expression exp = params.get(name);
		try {
			TemplateModel tm = exp.getAsTemplateModel(null);
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
		TemplateModel tm = getParameter(name);
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
		TemplateModel tm = getParameter(name);
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


