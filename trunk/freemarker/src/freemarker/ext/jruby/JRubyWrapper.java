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
		throw new UnsupportedOperationException("Do not know how to wrap object of type: " + obj);
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
