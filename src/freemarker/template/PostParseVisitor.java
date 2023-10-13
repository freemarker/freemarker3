package freemarker.template;

import freemarker.core.Configurable;
import freemarker.core.ast.*;
import freemarker.core.parser.ast.*;
import freemarker.core.parser.Node;
import freemarker.core.parser.ParseException;
import freemarker.core.parser.ParsingProblem;
import freemarker.core.parser.ast.Expression;
import freemarker.core.parser.ast.BuiltInExpression;
import freemarker.core.parser.ast.FallbackInstruction;
import freemarker.core.parser.ast.ImportDeclaration;
import freemarker.core.parser.ast.Interpolation;
import freemarker.core.parser.ast.StringLiteral;
import freemarker.core.parser.ast.TemplateNode;
import freemarker.template.utility.DeepUnwrap;

import static freemarker.ext.beans.ObjectWrapper.*;
import java.util.*;

/**
 * A class that visits the AST after the parsing step proper,
 * and makes various checks and adjustments. 
 * @author revusky
 */

public class PostParseVisitor extends ASTVisitor {
	
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
	
	public void visit(TemplateHeaderElement header) {
		if (header == null) return;
		for (Map.Entry<String, Expression> entry : header.getParams().entrySet()) {
			String key = entry.getKey();
			try {
				if (key.equals("strip_whitespace")) {
					template.setStripWhitespace(header.getBooleanParameter("strip_whitespace"));
				} 
				else if (key.equals("attributes")) {
					TemplateHashModelEx attributeMap = (TemplateHashModelEx) header.getParameter("attributes");
	                Iterable keys = attributeMap.keys();
	                for (Iterator<Object> it = keys.iterator(); it.hasNext();) {
	                    String attName = asString(it.next());
	                    Object attValue = DeepUnwrap.unwrap(attributeMap.get(attName));
	                    template.setCustomAttribute(attName, attValue);
	                }
				}
				else if (key.equals("strict_vars")) {
					boolean strictVariableDeclaration = header.getBooleanParameter("strict_vars");
	         	   	template.setStrictVariableDeclaration(strictVariableDeclaration);
	       	   	}
				else if (!key.equals("strip_text") && !key.equals("encoding")) {
					ParsingProblem problem  = new ParsingProblem("Unknown ftl header parameter: " + entry.getKey(), header);
					template.addParsingProblem(problem);
				}
			} catch (Exception e) {
				ParsingProblem problem = new ParsingProblem(e.getMessage(), header);
				template.addParsingProblem(problem);
			}
		}
	}
	
	public void visit(Include node) {
		if (template.strictVariableDeclaration() && !node.useFreshNamespace()) {
			ParsingProblem problem = new ParsingProblem("The legacy #include instruction is not permitted in strict_vars mode. Use #embed or possibly #import.", node);
			template.addParsingProblem(problem);
		}
		super.visit(node);
	}
	
