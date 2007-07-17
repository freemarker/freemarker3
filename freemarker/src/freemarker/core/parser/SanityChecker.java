package freemarker.core.parser;

import freemarker.core.ast.*;
import freemarker.template.*;

public class SanityChecker extends BaseASTVisitor {
	
	private StringBuffer errors = new StringBuffer();
	
	public void reportErrors() throws ParseException {
		if (errors.length() > 0) {
			throw new ParseException(errors.toString());
		}
	}
	
	
	private TemplateModel literalExpToTemplateModel(Expression exp) {
		try {
			return exp.getAsTemplateModel(null);
		} catch (Exception te) {
			return TemplateModel.INVALID_EXPRESSION;
		}
	}
	
	private void checkLiteralInBooleanContext(Expression exp) {
		if (exp.isLiteral()) {
			TemplateModel value = literalExpToTemplateModel(exp);
			if (!(value instanceof TemplateBooleanModel)) {
				String msg = exp.getStartLocation();
				if (value == TemplateModel.INVALID_EXPRESSION) {
					msg += ": Expression " + exp.getSource() + " is invalid.\n";
				} else {
					msg += ": Expression: " + exp.getSource() + " is not a boolean (true/false) value.\n";
				}
				errors.append(msg);
			}
		}
	}
	
	private void checkLiteralInStringContext(Expression exp) {
		if (exp.isLiteral()) {
			TemplateModel value = literalExpToTemplateModel(exp);
			if (!(value instanceof TemplateScalarModel)) {
				String msg = exp.getStartLocation();
				if (value == TemplateModel.INVALID_EXPRESSION) {
					msg += ": Expression " + exp.getSource() + " is invalid.\n";
				} else {
					msg += ": Expression: " + exp.getSource() + " is not a string.\n";
				}
				errors.append(msg);
			}
		}
	}
	
	private void checkLiteralInNumericalContext(Expression exp) {
		if (exp.isLiteral()) {
			TemplateModel value = literalExpToTemplateModel(exp);
			if (!(value instanceof TemplateNumberModel)) {
				String msg = exp.getStartLocation();
				if (value == TemplateModel.INVALID_EXPRESSION) {
					msg += ": Expression " + exp.getSource() + " is invalid.\n";
				} else {
					msg += ": Expression: " + exp.getSource() + " is not a numerical value.\n";
				}
				errors.append(msg);
			}
		}
	}
	
	private void checkLiteralInScalarContext(Expression exp) {
		if (exp.isLiteral()) {
			TemplateModel value = literalExpToTemplateModel(exp);
			if (!(value instanceof TemplateScalarModel)
				&& !(value instanceof TemplateNumberModel)
				&& !(value instanceof TemplateDateModel)) 
			{
				String msg = exp.getStartLocation();
				if (value == TemplateModel.INVALID_EXPRESSION) {
					msg += ": Expression " + exp.getSource() + " is invalid.\n";
				} else {
					msg += ": Expression: " + exp.getSource() + " is not a string, date, or number.\n";
				}
				errors.append(msg);
			}
		}
	}
	
	public void visit(AndExpression node) {
		visit(node.left);
		checkLiteralInBooleanContext(node.left);
		visit(node.right);
		checkLiteralInBooleanContext(node.right);
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
	
	public void visit(DollarVariable node) {
		super.visit(node);
		checkLiteralInScalarContext(node.escapedExpression);
	}
	
	public void visit(NumericalOutput node) {
		super.visit(node);
		checkLiteralInNumericalContext(node.expression);
	}
	
	public void visit(Dot node) {
		super.visit(node);
		if (node.target.isLiteral()) {
			TemplateModel target = literalExpToTemplateModel(node.target);
			if (!(target instanceof TemplateHashModel)) {
				String msg = node.getStartLocation();
				msg += ": Expression: " + node.target.getSource() + " is not a hash type.\n";
				errors.append(msg);
			}
		}
	}
	
	public void visit(DynamicKeyName node) {
		super.visit(node);
		if (node.target.isLiteral()) {
			TemplateModel target = literalExpToTemplateModel(node.target);
			if (!(target instanceof TemplateHashModel) && !(target instanceof TemplateSequenceModel)) {
				String msg = node.getStartLocation();
				msg += ": Expression: " + node.target.getSource() + " is not a hash or sequence type.\n";
				errors.append(msg);
			}
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
}
