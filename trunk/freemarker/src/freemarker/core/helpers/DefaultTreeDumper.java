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

import java.lang.reflect.*;
import java.util.*;
import freemarker.template.*;
import freemarker.template.utility.StringUtil;

/**
 * Object that outputs FTL tree nodes in a canonical syntax.
 * The idea is that various pretty printers could override key methods
 * here to get a custom dump of an FTL file.
 * @author Jonathan Revusky
 */

public class DefaultTreeDumper {
	
	String OPEN_BRACKET = "<";
	String CLOSE_BRACKET = ">";
	
	public DefaultTreeDumper(boolean altSyntax) {
		if (altSyntax) {
			this.OPEN_BRACKET = "[";
			this.CLOSE_BRACKET = "]";
		}
	}
	
    public String render(Template template) {
        return reconstructHeader(template) + render(template.getRootTreeNode());
    }
    
    public String reconstructHeader(Template template) {
    	return ""; //TODO
    }
	
	public String render(TemplateNode node) {
		String result = null;
    	try {
    		Class clazz = node.getClass();
        	Method visitMethod = this.getClass().getMethod("render", new Class[] {clazz});
    		result = (String) visitMethod.invoke(this, new Object[] {node});
    	}
    	catch (InvocationTargetException ite) {
    		Throwable cause = ite.getCause();
    		if (cause instanceof RuntimeException) {
    			throw (RuntimeException) cause;
    		}
    	}
    	catch (Exception e) {
    		throw new IllegalArgumentException("There is no routine to render node class " + node.getClass());
    	}
    	return result;
	}
	
	public String render(AddConcatExpression node) {
		return render(node.left) + "+" + render(node.right);
	}
	
	public String render(AndExpression exp) {
		return render(exp.left) + " && " + render(exp.right);
	}
	
	public String render(ArithmeticExpression exp) {
		String opString = null;
		switch (exp.operation) {
		   case ArithmeticExpression.DIVISION : opString = "/"; break;  
		   case ArithmeticExpression.MODULUS : opString = "%"; break;
		   case ArithmeticExpression.MULTIPLICATION : opString = "*"; break;
		   case ArithmeticExpression.SUBSTRACTION : opString = "-"; break;
		}
		return render(exp.left) + opString + render(exp.right);
	}
	
