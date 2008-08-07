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

package freemarker.core.helpers;

import freemarker.core.ast.*;

import java.util.*;
import freemarker.template.*;
import freemarker.template.utility.StringUtil;

/**
 * Object that outputs FTL tree nodes in a canonical syntax.
 * The idea is that various pretty printers could override key methods
 * here to get a custom dump of an FTL file.
 * @author Jonathan Revusky
 */

public class DefaultTreeDumper extends ASTVisitor {
	
	String OPEN_BRACKET = "<";
	String CLOSE_BRACKET = ">";
	
	protected StringBuilder buffer = new StringBuilder();
	
	public String toString() {
		return buffer.toString();
	}
	
	public DefaultTreeDumper(boolean altSyntax) {
		if (altSyntax) {
			this.OPEN_BRACKET = "[";
			this.CLOSE_BRACKET = "]";
		}
	}
	
    public String render(Template template) {
        return render(template.getHeaderElement()) + render(template.getRootTreeNode());
    }
    
    public String render(TemplateNode node) {
    	StringBuilder prevBuf = this.buffer;
    	this.buffer = new StringBuilder();
    	visit(node);
    	String result = buffer.toString();
    	this.buffer = prevBuf;
    	return result;
    }
    
    protected void openDirective(String directiveName) {
    	buffer.append(OPEN_BRACKET);
    	buffer.append("#");
    	buffer.append(directiveName);
    }
    
    protected void closeDirective(String directiveName) {
    	buffer.append(OPEN_BRACKET);
    	buffer.append("/#");
    	buffer.append(directiveName);
    	buffer.append(CLOSE_BRACKET);
    }
    
    public void visit(TemplateHeaderElement header) {
    	if (header == null) return;
    	openDirective("ftl");
    	for (String paramName : header.getParams().keySet()) {
    		buffer.append(" ");
    		buffer.append(paramName);
    		buffer.append("=");
    		visit(header.getParams().get(paramName));
    	}
    	buffer.append(CLOSE_BRACKET);
    	buffer.append("\n");
    }
	
	public void visit(AddConcatExpression node) {
		visit(node.left);
		buffer.append("+");
		visit(node.right);
	}
	
	public void visit(AndExpression node) {
		visit(node.left);
		buffer.append("&&");
		visit(node.right);
	}
	
	public void visit(ArithmeticExpression node) {
		String opString = null;
		switch (node.operation) {
		   case ArithmeticExpression.DIVISION : opString = "/"; break;  
		   case ArithmeticExpression.MODULUS : opString = "%"; break;
		   case ArithmeticExpression.MULTIPLICATION : opString = "*"; break;
		   case ArithmeticExpression.SUBSTRACTION : opString = "-"; break;
		}
		visit(node.left);
		buffer.append(opString);
		visit(node.right);
	}
	
