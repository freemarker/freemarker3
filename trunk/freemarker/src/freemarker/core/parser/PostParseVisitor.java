package freemarker.core.parser;

import freemarker.core.ast.*;
import freemarker.template.*;

import java.util.*;

/**
 * A class that visits the AST after the parsing step proper,
 * and makes various checks and adjustments. 
 * @author revusky
 */

public class PostParseVisitor extends BaseASTVisitor {
	
	protected boolean stripWhitespace, limitNesting;
	
	public PostParseVisitor(boolean stripWhitespace) {
		this.stripWhitespace = stripWhitespace;
		
	}
	
	public void visit(AndExpression node) {
		visit(node.left);
		checkLiteralInBooleanContext(node.left);
		visit(node.right);
		checkLiteralInBooleanContext(node.right);
	}
	
	public void visit(AssignmentInstruction node) {
		super.visit(node);
        if (node.type == AssignmentInstruction.LOCAL) {
        	Macro macro = getContainingMacro(node);
        	if (macro == null) {
        		String msg = "\n" + node.getStartLocation() + " : " + "The local directive can only be used inside a function or macro."; 
        		errors.append(msg);
        	}
        	else for (String varname : node.getVarNames()) {
        		if (!macro.declaresScopedVariable(varname)) {
       				macro.declareScopedVariable(varname);
        		}
        	}
        }
	}
	
	public void visit(BlockAssignment node) {
		super.visit(node);
		if (node.type == AssignmentInstruction.LOCAL) {
			Macro macro = getContainingMacro(node);
			if (macro == null) {
        		String msg = "\n" + node.getStartLocation() + " : " + "The local directive can only be used inside a function or macro."; 
        		errors.append(msg);
			} else {
				if (!macro.declaresScopedVariable(node.varName)) {
					macro.declareScopedVariable(node.varName);
				}
			}
		}
	}
	
	public void visit(DollarVariable node) {
		super.visit(node);
		checkLiteralInScalarContext(node.escapedExpression);
	}
	
	public void visit(IfBlock node) {
        if (node.getChildCount() == 1) {
            ConditionalBlock cblock = (ConditionalBlock) node.getChildAt(0);
            cblock.setIsSimple(true);
            try {
            	cblock.setLocation(node.getTemplate(), cblock, node);
            } catch (ParseException pe) {
            	errors.append(pe.getMessage());
            }
            node.getParent().replace(node, cblock);
            visit(cblock);
        } else {
            super.visit(node);
        }
	}
	
	public void visit(IteratorBlock node) {
		node.declareScopedVariable(node.indexName);
		node.declareScopedVariable(node.indexName + "_has_next");
		node.declareScopedVariable(node.indexName + "_index");
		super.visit(node);
	}
	
	public void visit(MixedContent node) {
		if (node.getChildCount() == 1 && node.getParent() != null) {
			node.getParent().replace(node, node.getChildAt(0));
		}
		super.visit(node);
	}
	
	public void visit(FallbackInstruction node) {
		super.visit(node);
		if (getContainingMacro(node) == null) {
       		String msg = "\n" + node.getStartLocation() + " : " + "The fallback directive can only be used inside a macro."; 
       		errors.append(msg);
		}
	}
	
	public void visit(BreakInstruction node) {
		super.visit(node);
		TemplateElement parent = node;
		while (parent != null && !(parent instanceof SwitchBlock) && !(parent instanceof IteratorBlock)) { 
			parent = parent.getParent();
		}
		if (parent == null) {
			String msg = "\n" + node.getStartLocation() + " : The break directive can only be used within a loop or a switch-case construct.";
			errors.append(msg);
		}
	}
	
	public void visit(BodyInstruction node) {
		super.visit(node);
		Macro macro = getContainingMacro(node);
		if (macro == null) {
       		String msg = "\n" + node.getStartLocation() + " : " + "The nested directive can only be used inside a function or macro."; 
       		errors.append(msg);
		}
	}
	
	public void visit(ReturnInstruction node) {
		super.visit(node);
		TemplateElement parent = node;
		while (parent != null && !(parent instanceof Macro)) {
			parent = parent.getParent();
		}
		if (parent == null) {
       		String msg = "\n" + node.getStartLocation() + " : " + "The return directive can only be used inside a function or macro."; 
       		errors.append(msg);
		} else {
			Macro macro = (Macro) parent;
			if (!macro.isFunction() && node.returnExp != null) {
				String msg = "\n" + node.getStartLocation() + " : " + "Can only return a value from a function, not a macro";
				errors.append(msg);
			}
			else if (macro.isFunction() && node.returnExp == null) {
				String msg = "\n" + node.getStartLocation() + " : " + "A function must return a value.";
				errors.append(msg);
			}
		}
	}
	
	public void visit(ScopedDirective node) {
        TemplateElement parent = node.getParent();
        if (parent instanceof MixedContent) {
            parent = parent.getParent();
        }
        if (parent != null) {
        	for (String key : node.getVariables().keySet()) {
       			parent.declareScopedVariable(key);
        	}
        }
	}
	
