package freemarker.core.parser;

import freemarker.core.ast.*;
import freemarker.template.*;

import java.io.StringReader;
import java.util.*;

/**
 * A class that visits the AST after the parsing step proper,
 * and makes various checks and adjustments. 
 * @author revusky
 */

public class PostParseVisitor extends BaseASTVisitor {
	
	protected boolean stripWhitespace;
	private List<ParsingProblem> problems = new ArrayList<ParsingProblem>();
	private List<EscapeBlock> escapes = new ArrayList<EscapeBlock>();
	
	private Expression escapedExpression(Expression exp) {
		if(escapes.isEmpty()) {
			return exp;
		}
		EscapeBlock lastEscape = escapes.get(escapes.size() -1);
		return lastEscape.doEscape(exp);
	}
	
	public void reportErrors() throws ParseException {
		if (!problems.isEmpty()) {
			throw new MultiParseException(problems); 
		}
	}
	
	public PostParseVisitor(boolean stripWhitespace) {
		this.stripWhitespace = stripWhitespace;
	}
	
	public void visit(InvalidExpression node) {
		problems.add(new ParsingProblem(node.getMessage() + " " + node.getSource(), node));
	}
	
	public void visit(UnclosedElement node) {
		problems.add(new ParsingProblem(node.getDescription(), node));
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
        		problems.add(problem);
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
				problems.add(new ParsingProblem("The local directive can only be used inside a function or macro.", node));
			} else {
				if (!macro.declaresScopedVariable(node.varName)) {
					macro.declareScopedVariable(node.varName);
				}
			}
		}
	}
	
	public void visit(BuiltInExpression node) {
		super.visit(node);
		if (node.findImplementation()==null) {
			ParsingProblem problem = new ParsingProblem("Unknown builtin: " + node.getName(), node);
			problems.add(problem);
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
            	problems.add(new ParsingProblem(pe.getMessage(), node));
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
			problems.add(new ParsingProblem("The noescape directive only makes sense inside an escape block.", node));
		}
		EscapeBlock last = escapes.remove(escapes.size() -1);
		super.visit(node);
		escapes.add(last);
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
	
	public void visit(AttemptBlock node) {
		PostParseVisitor ppv = new PostParseVisitor(this.stripWhitespace);
		ppv.visit(node.getAttemptBlock());
		if (!ppv.problems.isEmpty()) {
			node.setParsingProblems(ppv.problems);
		}
		visit(node.getRecoverBlock());
	}
	
	public void visit(FallbackInstruction node) {
		super.visit(node);
		if (getContainingMacro(node) == null) {
			problems.add(new ParsingProblem("The fallback directive can only be used inside a macro", node));
		}
	}
	
	public void visit(BreakInstruction node) {
		super.visit(node);
		TemplateElement parent = node;
		while (parent != null && !(parent instanceof SwitchBlock) && !(parent instanceof IteratorBlock)) { 
			parent = parent.getParent();
		}
		if (parent == null) {
			problems.add(new ParsingProblem("The break directive can only be used within a loop or a switch-case construct.", node));
		}
	}
	
	public void visit(BodyInstruction node) {
		super.visit(node);
		Macro macro = getContainingMacro(node);
		if (macro == null) {
			problems.add(new ParsingProblem("The nested directive can only be used inside a function or macro.", node));
		}
	}
	
	public void visit(ReturnInstruction node) {
		super.visit(node);
		TemplateElement parent = node;
		while (parent != null && !(parent instanceof Macro)) {
			parent = parent.getParent();
		}
		if (parent == null) {
       		problems.add(new ParsingProblem("The return directive can only be used inside a function or macro.", node));
		} else {
			Macro macro = (Macro) parent;
			if (!macro.isFunction() && node.returnExp != null) {
				problems.add(new ParsingProblem("Can only return a value from a function, not a macro", node));
			}
			else if (macro.isFunction() && node.returnExp == null) {
				problems.add(new ParsingProblem("A function must return a value.", node));
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
					problems.add(new ParsingProblem("You can only have one default case in a switch construct.", node));
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
			problems.add(new ParsingProblem("Expression " + node.target.getSource() + " is not a hash type.", node.target));
		}
	}
	
	public void visit(DynamicKeyName node) {
		super.visit(node);
		TemplateModel target = node.target.literalValue();
		if (target != null && !(target instanceof TemplateHashModel) && !(target instanceof TemplateSequenceModel)) {
			String msg = "Expression: " + node.target.getSource() + " is not a hash or sequence type.";
			problems.add(new ParsingProblem(msg, node.target));
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
				problems.add(new ParsingProblem(msg, node));
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
		if (stripWhitespace) {
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
			problems.add(new ParsingProblem(msg, exp));
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
			problems.add(new ParsingProblem(msg, exp));
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
			problems.add(new ParsingProblem(msg, exp));
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
			problems.add(new ParsingProblem(msg, exp));
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