	public String render(AssignmentInstruction node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#");
		switch (node.type) {
			case AssignmentInstruction.GLOBAL : buf.append("global "); break;
			case AssignmentInstruction.LOCAL : buf.append("local "); break;
			case AssignmentInstruction.SET : buf.append("set "); break;
			case AssignmentInstruction.NAMESPACE : buf.append("assign "); break;
		}
		List varnames = node.getVarNames();
		List values = node.getValues();
		for (int i=0; i<varnames.size(); i++) {
			if (i>0) buf.append(", ");
			String varname = (String) varnames.get(i);
			Expression value = (Expression) values.get(i);
			buf.append(quoteVarnameIfNecessary(varname));
			buf.append(" = ");
			buf.append(render(value));
		}
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(AttemptBlock node) {
		return OPEN_BRACKET 
                + "#attempt" 
                + CLOSE_BRACKET 
                + render(node.getAttemptBlock()) + render(node.getRecoverBlock());
	}
	
	public String render(BlockAssignment node) {
		String varname = StringUtil.quoteStringIfNecessary(node.varName);
		Expression nsExp = node.namespaceExp;
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		String instruction = null;
		switch(node.type) {
			case AssignmentInstruction.GLOBAL : instruction = "#global"; break;
			case AssignmentInstruction.LOCAL : instruction = "#local"; break;
			case AssignmentInstruction.NAMESPACE : instruction = "#assign"; break;
			case AssignmentInstruction.SET : instruction = "#set"; break;
		}
		buf.append(instruction);
		buf.append(" ");
		buf.append(varname);
		if (nsExp != null) {
			buf.append(" in ");
			buf.append(render(nsExp));
		}
		buf.append(CLOSE_BRACKET);
		buf.append(render(node.getNestedBlock()));
		buf.append(OPEN_BRACKET);
		buf.append("/");
		buf.append(instruction);
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(BodyInstruction node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#nested");
		ArgsList args = node.getArgs();
		if (args != null) {
			buf.append(" ");
			buf.append(render(args));
		}
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(BooleanLiteral node) {
		return node.value ? "true" : "false"; 
	}
	
	public String render(BreakInstruction node) {
		return OPEN_BRACKET + "#break" + CLOSE_BRACKET; 
	}
	
	public String render(BuiltInExpression node) {
		return render(node.getTarget()) + "?" + node.getName();
	}
	
	public String render(BuiltinVariable node) {
		return "." + node.name;
	}
	
	public String render(Case node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		if (node.isDefault) {
			buf.append("#default");
		} else {
			buf.append("#case ");
			buf.append(render(node.expression));
		}
		buf.append(CLOSE_BRACKET);
		buf.append(render(node.getNestedBlock()));
		return buf.toString();
	}
	
	public String render(Comment node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#--");
		buf.append(node.text);
		buf.append("--");
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(ComparisonExpression node) {
		StringBuilder buf = new StringBuilder();
		buf.append(render(node.left));
		boolean usingAltSyntax = CLOSE_BRACKET.equals("]");
		switch(node.operation) {
			case ComparisonExpression.EQUALS : buf.append(" = "); break;
			case ComparisonExpression.NOT_EQUALS : buf.append(" != "); break;
			case ComparisonExpression.GREATER_THAN :
				if (usingAltSyntax) {
					buf.append(">");
				} else {
					buf.append("gt");
				}
				break;
			case ComparisonExpression.GREATER_THAN_EQUALS :
				if (usingAltSyntax) {
					buf.append(">=");
				}
				else {
					buf.append(" gte ");
				}
				break;
			case ComparisonExpression.LESS_THAN :
				buf.append("<");
				break;
			case ComparisonExpression.LESS_THAN_EQUALS :
				buf.append("<=");
				break;
		}
		buf.append(render(node.right));
		return buf.toString();
	}
	
	
	public String render(CompressedBlock node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#compress");
		buf.append(CLOSE_BRACKET);
		buf.append(render(node.getNestedBlock()));
		buf.append(OPEN_BRACKET);
		buf.append("/#compress");
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(ConditionalBlock node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		if (node.isFirst) {
			buf.append("#if ");
		}
		else if (node.condition == null) {
			buf.append("#else");
		}
		else {
			buf.append("#elseif ");
		}
		if (node.condition != null) {
			buf.append(render(node.condition));
		}
		buf.append(CLOSE_BRACKET);
		TemplateElement nestedBlock = node.getNestedBlock();
		if (nestedBlock != null) {
			buf.append(render(nestedBlock));
		}
		if (node.isLoneIfBlock()) {
			buf.append(OPEN_BRACKET);
			buf.append("/#if");
			buf.append(CLOSE_BRACKET);
		}
		return buf.toString();
	}
	
	public String render(DefaultToExpression node) {
		String rhs = node.rhs == null ? "" : (String) render(node.rhs);
		return render(node.lhs) + "!" + rhs;
	}
	
	public String render(DollarVariable node) {
		return "${" + render(node.expression) + "}";
	}
	
	public String render(Dot node) {
		return render(node.target) + "." + node.key;
	}
	
	public String render(DynamicKeyName node) {
		return render(node.target) + "[" + render(node.nameExpression) + "]";
	}
	
	public String render(EscapeBlock node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#escape ");
		buf.append(node.variable);
		buf.append(" as ");
		buf.append(render(node.expr));
		buf.append(CLOSE_BRACKET);
		buf.append(render(node.getNestedBlock()));
		buf.append(OPEN_BRACKET);
		buf.append("/#escape");
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(ExistsExpression node) {
		return render(node.exp) + "??";
	}
	
	public String render(FallbackInstruction node) {
		return OPEN_BRACKET + "#fallback" + CLOSE_BRACKET;
	}
	
	public String render(FlushInstruction node) {
		return OPEN_BRACKET + "#flush" + CLOSE_BRACKET;
	}
	
	public String render(HashLiteral node) {
		StringBuilder buf = new StringBuilder("{");
		List keys = node.getKeys();
		List values = node.getValues();
		for (int i=0; i<keys.size(); i++) {
            if (i >0) buf.append(", ");
			buf.append(render((Expression) keys.get(i)));
			buf.append(" : ");
			buf.append(render((Expression) values.get(i)));
		}
		buf.append("}");
		return buf.toString();
	}

	public String render(Identifier node) {
		return node.name;
	}
	
	public String render(IfBlock node) {
		List subBlocks = node.getSubBlocks();
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<subBlocks.size(); i++) {
			TemplateElement block = (TemplateElement) subBlocks.get(i);
			buf.append(render(block));
		}
		buf.append(OPEN_BRACKET);
		buf.append("/#if");
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(Include node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#include ");
		buf.append(render(node.getIncludedTemplateExpression()));
		Expression parseExp = node.getParseExp();
		Expression encodingExp = node.getEncodingExp();
		if (parseExp != null || encodingExp != null) {
			buf.append (" ;");
			if (encodingExp != null) {
				buf.append(" ");
				buf.append("encoding=");
				buf.append(render(encodingExp));
			}
			if (parseExp != null) {
				buf.append(" ");
				buf.append("parse=");
				buf.append(render(parseExp));
			}
		}
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}

	public String render(IteratorBlock node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#list ");
		buf.append(render(node.listExpression));
		buf.append(" as ");
		buf.append(node.indexName);
		buf.append(CLOSE_BRACKET);
		buf.append(render(node.getNestedBlock()));
		buf.append(OPEN_BRACKET);
		buf.append("/#list");
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(LibraryLoad node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#import ");
		buf.append(render(node.templateName));
		buf.append(" as ");
		buf.append(node.namespace);
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(ListLiteral node) {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		List elements = node.getElements();
		for (int i=0; i<elements.size(); i++) {
			if (i>0) {
				buf.append(", ");
			}
			buf.append(render((TemplateNode) elements.get(i)));
		}
		buf.append("]");
		return buf.toString();
	}
	
	public String render(Macro node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		if (node.isFunction()) {
			buf.append("#function ");
		} else {
			buf.append("#macro ");
		}
		buf.append(quoteVarnameIfNecessary(node.getName()));
		ParameterList params = node.getParams();
		if (params != null) {
			String paramsString = render(params).trim();
			if (paramsString.length()>0) {
				buf.append(" ");
				buf.append(paramsString);
			}
		}
		buf.append(CLOSE_BRACKET);
		TemplateElement nestedBlock = node.getNestedBlock();
		if (nestedBlock != null) {
			buf.append(render(nestedBlock));
		}
		buf.append(OPEN_BRACKET);
		if (node.isFunction()) {
			buf.append("/#function");
		} else {
			buf.append("/#macro");
		}
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(MethodCall node) {
		StringBuilder buf = new StringBuilder();
		buf.append(render(node.target));
		buf.append("(");
		ArgsList args = node.getArgs();
		if (args != null) {
			buf.append(render(args));
		}
		buf.append(")");
		return buf.toString();
	}
	
	public String render(MixedContent node) {
		StringBuilder buf = new StringBuilder();
		List l = node.getNestedElements();
		for (int i=0; i<l.size(); i++) {
			buf.append(render((TemplateNode) l.get(i)));
		}
		return buf.toString();
	}
	
	public String render(NamedArgsList args) {
		StringBuilder buf = new StringBuilder();
		for (Map.Entry<String, Expression> entry : args.getArgs().entrySet()) {
			if (buf.length() >0 && buf.charAt(buf.length() -1) == '!') {
				buf.append(",");
			}
			if (buf.length() >0) buf.append(" ");
			buf.append(entry.getKey());
			buf.append("=");
			Expression exp = entry.getValue();
			buf.append(render(exp));
		}
		return buf.toString();
	}
	
	public String render(NoEscapeBlock node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#noescape");
		buf.append(CLOSE_BRACKET);
		buf.append(render(node.getNestedBlock()));
		buf.append(OPEN_BRACKET);
		buf.append("/#noescape");
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(NotExpression node) {
		return "!" + render(node.target);
	}
	
	public String render(NullLiteral node) {
		return "null";
	}
	
	public String render(NumberLiteral node) {
		return node.value.toString();
	}
	
	public String render(NumericalOutput node) {
		StringBuilder buf = new StringBuilder();
		buf.append("#{");
		buf.append(render(node.expression));
		if (node.hasFormat) {
			buf.append(" ; ");
			buf.append("m");
			buf.append(node.minFracDigits);
			buf.append("M");
			buf.append(node.maxFracDigits);
		}
		buf.append("}");
		return buf.toString();
	}
	
	public String render(OrExpression exp) {
		return render(exp.left) + " || " + render(exp.right);
	}
	
	public String render(ParameterList plist) {
		StringBuilder buf = new StringBuilder();
		for (String paramName : plist.getParamNames()) {
			if (buf.length() >0) {
				buf.append(" ");
			}
			buf.append(paramName);
			Expression defaultExp = plist.getDefaultExpression(paramName);
			if (defaultExp != null) {
				buf.append("=");
				buf.append(render(defaultExp));
			}
		}
		String catchall = plist.getCatchAll();
		if (catchall !=null) {
			if (buf.length() >0) buf.append(" ");
			buf.append(catchall + "...");
		}
		return buf.toString();
	}
	
	public String render(ParentheticalExpression node) {
		return "(" + render(node.nested) + ")";
	}
	
	public String render(PositionalArgsList plist) {
		StringBuilder buf = new StringBuilder();
		for (Expression exp : plist.getArgs()) {
			if (buf.length() >0) buf.append(", ");
			buf.append(render(exp));
		}
		return buf.toString();
	}
	
	public String render(PropertySetting node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#setting ");
		buf.append(node.key);
		buf.append(" = ");
		buf.append(render(node.value));
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(Range node) {
		StringBuilder buf = new StringBuilder();
		buf.append(render(node.left));
		buf.append("..");
		if (node.right != null) {
			String right = render(node.right);
			if (right.charAt(0) == '.') buf.append(" ");
			buf.append(right);
		}
		return buf.toString();
	}
	
	public String render(RecoveryBlock node) {
		TemplateElement content = node.getNestedBlock();
		String contents = content != null ? (String) render(content) : "";
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#recover");
		buf.append(CLOSE_BRACKET);
		buf.append(contents);
		buf.append(OPEN_BRACKET);
		buf.append("/#attempt");
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(RecurseNode node) {
		StringBuilder buf = new StringBuilder(); 
		buf.append(OPEN_BRACKET);
		buf.append("#recurse");
        if (node.targetNode != null) {
            buf.append(render(node.targetNode));
        }
		if (node.namespaces != null) {
			buf.append(" using ");
			buf.append(render(node.namespaces));
		}
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(ReturnInstruction node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#return");
		if (node.returnExp != null) {
			buf.append(" ");
			buf.append(render(node.returnExp));
		}
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}

	public String render(ScopedDirective node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#scoped ");
		Map vars = node.getVariables();
		for (Iterator it = vars.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String varname = (String) entry.getKey();
			varname = StringUtil.quoteStringIfNecessary(varname);
			Expression exp = (Expression) entry.getValue();
			buf.append(varname);
			if (exp != null) {
				buf.append("=");
				buf.append(render(exp));
			}
			if (it.hasNext()) buf.append(" ");
		}
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(StopInstruction node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#stop");
		if (node.message != null) {
			buf.append(" ");
			buf.append(render(node.message));
		}
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(StringLiteral node) { //REVISIT
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
		return result;
	}
	
	public String render(SwitchBlock node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#switch ");
		buf.append(render(node.testExpression));
		buf.append(CLOSE_BRACKET);
		List cases = node.getCases();
		if (cases != null && cases.size() >0) {
			for (int i=0; i<cases.size(); i++) {
				buf.append(render((Case) cases.get(i)));
			}
		}
		buf.append(OPEN_BRACKET);
		buf.append("/#switch");
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	
	public String render(TextBlock node) {
		String text = node.getText();
		try {
			text = node.getSource();
		} catch (Exception e) {} 
		if (!node.unparsed
			&& text.indexOf(OPEN_BRACKET + "#") <0
		    && text.indexOf(OPEN_BRACKET + "@") <0
		    && text.indexOf(OPEN_BRACKET + "/#") <0
		    && text.indexOf(OPEN_BRACKET + "/@") <0) 
		{
			return text;
		}
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#noparse");
		buf.append(CLOSE_BRACKET);
		buf.append(text);
		buf.append(OPEN_BRACKET);
		buf.append("/#noparse");
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(TransformBlock node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		buf.append("#transform ");
		buf.append(render(node.transformExpression));
		Map args = node.getArgs();
		for (Iterator it = args.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String varname = (String) entry.getKey();
			varname = StringUtil.quoteStringIfNecessary(varname);
			Expression value = (Expression) entry.getValue();
			buf.append(" ");
			buf.append(varname);
			buf.append("=");
			buf.append(render(value));
		}
		buf.append(CLOSE_BRACKET);
		TemplateElement nestedBlock = node.getNestedBlock();
		if (nestedBlock != null) {
			buf.append(render(nestedBlock));
		}
		buf.append(OPEN_BRACKET);
		buf.append("/#transform");
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	public String render(TrimInstruction node) {
		StringBuilder buf = new StringBuilder();
		buf.append(OPEN_BRACKET);
		if (node.left && node.right) {
			buf.append("#t");
		}
		else if (node.left) {
			buf.append("#lt");
		}
		else if (node.right) {
			buf.append("#rt");
		}
		else {
			buf.append("#nt");
		}
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	
	public String render(UnaryPlusMinusExpression node) {
		String op = node.isMinus ? "-" : "+";
		return op + render(node.target);
	}

	public String render(UnifiedCall node) {
		StringBuilder buf = new StringBuilder();
		Expression nodeNameExp = node.getNameExp();
		buf.append(OPEN_BRACKET);
		buf.append("@");
		buf.append(render(nodeNameExp));
		ArgsList args = node.getArgs();
		if (args != null) {
			buf.append(" ");
			buf.append(render(args));
		}
		ParameterList bodyParameters = node.getBodyParameters();
		if (bodyParameters != null) {
			buf.append("; ");
			buf.append(render(bodyParameters));
		}
		TemplateElement body = node.getNestedBlock();
		if (body == null) {
			buf.append("/");
			buf.append(CLOSE_BRACKET);
		} else {
			buf.append(CLOSE_BRACKET);
			buf.append(render(body));
			buf.append(OPEN_BRACKET);
			buf.append("/@");
			if (nodeNameExp instanceof Identifier) {
				buf.append(nodeNameExp);
			}
			buf.append(CLOSE_BRACKET);
		}
		return buf.toString();
	}
	
	public String render(VisitNode node) {
		StringBuilder buf = new StringBuilder(); 
		buf.append(OPEN_BRACKET);
		buf.append("#visit ");
		buf.append(render(node.targetNode));
		if (node.namespaces != null) {
			buf.append(" using ");
			buf.append(render(node.namespaces));
		}
		buf.append(CLOSE_BRACKET);
		return buf.toString();
	}
	
	static String quoteVarnameIfNecessary(String varname) {
		if (StringUtil.isFTLIdentifier(varname)) return varname;
		return "\"" + StringUtil.FTLStringLiteralEnc(varname) + "\"";
	}
    
}
	
