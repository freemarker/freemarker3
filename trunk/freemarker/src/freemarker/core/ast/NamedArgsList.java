/*
 * Copyright (c) 2006 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */

package freemarker.core.ast;

import java.util.*;

import freemarker.core.Environment;
import freemarker.core.parser.ParseException;
import freemarker.template.*;

public class NamedArgsList extends ArgsList {
	
	
	private Map<String,Expression> namedArgs = new LinkedHashMap<String, Expression>();
	
	public void addNamedArg(String name, Expression exp) throws ParseException{
		if (namedArgs.containsKey(name)) throw new ParseException("Error at: " + exp.getStartLocation() + "\nArgument " + name + " was already specified.");
		namedArgs.put(name, exp);
	}
	
	public Map<String, Expression> getArgs() {
		return namedArgs;
	}
	
	Map<String,Expression> getCopyOfMap() {
		return new LinkedHashMap<String, Expression>(namedArgs);
	}

	Map<String, TemplateModel> getParameterMap(TemplateModel tm, Environment env) throws TemplateException {
    	Map<String, TemplateModel> result = null; 
    	ParameterList annotatedParameterList = ArgsList.getParameterList(tm);
    	if (annotatedParameterList == null) {
    		result = new HashMap<String, TemplateModel>();
   			for (String paramName : namedArgs.keySet()) {
   				Expression exp = namedArgs.get(paramName);
   				TemplateModel value = exp.getAsTemplateModel(env);
   				TemplateNode.assertIsDefined(value, exp, env);
   				result.put(paramName, value);
   			}
    	}
    	else {
    		result = annotatedParameterList.getParameterMap(this, env);
    	}
    	return result;
    }
	
	List getParameterSequence(TemplateModel target, Environment env) throws TemplateException {
		ParameterList annotatedParameterList = getParameterList(target);
		if (annotatedParameterList == null) {
			String msg = "Error at: " + getStartLocation() 
            + "\nCannot invoke this method with a key=value parameter list because it is not annotated.";
			throw new TemplateException(msg, env);
		}
		List<TemplateModel> result = annotatedParameterList.getParameterSequence(this, env);
		if ((target instanceof TemplateMethodModel) && !(target instanceof TemplateMethodModelEx)) {
			List<String> strings = new ArrayList<String>();
			for (TemplateModel value : result) {
				try {
					strings.add(((TemplateScalarModel) value).getAsString());
				} catch (ClassCastException cce) {
					String msg = "Error at: " + getStartLocation() 
		             + "\nThis method can only be invoked with string arguments.";
					throw new TemplateException(msg, env);
				}
			}
			return strings;
		}
		return result;
	}

	
	public String getStartLocation() {
		for (Expression exp : namedArgs.values()) {
			return exp.getStartLocation();
		}
		return "";
	}
	
	ArgsList deepClone(String name, Expression subst) {
		NamedArgsList result = new NamedArgsList();
		for (Map.Entry<String, Expression> entry : namedArgs.entrySet()) {
			try {
				result.addNamedArg(entry.getKey(), entry.getValue());
			} catch (ParseException pe) {} // This can't happen anyway, since we already checked for repeats
		}
		return result;
	}
}