	public void visit(AssignmentInstruction node) {
		super.visit(node);
		if (template.strictVariableDeclaration()) {
			if (node.getBlockType() == AssignmentInstruction.NAMESPACE) {
				ParsingProblem problem = new ParsingProblem("The assign directive is deprecated and cannot be used in strict_vars mode. See the var and set directives.", node);
				template.addParsingProblem(problem);
			}
			if (node.getBlockType() == AssignmentInstruction.LOCAL) {
				ParsingProblem problem = new ParsingProblem("The local directive is deprecated and cannot be used in strict_vars mode. See the var and set directives.", node);
				template.addParsingProblem(problem);
			}
		}
        if (node.getBlockType() == AssignmentInstruction.LOCAL) {
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
		if (template.strictVariableDeclaration()) {
			if (node.getBlockType() == AssignmentInstruction.NAMESPACE) {
				ParsingProblem problem = new ParsingProblem("The assign directive is deprecated and cannot be used in strict_vars mode. See the var and set directives.", node);
				template.addParsingProblem(problem);
			}
			if (node.getBlockType() == AssignmentInstruction.LOCAL) {
				ParsingProblem problem = new ParsingProblem("The local directive is deprecated and cannot be used in strict_vars mode. See the var and set directives.", node);
				template.addParsingProblem(problem);
			}
		}
		if (node.getBlockType() == AssignmentInstruction.LOCAL) {
			Macro macro = getContainingMacro(node);
			if (macro == null) {
				template.addParsingProblem(new ParsingProblem("The local directive can only be used inside a function or macro.", node));
			} else {
				if (!macro.declaresVariable(node.getVarName())) {
					macro.declareVariable(node.getVarName());
				}
			}
		}
	}
	
	public void visit(BuiltInExpression node) {
		super.visit(node);
		if (node.getBuiltIn() == null) {
			ParsingProblem problem = new ParsingProblem("Unknown builtin: " + node.getName(), node);
			template.addParsingProblem(problem);
		}
	}
	
	public void visit(Interpolation node) {
		super.visit(node);
		markAsProducingOutput(node);
		Expression escapedExpression = escapedExpression(node.getExpression());
		node.setEscapedExpression(escapedExpression);
	}
	
	public void visit(IfBlock node) {
        if (node.size() == 1) {
            ConditionalBlock cblock = (ConditionalBlock) node.get(0);
            cblock.setIsSimple(true);
           	cblock.setLocation(node.getTemplate(), node.getTokenSource(), cblock, node);
            node.getParent().replace(node, cblock);
            visit(cblock);
        } else {
            super.visit(node);
        }
	}
	
	public void visit(EscapeBlock node) {
		Expression escapedExpression = escapedExpression(node.getExpression());
		node.setEscapedExpression(escapedExpression);
		escapes.add(node);
		super.visit(node);
		escapes.remove(escapes.size() -1);
	}
	
	public void visit(Macro node) {
		String macroName = node.getName();
		if (template.strictVariableDeclaration() && template.declaresVariable(macroName)) {
			ParsingProblem problem = new ParsingProblem("You already have declared a variable (or declared another macro) as " + macroName + ". You cannot reuse the variable name in the same template.", node);
			template.addParsingProblem(problem);
		}
		if (template.strictVariableDeclaration()) {
			template.declareVariable(macroName);
			Node parent=node.getParent();
			while (parent != null) {
				parent = parent.getParent();
				if (parent != null && !(parent instanceof EscapeBlock) && !(parent instanceof NoEscapeBlock) && !(parent instanceof MixedContent)) {
					ParsingProblem problem = new ParsingProblem("Macro " + macroName + " is within a " + ((TemplateNode)parent).getDescription() + ". It must be a top-level element.");
					template.addParsingProblem(problem);
				}
			}
		}
		template.addMacro(node);
		super.visit(node);
	}
	
	public void visit(NoEscapeBlock node) {
		Node parent = node;
		while (parent != null && !(parent instanceof EscapeBlock)) {
			parent = parent.getParent();
		}
		if (parent == null) {
			template.addParsingProblem(new ParsingProblem("The noescape directive only makes sense inside an escape block.", node));
		}
		EscapeBlock last = escapes.remove(escapes.size() -1);
		super.visit(node);
		escapes.add(last);
	}
	
	public void visit(IteratorBlock node) {
		node.declareVariable(node.getIndexName());
		node.declareVariable(node.getIndexName() + "_has_next");
		node.declareVariable(node.getIndexName() + "_index");
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
		Node parent = node;
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
		Node parent = node;
		while (parent != null && !(parent instanceof Macro)) {
			parent = parent.getParent();
		}
		if (parent == null) {
       		template.addParsingProblem(new ParsingProblem("The return directive can only be used inside a function or macro.", node));
		} else {
			Macro macro = (Macro) parent;
			if (!macro.isFunction() && node.size() > 1) {
				template.addParsingProblem(new ParsingProblem("Can only return a value from a function, not a macro", node));
			}
			else if (macro.isFunction() && node.size() ==1) {
				template.addParsingProblem(new ParsingProblem("A function must return a value.", node));
			}
		}
	}
	
	public void visit(VarDirective node) {
        Node parent = node.getParent();
        while (parent instanceof MixedContent 
        		|| parent instanceof EscapeBlock 
        		|| parent instanceof NoEscapeBlock) {
            parent = parent.getParent();
        }
       	for (String key : node.getVariables().keySet()) {
       		if (parent == null) {
       			template.declareVariable(key);
       		} else {
       			if (((TemplateElement)parent).declaresVariable(key)) {
       				String msg = "The variable " + key + " has already been declared in this block.";
       				if (parent instanceof Macro) {
       					String macroName = ((Macro) parent).getName();
       					msg = "The variable " + key + " has already been declared in macro " + macroName + ".";
       				}
       				template.addParsingProblem(new ParsingProblem(msg, node));
       			}
       			((TemplateElement)parent).declareVariable(key);
       		}
       	}
	}
	
	public void visit(SwitchBlock node) {
		super.visit(node);
		boolean foundDefaultCase = false;
		for (TemplateNode te : node.getCases()) {
			if (((Case) te).isDefault()) {
				if (foundDefaultCase) {
					template.addParsingProblem(new ParsingProblem("You can only have one default case in a switch construct.", node));
				}
				foundDefaultCase = true;
			}
		}
	}
	
	public void visit(TextBlock node) {
		int type = node.getBlockType();
		if (type == TextBlock.PRINTABLE_TEXT) {
			for (int i = node.getBeginLine(); i<=node.getEndLine(); i++) {
				boolean inMacro = getContainingMacro(node) != null;
				if (i >0) {//REVISIT THIS 
					template.markAsOutputtingLine(i, inMacro);
				}
			}
		} 
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
	
	public void visit(ImportDeclaration node) {
		String namespaceName = node.getNamespace();
		if (template.strictVariableDeclaration() && 
				template.declaresVariable(namespaceName)) { 
			String msg = "The variable "+namespaceName + " is already declared and should not be used as a namespace name to import.";
			template.addParsingProblem(new ParsingProblem(msg, node));
		}
		template.declareVariable(namespaceName);
		super.visit(node);
	}

	public void visit(TrimInstruction node) {
		for (int i = node.getBeginLine(); i<= node.getEndLine(); i++) {
			if (node.isLeft())
				template.setLineSaysLeftTrim(i);
			if (node.isRight())
				template.setLineSaysRightTrim(i);
			if (!(node.isLeft() || node.isRight())) 
				template.setLineSaysNoTrim(i);
		}
	}
	
    public void visit(PropertySetting node) {
    	String key = node.getKey();
        if (!key.equals(Configurable.LOCALE_KEY) &&
                !key.equals(Configurable.NUMBER_FORMAT_KEY) &&
                !key.equals(Configurable.TIME_FORMAT_KEY) &&
                !key.equals(Configurable.DATE_FORMAT_KEY) &&
                !key.equals(Configurable.DATETIME_FORMAT_KEY) &&
                !key.equals(Configurable.TIME_ZONE_KEY) &&
                !key.equals(Configurable.BOOLEAN_FORMAT_KEY) &&
                !key.equals(Configurable.URL_ESCAPING_CHARSET_KEY)) 
            {
        		ParsingProblem problem = new ParsingProblem("Invalid setting name, or it is not allowed to change the "
                        + "value of the setting with FTL: "
                        + key, node);
        		template.addParsingProblem(problem);
            }
    }
	
	static Macro getContainingMacro(TemplateNode node) {
		Node parent = node;
		while (parent != null && !(parent instanceof Macro)) {
			parent = parent.getParent();
		}
		return (Macro) parent;
	}
	
	private void markAsProducingOutput(TemplateNode node) {
		for (int i= node.getBeginLine(); i<=node.getEndLine(); i++) {
			boolean inMacro = getContainingMacro(node) != null;
			template.markAsOutputtingLine(i, inMacro);
		}
	}
}
