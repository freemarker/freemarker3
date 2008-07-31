package freemarker.core.ooparam;

import freemarker.template.*;
import freemarker.core.ast.*;

public class OOParamModel implements TemplateModel { 
// implements TemplateScalarModel, TemplateHashModel, TemplateDirectiveModel, TemplateNodeModel {
	
	
	private OOParamElement param;
	private ParameterList parameters;
	
	public OOParamModel(OOParamElement param) {
		this.param = param;
	}
	

}
