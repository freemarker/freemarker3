package freemarker.template;

import freemarker.core.ast.*;
import freemarker.core.parser.MultiParseException;
import freemarker.core.parser.ParseException;
import freemarker.core.parser.ParsingProblem;
import freemarker.template.*;

import java.io.StringReader;
import java.util.*;

/**
 * A class that visits the AST after the parsing step proper,
 * and makes various checks and adjustments. 
 * @author revusky
 */

public class PostParseVisitor extends BaseASTVisitor {
	
	private Template template;
	private List<EscapeBlock> escapes = new ArrayList<EscapeBlock>();
	
	public PostParseVisitor(Template template) {
		this.template = template;
	}
	
	private Expression escapedExpression(Expression exp) {
		if(escapes.isEmpty()) {
			return exp;
		}
		EscapeBlock lastEscape = escapes.get(escapes.size() -1);
		return lastEscape.doEscape(exp);
	}
	
	public void visit(InvalidExpression node) {
		template.addParsingProblem(new ParsingProblem(node.getMessage() + " " + node.getSource(), node));
	}
	
	public void visit(UnclosedElement node) {
		template.addParsingProblem(new ParsingProblem(node.getDescription(), node));
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
        		ParsingProblem problem = new ParsingProblem("The local directive can only be used inside a function or macro.", node);
        		template.addParsingProblem(problem);
        	}
        	else for (String varname : node.getVarNames()) {
        		if (!macro.declaresVariable(varname)) {
       				macro.declareVariable(varname);
        		}
        	}
        }
	}
	
	public void visit(BlockAssignment node) {
		super.visit(node);
		if (node.type == AssignmentInstruction.LOCAL) {
			Macro macro = getContainingMacro(node);
			if (macro == null) {
				template.addParsingProblem(new ParsingProblem("The local directive can only be used inside a function or macro.", node));
			} else {
				if (!macro.declaresVariable(node.varName)) {
					macro.declareVariable(node.varName);
				}
			}
		}
	}
	
	public void visit(BuiltInExpression node) {
		super.visit(node);
		if (node.findImplementation()==null) {
			ParsingProblem problem = new ParsingProblem("Unknown builtin: " + node.getName(), node);
			template.addParsingProblem(problem);
		}
	}
	
	public void visit(DollarVariable node) {
		super.visit(node);
		Expression escapedExpression = escapedExpression(node.expression);
		node.setEscapedExpression(escapedExpression);
		checkLiteralInScalarContext(escapedExpression);
	}
	
	public void visit(IfBlock node) {
        if (node.getChildCount() == 1) {
            ConditionalBlock cblock = (ConditionalBlock) node.getChildAt(0);
            cblock.setIsSimple(true);
            try {
            	cblock.setLocation(node.getTemplate(), cblock, node);
            } catch (ParseException pe) {
            	template.addParsingProblem(new ParsingProblem(pe.getMessage(), node));
            }
            node.getParent().replace(node, cblock);
            visit(cblock);
        } else {
            super.visit(node);
        }
	}
	
	public void visit(EscapeBlock node) {
		Expression escapedExpression = escapedExpression(node.expr);
		node.setEscapedExpression(escapedExpression);
		escapes.add(node);
		super.visit(node);
		escapes.remove(escapes.size() -1);
	}
	
	public void visit(NoEscapeBlock node) {
		TemplateElement parent = node;
		while (parent != null && !(parent instanceof EscapeBlock)) {
			parent = parent.getParent();
		}
		if (parent == null) {
			String msg = "\n" + node.getStartLocation();
			template.addParsingProblem(new ParsingProblem("The noescape directive only makes sense inside an escape block.", node));
		}
		EscapeBlock last = escapes.remove(escapes.size() -1);
		super.visit(node);
		escapes.add(last);
	}
	
	public void visit(IteratorBlock node) {
		node.declareVariable(node.indexName);
		node.declareVariable(node.indexName + "_has_next");
		node.declareVariable(node.indexName + "_index");
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
			template.addParsingProblem(new ParsingProblem("The fallback directive can only be used inside a macro", node));
		}
	}
	
	public void visit(BreakInstruction node) {
		super.visit(node);
		TemplateElement parent = node;
		while (parent != null && !(parent instanceof SwitchBlock) && !(parent instanceof IteratorBlock)) { 
			parent = parent.getParent();
		}
		if (parent == null) {
			template.addParsingProblem(new ParsingProblem("The break directive can only be used within a loop or a switch-case construct.", node));
		}
	}
	
	public void visit(BodyInstruction node) {
		super.visit(node);
		Macro macro = getContainingMacro(node);
		if (macro == null) {
			template.addParsingProblem(new ParsingProblem("The nested directive can only be used inside a function or macro.", node));
		}
	}
	
	public void visit(ReturnInstruction node) {
		super.visit(node);
		TemplateElement parent = node;
		while (parent != null && !(parent instanceof Macro)) {
			parent = parent.getParent();
		}
		if (parent == null) {
       		template.addParsingProblem(new ParsingProblem("The return directive can only be used inside a function or macro.", node));
		} else {
			Macro macro = (Macro) parent;
			if (!macro.isFunction() && node.returnExp != null) {
				template.addParsingProblem(new ParsingProblem("Can only return a value from a function, not a macro", node));
			}
			else if (macro.isFunction() && node.returnExp == null) {
				template.addParsingProblem(new ParsingProblem("A function must return a value.", node));
			}
		}
	}
	
	public void visit(VarDirective node) {
        TemplateElement parent = node.getParent();
        if (parent instanceof MixedContent) {
            parent = parent.getParent();
        }
        if (parent != null) {
        	for (String key : node.getVariables().keySet()) {
       			parent.declareVariable(key);
        	}
        }
	}
	
	public void visit(SwitchBlock node) {
		super.visit(node);
		boolean foundDefaultCase = false;
		for (TemplateElement te : node.getCases()) {
			if (((Case) te).isDefault) {
				if (foundDefaultCase) {
					template.addParsingProblem(new ParsingProblem("You can only have one default case in a switch construct.", node));
				}
				foundDefaultCase = true;
			}
		}
	}
	
	public void visit(TextBlock node) {
		node.whitespaceAdjust(!template.stripWhitespace);
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
			template.addParsingProblem(new ParsingProblem("Expression " + node.target.getSource() + " is not a hash type.", node.target));
		}
	}
	
	public void visit(DynamicKeyName node) {
		super.visit(node);
		TemplateModel target = node.target.literalValue();
		if (target != null && !(target instanceof TemplateHashModel) && !(target instanceof TemplateSequenceModel)) {
			String msg = "Expression: " + node.target.getSource() + " is not a hash or sequence type.";
			template.addParsingProblem(new ParsingProblem(msg, node.target));
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
	
	public void visit(StringLiteral node) {
		if (!node.isRaw()) {
			try {
				node.checkInterpolation();
			} catch (ParseException pe) {
				String msg = "Error in string " + node.getStartLocation();
				msg += "\n" + pe.getMessage();
				template.addParsingProblem(new ParsingProblem(msg, node));
			}
		}
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
		if (template.stripWhitespace) {
			node.removeIgnorableChildren();
		}
	}
	
	private void checkLiteralInBooleanContext(Expression exp) {
		TemplateModel value = exp.literalValue();
		if (value != null && !(value instanceof TemplateBooleanModel)) {
			String msg;
			if (value == TemplateModel.INVALID_EXPRESSION) {
				msg = "Invalid expression: " + exp.getSource();
			} else {
				msg = "Expression: " + exp.getSource() + " is not a boolean (true/false) value.";
			}
			template.addParsingProblem(new ParsingProblem(msg, exp));
		}
	}
	
	private void checkLiteralInStringContext(Expression exp) {
		TemplateModel value = exp.literalValue();
		if (value != null && !(value instanceof TemplateScalarModel)) {
			String msg;
			if (value == TemplateModel.INVALID_EXPRESSION) {
				msg = "Invalid expression: " + exp.getSource();
			} else {
				msg = "Expression: " + exp.getSource() + " is not a string.";
			}
			template.addParsingProblem(new ParsingProblem(msg, exp));
		}
	}
	
	private void checkLiteralInNumericalContext(Expression exp) {
		TemplateModel value = exp.literalValue();
		if (value != null && !(value instanceof TemplateNumberModel)) {
			String msg;
			if (value == TemplateModel.INVALID_EXPRESSION) {
				msg = "Invalid expression: " + exp.getSource();
			} else {
				msg = "Expression: " + exp.getSource() + " is not a numerical value.";
			}
			template.addParsingProblem(new ParsingProblem(msg, exp));
		}
	}
	
	private void checkLiteralInScalarContext(Expression exp) {
		TemplateModel value = exp.literalValue();
		if (value != null && !(value instanceof TemplateScalarModel)
			&& !(value instanceof TemplateNumberModel)
			&& !(value instanceof TemplateDateModel)) 
		{
			String msg;
			if (value == TemplateModel.INVALID_EXPRESSION) {
				msg = "Invalid expression: " + exp.getSource();
			} else {
				msg = "Expression: " + exp.getSource() + " is not a string, date, or number.";
			}
			template.addParsingProblem(new ParsingProblem(msg, exp));
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
