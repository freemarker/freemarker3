package freemarker.template;

import freemarker.core.Configurable;
import freemarker.core.nodes.generated.*;
import freemarker.core.parser.Node;
import freemarker.core.parser.ParseException;
import freemarker.core.parser.ParsingProblem;
import freemarker.core.parser.Token;
import freemarker.core.nodes.AssignmentInstruction;

import java.util.*;

/**
 * A class that visits the AST after the parsing step proper,
 * and makes various checks and adjustments. 
 * @author revusky
 */

class PostParseVisitor extends Node.Visitor {
	
	private Template template;
	private List<EscapeBlock> escapes = new ArrayList<>();

	PostParseVisitor(Template template) {
		this.template = template;
	}
	
	private Expression escapedExpression(Expression exp) {
		if(escapes.isEmpty()) {
			return exp;
		}
		EscapeBlock lastEscape = escapes.get(escapes.size() -1);
		return lastEscape.doEscape(exp);
	}

	void visit(Template template) {
		TemplateHeaderElement header = template.getHeaderElement();
		if (header != null) visit(header);
		visit(template.getRootTreeNode());
	}
	
	void visit(TemplateHeaderElement header) {
		if (header == null) return;
		for (Map.Entry<String, Expression> entry : header.getParams().entrySet()) {
			String key = entry.getKey();
			try {
				if (key.equals("strict_vars")) {
					boolean strictVariableDeclaration = header.getBooleanParameter("strict_vars");
	         	   	template.setStrictVariableDeclaration(strictVariableDeclaration);
	       	   	}
				else if (key.equals("legacy_syntax")) {
					boolean strictVariableDeclaration = !header.getBooleanParameter("legacy_syntax");
					template.setStrictVariableDeclaration(strictVariableDeclaration);
				}
				else if (key.equals("nsPrefixes")) {
					Object obj = header.getParameter("nsPrefixes");
					if (obj instanceof Map) {
						Map map = (Map) obj;
						for (Object prefix : map.keySet()) {
							Object value = map.get(prefix);
							template.addPrefixNSMapping(key.toString(), value.toString());
						}
					}
				}
				else if (!key.equals("encoding")) {
					ParsingProblem problem  = new ParsingProblem("Unknown ftl header parameter: " + entry.getKey(), header);
					template.addParsingProblem(problem);
				}
			} catch (Exception e) {
				ParsingProblem problem = new ParsingProblem(e.getMessage(), header);
				template.addParsingProblem(problem);
			}
		}
	}
	
	void visit(AssignmentInstruction node) {
		recurse(node);
		for (Expression target : node.getTargetExpressions()) {
			if (!target.isAssignableTo()) {
				ParsingProblem problem = new ParsingProblem("Cannot assign to expression" + target + " ", target);
				template.addParsingProblem(problem);
			}
		}
		if (template.strictVariableDeclaration()) {
			if (node.get(0).getType() == Token.TokenType.ASSIGN) {
				ParsingProblem problem = new ParsingProblem("The assign directive is deprecated and cannot be used in strict_vars mode. See the var and set directives.", node);
				template.addParsingProblem(problem);
			}
			else if (node.get(0).getType() == Token.TokenType.LOCALASSIGN) {
				ParsingProblem problem = new ParsingProblem("The local directive is deprecated and cannot be used in strict_vars mode. See the var and set directives.", node);
				template.addParsingProblem(problem);
			}
		}
        else if (node.get(0).getType() == Token.TokenType.LOCALASSIGN) {
        	Macro macro = getContainingMacro(node);
        	if (macro == null) {
        		ParsingProblem problem = new ParsingProblem("The local directive can only be used inside a function or macro.", node);
        		template.addParsingProblem(problem);
        	}
        }
	}
	
	void visit(BlockAssignment node) {
		recurse(node);
		Expression targetExpression = node.getTargetExpression();
		if (!targetExpression.isAssignableTo()) {
			ParsingProblem problem = new ParsingProblem("The expression " + targetExpression + " cannot be assigned to.", targetExpression);
			template.addParsingProblem(problem);
		}
		if (template.strictVariableDeclaration()) {
			if (node.get(0).getType() == Token.TokenType.ASSIGN) {
				ParsingProblem problem = new ParsingProblem("The assign directive is deprecated and cannot be used in strict_vars mode. See the var and set directives.", node);
				template.addParsingProblem(problem);
			}
			if (node.get(0).getType() == Token.TokenType.LOCALASSIGN) {
				ParsingProblem problem = new ParsingProblem("The local directive is deprecated and cannot be used in strict_vars mode. See the var and set directives.", node);
				template.addParsingProblem(problem);
			}
		}
		else if (node.get(0).getType() == Token.TokenType.LOCALASSIGN) {
			Macro macro = getContainingMacro(node);
			if (macro == null) {
				template.addParsingProblem(new ParsingProblem("The local directive can only be used inside a function or macro.", node));
			} 
		}
	}
	
