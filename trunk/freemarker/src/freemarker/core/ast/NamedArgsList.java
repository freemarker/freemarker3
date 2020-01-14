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

import freemarker.core.Environment;
import freemarker.core.parser.ParseException;
import freemarker.template.*;

public class NamedArgsList extends ArgsList {


    private LinkedHashMap<String,Expression> namedArgs = 
        new LinkedHashMap<String, Expression>();

    public void addNamedArg(String name, Expression exp) throws ParseException{
        if (namedArgs.containsKey(name)) throw new ParseException(
                "Error at: " + exp.getStartLocation() + "\nArgument " + name + " was already specified.");
        namedArgs.put(name, exp);
        exp.parent = this;
    }

    public Map<String, Expression> getArgs() {
        return namedArgs;
    }

    public int size() {
        return namedArgs.size();
    }

    Map<String,Expression> getCopyOfMap() {
        return (Map<String,Expression>)namedArgs.clone();
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
            + "\nCannot invoke method " + target + " with a key=value parameter list because it is not annotated.";
            throw new TemplateException(msg, env);
        }
        List<TemplateModel> result = annotatedParameterList.getParameterSequence(this, env);
        if ((target instanceof TemplateMethodModel) && !(target instanceof TemplateMethodModelEx)) {
            List<String> strings = new ArrayList<String>();
            List<String> paramNames = annotatedParameterList.getParamNames();
            for(int i = 0; i < result.size(); ++i) {
                TemplateModel value = result.get(i);
                Expression exp;
                String paramName = paramNames.get(i);
                exp = namedArgs.get(paramName);
                if(exp == null) {
                    exp = annotatedParameterList.getDefaultExpression(paramName);
                }
                strings.add(Expression.getStringValue(value, exp, env));
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
    
    void addOOParamArg(OOParamElement param) throws ParseException {
    	Expression exp = namedArgs.get(param.getName());
    	if (exp != null) {
    		
    	}
    	addNamedArg(param.getName(), param.asExp());
    }
}