	public void visit(AssignmentInstruction node) {
		switch (node.type) {
			case AssignmentInstruction.GLOBAL : openDirective("global "); break;
			case AssignmentInstruction.LOCAL : openDirective("local "); break;
			case AssignmentInstruction.SET : openDirective("set "); break;
			case AssignmentInstruction.NAMESPACE : openDirective("assign "); break;
		}
		List varnames = node.getVarNames();
		List values = node.getValues();
		for (int i=0; i<varnames.size(); i++) {
			if (i>0) buffer.append(", ");
			String varname = (String) varnames.get(i);
			Expression value = (Expression) values.get(i);
			buffer.append(quoteVarnameIfNecessary(varname));
			buffer.append(" = ");
			visit(value);
		}
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(AttemptBlock node) {
		openDirective("attempt" + CLOSE_BRACKET);
		visit(node.getAttemptBlock());
		visit(node.getRecoverBlock());
	}
	
	public void visit(BlockAssignment node) {
		String varname = StringUtil.quoteStringIfNecessary(node.varName);
		Expression nsExp = node.namespaceExp;
		String instruction = null;
		switch(node.type) {
			case AssignmentInstruction.GLOBAL : instruction = "global"; break;
			case AssignmentInstruction.LOCAL : instruction = "local"; break;
			case AssignmentInstruction.NAMESPACE : instruction = "assign"; break;
			case AssignmentInstruction.SET : instruction = "set"; break;
		}
		openDirective(instruction);
		buffer.append(" ");
		buffer.append(varname);
		if (nsExp != null) {
			buffer.append(" in ");
			visit(nsExp);
		}
		buffer.append(CLOSE_BRACKET);
		visit(node.getNestedBlock());
		closeDirective(instruction);
	}
	
	public void visit(BodyInstruction node) {
		openDirective("nested");
		ArgsList args = node.getArgs();
		if (args != null) {
			buffer.append(" ");
			visit(args);
		}
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(BooleanLiteral node) {
		buffer.append(node.value ? "true" : "false"); 
	}
	
	public void visit(BreakInstruction node) {
		openDirective("break");
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(BuiltInExpression node) {
		visit(node.getTarget());
		buffer.append("?");
		buffer.append(node.getName());
	}
	
	public void visit(BuiltinVariable node) {
		buffer.append(".");
		buffer.append(node.name);
	}
	
	public void visit(Case node) {
		buffer.append(OPEN_BRACKET);
		if (node.isDefault) {
			buffer.append("#default");
		} else {
			buffer.append("#case ");
			visit(node.expression);
		}
		buffer.append(CLOSE_BRACKET);
		visit(node.getNestedBlock());
	}
	
	public void visit(Comment node) {
		buffer.append(OPEN_BRACKET);
		buffer.append("#--");
		buffer.append(node.text);
		buffer.append("--");
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(ComparisonExpression node) {
		visit(node.left);
		boolean usingAltSyntax = CLOSE_BRACKET.equals("]");
		switch(node.operation) {
			case ComparisonExpression.EQUALS : buffer.append(" = "); break;
			case ComparisonExpression.NOT_EQUALS : buffer.append(" != "); break;
			case ComparisonExpression.GREATER_THAN :
				if (usingAltSyntax) {
					buffer.append(">");
				} else {
					buffer.append("gt");
				}
				break;
			case ComparisonExpression.GREATER_THAN_EQUALS :
				if (usingAltSyntax) {
					buffer.append(">=");
				}
				else {
					buffer.append(" gte ");
				}
				break;
			case ComparisonExpression.LESS_THAN :
				buffer.append("<");
				break;
			case ComparisonExpression.LESS_THAN_EQUALS :
				buffer.append("<=");
				break;
		}
		visit(node.right);
	}
	
	
	public void visit(CompressedBlock node) {
		openDirective("compress");
		buffer.append(CLOSE_BRACKET);
		visit(node.getNestedBlock());
		closeDirective("compress");
	}
	
	public void visit(ConditionalBlock node) {
		buffer.append(OPEN_BRACKET);
		if (node.isFirst) {
			buffer.append("#if ");
		}
		else if (node.condition == null) {
			buffer.append("#else");
		}
		else {
			buffer.append("#elseif ");
		}
		visit(node.condition);
		buffer.append(CLOSE_BRACKET);
		visit(node.getNestedBlock());
		if (node.isLoneIfBlock()) {
			closeDirective("if");
		}
	}
	
	public void visit(DefaultToExpression node) {
		visit(node.lhs);
		buffer.append("!");
		visit(node.rhs);
	}
	
	public void visit(Interpolation node) {
		buffer.append("${");
		visit(node.expression);
		buffer.append("}");
	}
	
	public void visit(Dot node) {
		visit(node.target);
		buffer.append(".");
		buffer.append(node.key);
	}
	
	public void visit(DynamicKeyName node) {
		visit(node.target);
		buffer.append("[");
		visit(node.nameExpression);
		buffer.append("]");
	}
	
	public void visit(EscapeBlock node) {
		openDirective("escape ");
		buffer.append(node.variable);
		buffer.append(" as ");
		visit(node.expr);
		buffer.append(CLOSE_BRACKET);
		visit(node.getNestedBlock());
		closeDirective("escape");
	}
	
	public void visit(ExistsExpression node) {
		visit(node.exp);
		buffer.append("??");
	}
	
	public void visit(FallbackInstruction node) {
		openDirective("fallback");
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(FlushInstruction node) {
		openDirective("flush");
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(HashLiteral node) {
		buffer.append("{");
		List<Expression> keys = node.getKeys();
		List<Expression> values = node.getValues();
		for (int i=0; i<keys.size(); i++) {
            if (i >0) buffer.append(", ");
            visit(keys.get(i));
			buffer.append(" : ");
			visit(values.get(i));
		}
		buffer.append("}");
	}

	public void visit(Identifier node) {
		buffer.append(node.name);
	}
	
	public void visit(IfBlock node) {
		for (TemplateNode block : node.getSubBlocks()) {
			visit(block);
			
		}
		closeDirective("if");
	}
	
	public void visit(Include node) {
		if (node.freshNamespace) openDirective("embed ");
		else openDirective("include ");
		visit(node.getIncludedTemplateExpression());
		Expression parseExp = node.getParseExp();
		Expression encodingExp = node.getEncodingExp();
		if (parseExp != null || encodingExp != null) {
			buffer.append (" ;");
			if (encodingExp != null) {
				buffer.append(" ");
				buffer.append("encoding=");
				visit(encodingExp);
			}
			if (parseExp != null) {
				buffer.append(" ");
				buffer.append("parse=");
				visit(parseExp);
			}
		}
		buffer.append(CLOSE_BRACKET);
	}

	public void visit(IteratorBlock node) {
		openDirective("list ");
		visit(node.listExpression);
		buffer.append(" as ");
		buffer.append(node.indexName);
		buffer.append(CLOSE_BRACKET);
		visit(node.getNestedBlock());
		closeDirective("list");
	}
	
	public void visit(LibraryLoad node) {
		openDirective("import ");
		visit(node.templateName);
		buffer.append(" as ");
		buffer.append(node.namespace);
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(ListLiteral node) {
		buffer.append("[");
		boolean firstElement = true;
		for (Expression exp : node.getElements()) {
			if (!firstElement) {
				buffer.append(", ");
			}
			firstElement = false;
			visit(exp);
		}
		buffer.append("]");
	}
	
	public void visit(Macro node) {
		if (node.isFunction()) {
			openDirective("function ");
		} else {
			openDirective("macro ");
		}
		buffer.append(quoteVarnameIfNecessary(node.getName()));
		ParameterList params = node.getParams();
		if (params != null) {
			String paramsString = render(params).trim();
			if (paramsString.length()>0) {
				buffer.append(" ");
				buffer.append(paramsString);
			}
		}
		buffer.append(CLOSE_BRACKET);
		visit(node.getNestedBlock());
		if (node.isFunction()) {
			closeDirective("function");
		} else {
			closeDirective("macro");
		}
	}
	
	public void visit(MethodCall node) {
		visit(node.target);
		buffer.append("(");
		visit(node.getArgs());
		buffer.append(")");
	}
	
	public void visit(NamedArgsList args) {
		boolean atFirstArg = true;
		for (Map.Entry<String, Expression> entry : args.getArgs().entrySet()) {
			if (!atFirstArg && buffer.charAt(buffer.length() -1) == '!') {
				buffer.append(",");
			}
			if (!atFirstArg) buffer.append(" ");
			buffer.append(entry.getKey());
			buffer.append("=");
			visit(entry.getValue());
			atFirstArg = false;
		}
	}
	
	public void visit(NoEscapeBlock node) {
		openDirective("noescape");
		buffer.append(CLOSE_BRACKET);
		visit(node.getNestedBlock());
		closeDirective("noescape");
	}
	
	public void visit(NotExpression node) {
		buffer.append("!");
		visit(node.target);
	}
	
	public void visit(NullLiteral node) {
		buffer.append("null");
	}
	
	public void visit(NumberLiteral node) {
		buffer.append(node.value.toString());
	}
	
	public void visit(NumericalOutput node) {
		buffer.append("#{");
		visit(node.expression);
		String formatString = node.getFormatString();
		if (formatString != null) {
			buffer.append(" ; ");
			buffer.append(formatString);
		}
		buffer.append("}");
	}
	
	public void visit(OrExpression exp) {
		visit(exp.left);
		buffer.append(" || ");
		visit(exp.right);
	}
	
	public void visit(ParameterList plist) {
		boolean atFirstParam = true;
		for (String paramName : plist.getParamNames()) {
			if (!atFirstParam) {
				buffer.append(" ");
			}
			buffer.append(paramName);
			Expression defaultExp = plist.getDefaultExpression(paramName);
			if (defaultExp != null) {
				buffer.append("=");
				visit(defaultExp);
			}
			atFirstParam = false;
		}
		String catchall = plist.getCatchAll();
		if (catchall !=null) {
			if (!plist.getParamNames().isEmpty()) buffer.append(" ");
			buffer.append(catchall + "...");
		}
	}
	
	public void visit(ParentheticalExpression node) {
		buffer.append("(");
		visit(node.nested);
		buffer.append(")");
	}
	
	public void visit(PositionalArgsList plist) {
		boolean atFirstArg = true;
		for (Expression exp : plist.getArgs()) {
			if (!atFirstArg) buffer.append(", ");
			visit(exp);
			atFirstArg = false;
		}
	}
	
	public void visit(PropertySetting node) {
		openDirective("setting ");
		buffer.append(node.key);
		buffer.append(" = ");
		visit(node.value);
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(Range node) {
		visit(node.left);
		buffer.append("..");
		if (node.right != null) {
			String right = render(node.right);
			if (right.charAt(0) == '.') buffer.append(" ");
			buffer.append(right);
		}
	}
	
	public void visit(RecoveryBlock node) {
		openDirective("recover");
		buffer.append(CLOSE_BRACKET);
		visit(node.getNestedBlock());
		closeDirective("attempt");
	}
	
	public void visit(RecurseNode node) {
		openDirective("recurse ");
		visit(node.targetNode);
		if (node.namespaces != null) {
			buffer.append(" using ");
			visit(node.namespaces);
		}
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(ReturnInstruction node) {
		buffer.append(OPEN_BRACKET);
		buffer.append("#return");
		if (node.returnExp != null) {
			buffer.append(" ");
			buffer.append(render(node.returnExp));
		}
		buffer.append(CLOSE_BRACKET);
	}

	public void visit(VarDirective node) {
		openDirective("var ");
		Map vars = node.getVariables();
		for (Iterator it = vars.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String varname = (String) entry.getKey();
			varname = StringUtil.quoteStringIfNecessary(varname);
			Expression exp = (Expression) entry.getValue();
			buffer.append(varname);
			if (exp != null) {
				buffer.append("=");
				visit(exp);
			}
			if (it.hasNext()) buffer.append(" ");
		}
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(StopInstruction node) {
		openDirective("stop");
		if (node.message != null) {
			buffer.append(" ");
			visit(node.message);
		}
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(StringLiteral node) { //REVISIT
		String result;
		if (node.getValue().indexOf('"') == -1) {
			result = "\"" + node.getValue() + "\""; 
		} 
		else if (node.getValue().indexOf('\'') == -1) {
			result = "'" + node.getValue() + "'";
		}
		else {
			result = "\"" + node.getValue().replace("\"", "\\\"") + "\"";
		}
		if (node.isRaw()) {
			result = "r" + result;
		}
		buffer.append(result);
	}
	
	public void visit(SwitchBlock node) {
		openDirective("switch ");
		buffer.append(render(node.testExpression));
		buffer.append(CLOSE_BRACKET);
		List<TemplateElement> cases = node.getCases();
		if (cases != null) for (TemplateNode cas : node.getCases()) {
			visit(cas);
		}
		closeDirective("switch");
	}
	
	
	public void visit(TextBlock node) {
		buffer.append(node.getText());
	}
	
	
	public void visit(NoParseBlock node) {
		openDirective("noparse");
		buffer.append(CLOSE_BRACKET);
		super.visit(node);
		closeDirective("noparse");
	}
	
	public void visit(TransformBlock node) {
		openDirective("transform ");
		visit(node.transformExpression);
		Map args = node.getArgs();
		for (Iterator it = args.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String varname = (String) entry.getKey();
			varname = StringUtil.quoteStringIfNecessary(varname);
			Expression value = (Expression) entry.getValue();
			buffer.append(" ");
			buffer.append(varname);
			buffer.append("=");
			visit(value);
		}
		buffer.append(CLOSE_BRACKET);
		visit(node.getNestedBlock());
		closeDirective("transform");
	}
	
	public void visit(TrimInstruction node) {
		if (node.left && node.right) {
			openDirective("t");
		}
		else if (node.left) {
			openDirective("lt");
		}
		else if (node.right) {
			openDirective("rt");
		}
		else {
			openDirective("nt");
		}
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(TrimBlock node) {
		String tagName = "nt_lines";
		if (node.left && node.right) {
			tagName = "t_lines";
		} else if (node.left) {
			tagName = "lt_lines";
		} else if (node.right) {
			tagName = "rt_lines";
		}
		openDirective(tagName);
		visit(node.getNestedBlock());
		closeDirective(tagName);
	}
	
	
	public void visit(UnaryPlusMinusExpression node) {
		String op = node.isMinus ? "-" : "+";
		buffer.append(op);
		visit(node.target);
	}

	public void visit(UnifiedCall node) {
		buffer.append(OPEN_BRACKET);
		buffer.append("@");
		visit(node.getNameExp());
		ArgsList args = node.getArgs();
		if (args != null) {
			buffer.append(" ");
			visit(args);
		}
		ParameterList bodyParameters = node.getBodyParameters();
		if (bodyParameters != null) {
			buffer.append("; ");
			visit(bodyParameters);
		}
		TemplateElement body = node.getNestedBlock();
		if (body == null) {
			buffer.append("/");
			buffer.append(CLOSE_BRACKET);
		} else {
			buffer.append(CLOSE_BRACKET);
			visit(body);
			buffer.append(OPEN_BRACKET);
			buffer.append("/@");
			if (node.getNameExp() instanceof Identifier) {
				buffer.append(node.getNameExp());
			}
			buffer.append(CLOSE_BRACKET);
		}
	}
	
	public void visit(VisitNode node) {
		openDirective("visit ");
		visit(node.targetNode);
		if (node.namespaces != null) {
			buffer.append(" using ");
			visit(node.namespaces);
		}
		buffer.append(CLOSE_BRACKET);
	}
	
	static String quoteVarnameIfNecessary(String varname) {
		if (StringUtil.isFTLIdentifier(varname)) return varname;
		return "\"" + StringUtil.FTLStringLiteralEnc(varname) + "\"";
	}
    
}
	
