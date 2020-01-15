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

package freemarker.ext.jruby;

import freemarker.template.*;
import org.jruby.*;
import org.jruby.javasupport.JavaObject;

/**
 * ObjectWrapper for JRuby objects. This code is in an 
 * incomplete, experimental state.
 * @author revusky
 */

public class JRubyWrapper implements ObjectWrapper {
	
	private ObjectWrapper fallbackWrapper = new DefaultObjectWrapper();
	
	public TemplateModel wrap(Object obj) throws TemplateModelException {
		if (obj == null) {
			return TemplateModel.JAVA_NULL;
		}
		if (obj instanceof RubyObject) {
			return wrap((RubyObject) obj);
		}
		if (obj instanceof TemplateModel) {
			return (TemplateModel) obj;
		}
		return fallbackWrapper.wrap(obj);
	}
	
	public TemplateModel wrap(RubyObject robj) throws TemplateModelException {
		if (robj instanceof RubyNil) {
			return TemplateModel.JAVA_NULL;
		}
		if (robj instanceof JavaObject) {
			Object jobj = ((JavaObject) robj).getValue();
			return Configuration.getCurrentObjectWrapper().wrap(jobj);
		}
		if (robj instanceof RubyString) {
			return new SimpleScalar(robj.toString());
		}
		if (robj instanceof RubyNumeric) { 
			// This can be refined later.
			double d = ((RubyNumeric) robj).getDoubleValue();
			return new SimpleNumber(new Double(d));
		}
		if (robj instanceof RubyTime) {
			java.util.Date date = ((RubyTime) robj).getJavaDate();
			return Configuration.getCurrentObjectWrapper().wrap(date);
		}
		return new RubyModel(robj, this);
	}
}