	void visit(BuiltInExpression node) {
		recurse(node);
		if (node.getBuiltIn() == null) {
			ParsingProblem problem = new ParsingProblem("Unknown builtin: " + node.getName(), node);
			template.addParsingProblem(problem);
		}
	}
	
	void visit(Interpolation node) {
		recurse(node);
		Expression escapedExpression = escapedExpression(node.getExpression());
		node.setEscapedExpression(escapedExpression);
	}
	
	void visit(EscapeBlock node) {
		Expression escapedExpression = escapedExpression(node.getExpression());
		node.setEscapedExpression(escapedExpression);
		escapes.add(node);
		recurse(node);
		escapes.remove(escapes.size() -1);
	}
	
	void visit(Macro node) {
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
				if (parent != null && !(parent instanceof EscapeBlock) && !(parent instanceof NoEscapeBlock) && !(parent instanceof Block)) {
					ParsingProblem problem = new ParsingProblem("Macro " + macroName + " is within a " + ((TemplateNode)parent).getDescription() + ". It must be a top-level element.", node);
					template.addParsingProblem(problem);
				}
			}
		}
		template.addMacro(node);
		recurse(node);
	}
	
	void visit(NoEscapeBlock node) {
		Node parent = node;
		while (parent != null && !(parent instanceof EscapeBlock)) {
			parent = parent.getParent();
		}
		if (parent == null) {
			template.addParsingProblem(new ParsingProblem("The noescape directive only makes sense inside an escape block.", node));
		}
		EscapeBlock last = escapes.remove(escapes.size() -1);
		recurse(node);
		escapes.add(last);
	}
	
	void visit(IteratorBlock node) {
		node.getNestedBlock().declareVariable(node.getIndexName());
		node.getNestedBlock().declareVariable(node.getIndexName() + "_has_next");
		node.getNestedBlock().declareVariable(node.getIndexName() + "_index");
		if (node.getValueVarName() != null) {
			node.getNestedBlock().declareVariable(node.getValueVarName());
			node.getNestedBlock().declareVariable(node.getValueVarName() + "_has_next");
			node.getNestedBlock().declareVariable(node.getValueVarName() + "_index");
		}
		recurse(node);
	}
	
	void visit(BreakInstruction node) {
		recurse(node);
		Node parent = node;
		while (parent != null && !(parent instanceof SwitchBlock) && !(parent instanceof IteratorBlock)) { 
			parent = parent.getParent();
		}
		if (parent == null) {
			template.addParsingProblem(new ParsingProblem("The break directive can only be used within a loop or a switch-case construct.", node));
		}
	}
	
	void visit(ReturnInstruction node) {
		recurse(node);
		Node parent = node;
		while (parent != null && !(parent instanceof Macro)) {
			parent = parent.getParent();
		}
		if (parent == null) {
       		template.addParsingProblem(new ParsingProblem("The return directive can only be used inside a function or macro.", node));
		} else {
			Macro macro = (Macro) parent;
			if (!macro.isFunction() && node.size() > 2) {
				template.addParsingProblem(new ParsingProblem("Can only return a value from a function, not a macro", node));
			}
			else if (macro.isFunction() && node.size() ==2) {
				template.addParsingProblem(new ParsingProblem("A function must return a value.", node));
			}
		}
	}
	
	void visit(VarDirective node) {
        Block parent = (Block) node.getParent();
       	for (String key : node.getVariables().keySet()) {
       		if (parent == null) {
       			template.declareVariable(key);
       		} else {
       			if (parent.declaresVariable(key)) {
       				String msg = "The variable " + key + " has already been declared in this block.";
       				template.addParsingProblem(new ParsingProblem(msg, node));
       			}
       			parent.declareVariable(key);
       		}
       	}
	}
	
	void visit(StringLiteral node) {
		if (!node.isRaw()) {
			try {
				node.checkInterpolation();
			} catch (ParseException pe) {
				String msg = "Error in string " + node.getLocation();
				msg += "\n" + pe.getMessage();
				template.addParsingProblem(new ParsingProblem(msg, node));
			}
		}
	}
	
	void visit(ImportDeclaration node) {
		String namespaceName = node.getNamespace();
		if (template.strictVariableDeclaration() && 
				template.declaresVariable(namespaceName)) { 
			String msg = "The variable "+namespaceName + " is already declared and should not be used as a namespace name to import.";
			template.addParsingProblem(new ParsingProblem(msg, node));
		}
		template.declareVariable(namespaceName);
		recurse(node);
	}

    void visit(PropertySetting node) {
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
        		ParsingProblem problem = new ParsingProblem("Invalid setting name, or it is not allowed to change the"
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
}
