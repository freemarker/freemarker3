package freemarker.core.helpers;

import freemarker.core.ast.*;
import freemarker.core.parser.ast.BooleanLiteral;
import freemarker.core.parser.ast.NullLiteral;
import freemarker.core.parser.ast.Identifier;
import freemarker.core.parser.ast.TemplateNode;

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
		visit(node.getLeft());
		buffer.append("+");
		visit(node.getRight());
	}
	
	public void visit(AndExpression node) {
		visit(node.getLeft());
		buffer.append("&&");
		visit(node.getRight());
	}
	
	public void visit(ArithmeticExpression node) {
		String opString = null;
		switch (node.getOperation()) {
		   case ArithmeticExpression.DIVISION : opString = "/"; break;  
		   case ArithmeticExpression.MODULUS : opString = "%"; break;
		   case ArithmeticExpression.MULTIPLICATION : opString = "*"; break;
		   case ArithmeticExpression.SUBTRACTION : opString = "-"; break;
		}
		visit(node.getLeft());
		buffer.append(opString);
		visit(node.getRight());
	}
	
	public void visit(AssignmentInstruction node) {
		switch (node.getBlockType()) {
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
		String varname = StringUtil.quoteStringIfNecessary(node.getVarName());
		Expression nsExp = node.getNamespaceExpression();
		String instruction = null;
		switch(node.getBlockType()) {
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
		visit(node.firstChildOfType(TemplateElement.class));
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
		buffer.append(node.isValue() ? "true" : "false"); 
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
		buffer.append(node.getName());
	}
	
	public void visit(Case node) {
		buffer.append(OPEN_BRACKET);
		if (node.isDefault()) {
			buffer.append("#default");
		} else {
			buffer.append("#case ");
			visit(node.getExpression());
		}
		buffer.append(CLOSE_BRACKET);
		visit(node.firstChildOfType(TemplateElement.class));
	}
	
	public void visit(Comment node) {
		buffer.append(OPEN_BRACKET);
		buffer.append("#--");
		buffer.append(node.getText());
		buffer.append("--");
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(ComparisonExpression node) {
		visit(node.getLeft());
		boolean usingAltSyntax = CLOSE_BRACKET.equals("]");
		switch(node.getOperation()) {
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
		visit(node.getRight());
	}
	
	
	public void visit(CompressedBlock node) {
		openDirective("compress");
		buffer.append(CLOSE_BRACKET);
		visit(node.firstChildOfType(TemplateElement.class));
		closeDirective("compress");
	}
	
	public void visit(ConditionalBlock node) {
		buffer.append(OPEN_BRACKET);
		if (node.isFirst()) {
			buffer.append("#if ");
		}
		else if (node.getCondition() == null) {
			buffer.append("#else");
		}
		else {
			buffer.append("#elseif ");
		}
		visit(node.getCondition());
		buffer.append(CLOSE_BRACKET);
		visit(node.firstChildOfType(TemplateElement.class));
		if (node.isLoneIfBlock()) {
			closeDirective("if");
		}
	}
	
	public void visit(DefaultToExpression node) {
		visit(node.getLeft());
		buffer.append("!");
		visit(node.getLeft());
	}
	
	public void visit(Interpolation node) {
		buffer.append("${");
		visit(node.getExpression());
		buffer.append("}");
	}
	
	public void visit(Dot node) {
		visit(node.getTarget());
		buffer.append(".");
		buffer.append(node.getKey());
	}
	
	public void visit(DynamicKeyName node) {
		visit(node.getTarget());
		buffer.append("[");
		visit(node.getNameExpression());
		buffer.append("]");
	}
	
	public void visit(EscapeBlock node) {
		openDirective("escape ");
		buffer.append(node.getVariable());
		buffer.append(" as ");
		visit(node.getExpression());
		buffer.append(CLOSE_BRACKET);
		visit(node.firstChildOfType(TemplateElement.class));
		closeDirective("escape");
	}
	
	public void visit(ExistsExpression node) {
		visit(node.getExpression());
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
		buffer.append(node.getName());
	}
	
	public void visit(IfBlock node) {
		for (TemplateNode block : node.getSubBlocks()) {
			visit(block);
			
		}
		closeDirective("if");
	}
	
	public void visit(Include node) {
		if (node.isFreshNamespace()) openDirective("embed ");
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
		visit(node.getListExpression());
		buffer.append(" as ");
		buffer.append(node.getIndexName());
		buffer.append(CLOSE_BRACKET);
		visit(node.firstChildOfType(TemplateElement.class));
		closeDirective("list");
	}
	
	public void visit(LibraryLoad node) {
		openDirective("import ");
		visit(node.getTemplateNameExpression());
		buffer.append(" as ");
		buffer.append(node.getNamespace());
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
		visit(node.firstChildOfType(TemplateElement.class));
		if (node.isFunction()) {
			closeDirective("function");
		} else {
			closeDirective("macro");
		}
	}
	
	public void visit(MethodCall node) {
		visit(node.getTarget());
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
		visit(node.firstChildOfType(TemplateElement.class));
		closeDirective("noescape");
	}
	
	public void visit(NotExpression node) {
		buffer.append("!");
		visit(node.getTarget());
	}
	
	public void visit(NullLiteral node) {
		buffer.append("null");
	}
	
	public void visit(NumberLiteral node) {
		buffer.append(node.getValue().toString());
	}
	
	public void visit(NumericalOutput node) {
		buffer.append("#{");
		visit(node.getExpression());
		String formatString = node.getFormatString();
		if (formatString != null) {
			buffer.append(" ; ");
			buffer.append(formatString);
		}
		buffer.append("}");
	}
	
	public void visit(OrExpression exp) {
		visit(exp.getLeft());
		buffer.append(" || ");
		visit(exp.getRight());
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
		visit(node.getNested());
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
		buffer.append(node.getKey());
		buffer.append(" = ");
		visit(node.getValue());
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(Range node) {
		visit(node.getLeft());
		buffer.append("..");
		if (node.getRight() != null) {
			String right = render(node.getRight());
			if (right.charAt(0) == '.') buffer.append(" ");
			buffer.append(right);
		}
	}
	
	public void visit(RecoveryBlock node) {
		openDirective("recover");
		buffer.append(CLOSE_BRACKET);
		visit(node.firstChildOfType(TemplateElement.class));
		closeDirective("attempt");
	}
	
	public void visit(RecurseNode node) {
		openDirective("recurse ");
		visit(node.getTargetNode());
		if (node.getNamespaces() != null) {
			buffer.append(" using ");
			visit(node.getNamespaces());
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
		buffer.append(render(node.getTestExpression()));
		buffer.append(CLOSE_BRACKET);
		List<Case> cases = node.getCases();
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
		visit(node.getTransformExpression());
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
		visit(node.firstChildOfType(TemplateElement.class));
		closeDirective("transform");
	}
	
	public void visit(TrimInstruction node) {
		if (node.isLeft() && node.isRight()) {
			openDirective("t");
		}
		else if (node.isLeft()) {
			openDirective("lt");
		}
		else if (node.isRight()) {
			openDirective("rt");
		}
		else {
			openDirective("nt");
		}
		buffer.append(CLOSE_BRACKET);
	}
	
	public void visit(TrimBlock node) {
		String tagName = "nt_lines";
		if (node.isLeft() && node.isRight()) {
			tagName = "t_lines";
		} else if (node.isLeft()) {
			tagName = "lt_lines";
		} else if (node.isRight()) {
			tagName = "rt_lines";
		}
		openDirective(tagName);
		visit(node.firstChildOfType(TemplateElement.class));
		closeDirective(tagName);
	}
	
	
	public void visit(UnaryPlusMinusExpression node) {
		String op = node.isMinus() ? "-" : "+";
		buffer.append(op);
		visit(node.getTarget());
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
		TemplateElement body = node.firstChildOfType(TemplateElement.class);
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
		visit(node.getTargetNode());
		if (node.getNamespaces() != null) {
			buffer.append(" using ");
			visit(node.getNamespaces());
		}
		buffer.append(CLOSE_BRACKET);
	}
	
	static String quoteVarnameIfNecessary(String varname) {
		if (StringUtil.isFTLIdentifier(varname)) return varname;
		return "\"" + StringUtil.FTLStringLiteralEnc(varname) + "\"";
	}
    
}
	