	public void visit(SwitchBlock node) {
		super.visit(node);
		boolean foundDefaultCase = false;
		for (TemplateElement te : node.getCases()) {
			if (((Case) te).isDefault) {
				if (foundDefaultCase) {
					String msg = "\n" + te.getStartLocation() + " : You can only have one default case in a switch construct.";
					errors.append(msg);
				}
				foundDefaultCase = true;
			}
		}
	}
	
	public void visit(TextBlock node) {
		node.whitespaceAdjust(!stripWhitespace);
	}
	
	public void visit(OrExpression node) {
		visit(node.left);
		checkLiteralInBooleanContext(node.left);
		visit(node.right);
		checkLiteralInBooleanContext(node.right);
	}
	
	public void visit(ArithmeticExpression node) {
		visit(node.left);
		checkLiteralInNumericalContext(node.left);
		visit(node.right);
		checkLiteralInNumericalContext(node.right);
	}
	
	public void visit(ComparisonExpression node) {
		visit(node.left);
		checkLiteralInScalarContext(node.left);
		visit(node.right);
		checkLiteralInScalarContext(node.right);
	}
	
	public void visit(NumericalOutput node) {
		super.visit(node);
		checkLiteralInNumericalContext(node.expression);
	}
	
	public void visit(Dot node) {
		super.visit(node);
		TemplateModel target = node.target.literalValue();
		if (target != null && !(target instanceof TemplateHashModel)) {
			String msg = "\n" + node.getStartLocation();
			msg += ": Expression: " + node.target.getSource() + " is not a hash type.";
			errors.append(msg);
		}
	}
	
	public void visit(DynamicKeyName node) {
		super.visit(node);
		TemplateModel target = node.target.literalValue();
			if (target != null && !(target instanceof TemplateHashModel) && !(target instanceof TemplateSequenceModel)) {
			String msg = "\n" + node.getStartLocation();
			msg += ": Expression: " + node.target.getSource() + " is not a hash or sequence type.";
			errors.append(msg);
		}
		if (!(node.nameExpression instanceof Range)) {
			checkLiteralInScalarContext(node.nameExpression);
		}
	}
	
	public void visit(HashLiteral node) {
		for (Expression key : node.getKeys()) {
			checkLiteralInStringContext(key);
		}
		super.visit(node);
	}

	public void visit(Range node) {
		super.visit(node);
		checkLiteralInNumericalContext(node.left);
		if (node.right != null) {
			checkLiteralInNumericalContext(node.right);
		}
	}
	
	
	public void visit(UnaryPlusMinusExpression node) {
		checkLiteralInNumericalContext(node.target);
		super.visit(node);
	}
	
	
	protected void recurse(TemplateElement node){
		super.recurse(node);
		if (stripWhitespace) {
			node.removeIgnorableChildren();
		}
	}
	
	private void checkLiteralInBooleanContext(Expression exp) {
		TemplateModel value = exp.literalValue();
		if (value != null && !(value instanceof TemplateBooleanModel)) {
			String msg = "\n" + exp.getStartLocation();
			if (value == TemplateModel.INVALID_EXPRESSION) {
				msg += ": Expression " + exp.getSource() + " is invalid.";
			} else {
				msg += ": Expression: " + exp.getSource() + " is not a boolean (true/false) value.";
			}
			errors.append(msg);
		}
	}
	
	private void checkLiteralInStringContext(Expression exp) {
		TemplateModel value = exp.literalValue();
		if (value != null && !(value instanceof TemplateScalarModel)) {
			String msg = "\n" + exp.getStartLocation();
			if (value == TemplateModel.INVALID_EXPRESSION) {
				msg += ": Expression " + exp.getSource() + " is invalid.";
			} else {
				msg += ": Expression: " + exp.getSource() + " is not a string.";
			}
			errors.append(msg);
		}
	}
	
	private void checkLiteralInNumericalContext(Expression exp) {
		TemplateModel value = exp.literalValue();
		if (value != null && !(value instanceof TemplateNumberModel)) {
			String msg = "\n" + exp.getStartLocation();
			if (value == TemplateModel.INVALID_EXPRESSION) {
				msg += ": Expression " + exp.getSource() + " is invalid.";
			} else {
				msg += ": Expression: " + exp.getSource() + " is not a numerical value.";
			}
			errors.append(msg);
		}
	}
	
	private void checkLiteralInScalarContext(Expression exp) {
		TemplateModel value = exp.literalValue();
		if (value != null && !(value instanceof TemplateScalarModel)
			&& !(value instanceof TemplateNumberModel)
			&& !(value instanceof TemplateDateModel)) 
		{
			String msg = "\n" + exp.getStartLocation();
			if (value == TemplateModel.INVALID_EXPRESSION) {
				msg += ": Expression " + exp.getSource() + " is invalid.";
			} else {
				msg += ": Expression: " + exp.getSource() + " is not a string, date, or number.";
			}
			errors.append(msg);
		}
	}
	
	private Macro getContainingMacro(TemplateElement node) {
		TemplateElement parent = node;
		while (parent != null && !(parent instanceof Macro)) {
			parent = parent.getParent();
		}
		return (Macro) parent;
	}
}
