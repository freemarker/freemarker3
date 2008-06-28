/*
 * Copyright (c) 2007 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */

package freemarker.template;

import freemarker.core.Configurable;
import freemarker.core.ast.*;
import freemarker.core.parser.ParseException;
import freemarker.core.parser.ParsingProblem;
import freemarker.template.*;
import freemarker.template.utility.DeepUnwrap;


import java.io.StringReader;
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
				else if (key.equals("ns_prefixes")) {
					TemplateHashModelEx prefixMap = (TemplateHashModelEx) header.getParameter("ns_prefixes");
	                TemplateCollectionModel keys = prefixMap.keys();
	                for (TemplateModelIterator it = keys.iterator(); it.hasNext();) {
	                    String prefix = ((TemplateScalarModel) it.next()).getAsString();
	                    TemplateModel valueModel = prefixMap.get(prefix);
	                    String nsURI = ((TemplateScalarModel) valueModel).getAsString();
	                    template.addPrefixNSMapping(prefix, nsURI);
	                }
				}
				else if (key.equals("attributes")) {
					TemplateHashModelEx attributeMap = (TemplateHashModelEx) header.getParameter("attributes");
	                TemplateCollectionModel keys = attributeMap.keys();
	                for (TemplateModelIterator it = keys.iterator(); it.hasNext();) {
	                    String attName = ((TemplateScalarModel) it.next()).getAsString();
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
		if (template.strictVariableDeclaration()) {
			if (node.type == AssignmentInstruction.NAMESPACE) {
				ParsingProblem problem = new ParsingProblem("The assign directive is deprecated and cannot be used in strict_vars mode. See the var and set directives.", node);
				template.addParsingProblem(problem);
			}
			if (node.type == AssignmentInstruction.LOCAL) {
				ParsingProblem problem = new ParsingProblem("The local directive is deprecated and cannot be used in strict_vars mode. See the var and set directives.", node);
				template.addParsingProblem(problem);
			}
		}
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
		if (template.strictVariableDeclaration()) {
			if (node.type == AssignmentInstruction.NAMESPACE) {
				ParsingProblem problem = new ParsingProblem("The assign directive is deprecated and cannot be used in strict_vars mode. See the var and set directives.", node);
				template.addParsingProblem(problem);
			}
			if (node.type == AssignmentInstruction.LOCAL) {
				ParsingProblem problem = new ParsingProblem("The local directive is deprecated and cannot be used in strict_vars mode. See the var and set directives.", node);
				template.addParsingProblem(problem);
			}
		}
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
	
	public void visit(Interpolation node) {
		super.visit(node);
		markAsProducingOutput(node);
		Expression escapedExpression = escapedExpression(node.expression);
		node.setEscapedExpression(escapedExpression);
		checkLiteralInScalarContext(escapedExpression);
	}
	
	public void visit(IfBlock node) {
        if (node.getChildCount() == 1) {
            ConditionalBlock cblock = (ConditionalBlock) node.getChildAt(0);
            cblock.setIsSimple(true);
           	cblock.setLocation(node.getTemplate(), cblock, node);
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
	
	public void visit(Macro node) {
		String macroName = node.getName();
		if (template.strictVariableDeclaration() && template.declaresVariable(macroName)) {
			ParsingProblem problem = new ParsingProblem("You already have declared a variable (or declared another macro) as " + macroName + ". You cannot reuse the variable name in the same template.", node);
			template.addParsingProblem(problem);
		}
		if (template.strictVariableDeclaration()) {
			template.declareVariable(macroName);
			TemplateElement parent=node.getParent();
			while (parent != null) {
				parent = parent.getParent();
				if (parent != null && !(parent instanceof EscapeBlock) && !(parent instanceof NoEscapeBlock) && !(parent instanceof MixedContent)) {
					ParsingProblem problem = new ParsingProblem("Macro " + macroName + " is within a " + parent.getDescription() + ". It must be a top-level element.");
					template.addParsingProblem(problem);
				}
			}
		}
		template.addMacro(node);
		super.visit(node);
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
        while (parent instanceof MixedContent 
        		|| parent instanceof EscapeBlock 
        		|| parent instanceof NoEscapeBlock) {
            parent = parent.getParent();
        }
       	for (String key : node.getVariables().keySet()) {
       		if (parent == null) {
       			template.declareVariable(key);
       		} else {
       			if (parent.declaresVariable(key)) {
       				String msg = "The variable " + key + " has already been declared in this block.";
       				if (parent instanceof Macro) {
       					String macroName = ((Macro) parent).getName();
       					msg = "The variable " + key + " has already been declared in macro " + macroName + ".";
       				}
       				template.addParsingProblem(new ParsingProblem(msg, node));
       			}
       			parent.declareVariable(key);
       		}
       	}
	}
	
	public void visit(SwitchBlock node) {
		super.visit(node);
		boolean foundDefaultCase = false;
		for (TemplateNode te : node.getCases()) {
			if (((Case) te).isDefault) {
				if (foundDefaultCase) {
					template.addParsingProblem(new ParsingProblem("You can only have one default case in a switch construct.", node));
				}
				foundDefaultCase = true;
			}
		}
	}
	
	public void visit(TextBlock node) {
		int type = node.getType();
		if (type == TextBlock.PRINTABLE_TEXT) {
			for (int i = node.getBeginLine(); i<=node.getEndLine(); i++) {
				boolean inMacro = getContainingMacro(node) != null;
				if (i >0) {//REVISIT THIS 
					template.markAsOutputtingLine(i, inMacro);
				}
			}
		} else if (type == TextBlock.WHITE_SPACE) {
			
			
		}
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
		try {
			node.parseFormat();
		} catch (Exception e) {
			String msg = e.getMessage();
			ParsingProblem problem = new ParsingProblem(msg, node);
			template.addParsingProblem(problem);
		}
		markAsProducingOutput(node);
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
	
	public void visit(LibraryLoad node) {
		String namespaceName = node.namespace;
		if (template.strictVariableDeclaration() && 
				template.declaresVariable(namespaceName)) { 
			String msg = "The variable "+namespaceName + " is already declared and should not be used as a namespace name to import.";
			template.addParsingProblem(new ParsingProblem(msg, node));
		}
		template.declareVariable(namespaceName);
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
	
	public void visit(TrimInstruction node) {
		for (int i = node.getBeginLine(); i<= node.getEndLine(); i++) {
			if (node.left)
				template.setLineSaysLeftTrim(i);
			if (node.right)
				template.setLineSaysRightTrim(i);
			if (!(node.left || node.right)) 
				template.setLineSaysNoTrim(i);
		}
	}
	
	public void visit(TrimBlock node) {
		int beginLine = node.getBeginLine();
		int endLine = node.getEndLine();
		template.setLineSaysRightTrim(beginLine++);
		template.setLineSaysLeftTrim(endLine--);
		for (int i= beginLine; i<=endLine; i++) {
			template.lineSaysLeftTrim(i);
			template.setLineSaysRightTrim(i);
		}
		super.visit(node);
	}
	
    public void visit(PropertySetting node) {
    	String key = node.key;
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
	
	static Macro getContainingMacro(TemplateNode node) {
		TemplateNode parent = node;
		while (parent != null && !(parent instanceof Macro)) {
			parent = parent.getParentNode();
		}
		return (Macro) parent;
	}
	
	private void markAsProducingOutput(TemplateNode node) {
		for (int i= node.getBeginLine(); i<=node.getEndLine(); i++) {
			boolean inMacro = getContainingMacro(node) != null;
			template.markAsOutputtingLine(i, inMacro);
		}
	}
	
    public String firstLine(TemplateNode node) {
    	String line = template.getLine(node.getBeginLine());
    	if (node.getBeginLine() == node.getEndLine()) {
    		line = line.substring(0, node.getEndColumn());
    	}
    	return line.substring(node.getBeginColumn() -1);
    }
    
    public String lastLine(TemplateNode node) {
    	String line = template.getLine(node.getEndLine());
    	line = line.substring(0, node.getEndColumn());
    	if (node.getBeginLine() == node.getEndLine()) {
    		line = line.substring(node.getBeginColumn() -1);
    	}
    	return line;
    }
    
}
