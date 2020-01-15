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

import org.jruby.*;
import freemarker.template.*;

public class RubyModel 
implements TemplateScalarModel, TemplateNumberModel, TemplateHashModel, TemplateSequenceModel {
	
	private RubyObject robj;
	private JRubyWrapper wrapper;
	
	public RubyModel(RubyObject robj, JRubyWrapper wrapper) {
		this.robj = robj;
		this.wrapper = wrapper;
	}
	
	public String getAsString() throws TemplateModelException {
		return robj.toString();
	}
	
	public Number getAsNumber() throws TemplateModelException {
		RubyFloat rf = robj.convertToFloat();
		return new Double(rf.getDoubleValue());
	}
	
	public TemplateModel get(String key) throws TemplateModelException {
		RubyHash rh = robj.convertToHash();
		Object val = rh.get(key);
		if (val instanceof RubyObject) {
			return wrapper.wrap(val);
		} 
		return Configuration.getCurrentObjectWrapper().wrap(val);
	}
	
	public boolean isEmpty() {
		RubyHash rh = robj.convertToHash();
		return rh.isEmpty();
	}
	
	public int size() {
		RubyArray ra = robj.convertToArray();
		return ra.size();
	}
	
	public TemplateModel get(int i) throws TemplateModelException {
		RubyArray ra = robj.convertToArray();
		Object val = ra.get(i);
		if (val instanceof RubyObject) {
			return wrapper.wrap((RubyObject) val);
		}
		return Configuration.getCurrentObjectWrapper().wrap(val);
	}
	
}
