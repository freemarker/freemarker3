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
