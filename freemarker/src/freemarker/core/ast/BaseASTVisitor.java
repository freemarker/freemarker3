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

package freemarker.core.ast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


import freemarker.core.parser.ParseException;


/**
 * A useful base class for writing tree-walking utilities
 * that walk the AST in a top-down manner.
 * 
 * The base implementations of visit(....) simply invoke
 * visit(...) on the subnodes (or do nothing if the node is terminal.)
 * 
 * For concrete implementations, see for example
 * {@link freemarker.core.helpers.DefaultTreeDumper} or
 * {@link freemarker.template.PostParseVisitor}
 * @author Jonathan Revusky
 */

public abstract class BaseASTVisitor {
	
	protected StringBuilder errors = new StringBuilder(), warnings = new StringBuilder();
	
	public void visit(TemplateNode node) {
		if (node == null) return;
    	try {
    		Class clazz = node.getClass();
        	Method visitMethod = this.getClass().getMethod("visit", new Class[] {clazz});
    		visitMethod.invoke(this, new Object[] {node});
    	}
    	catch (InvocationTargetException ite) {
    		Throwable cause = ite.getCause();
//    		cause.printStackTrace();
    		if (cause instanceof RuntimeException) {
    			throw (RuntimeException) cause;
    		}
    		throw new RuntimeException(ite);
    	}
    	catch (NoSuchMethodException nsme) {
    		if (node instanceof TemplateElement) {
    			recurse((TemplateElement) node);
    		}
    	}
    	catch (IllegalAccessException e) {
    		throw new RuntimeException(e.getMessage());
    	}
	}
	
	
	public void visit(AddConcatExpression node) {
		visit(node.left);
		visit(node.right);
	}
	
	public void visit(AndExpression node) {
		visit(node.left);
		visit(node.right);
	}
	
	public void visit(ArithmeticExpression node) {
		visit(node.left);
		visit(node.right);
	}
	
	public void visit(AssignmentInstruction node) {
		for (Expression e : node.getValues()) {
			visit(e);
		}
	}
	
	public void visit(AttemptBlock node) {
		recurse(node);
	}
	
	public void visit(BlockAssignment node) {
		recurse(node);
	}
	
	public void visit(BodyInstruction node) {
		visit(node.getArgs());
	}
	
	public void visit(BuiltInExpression node) {
		visit(node.getTarget());
	}
	
	
	public void visit(BooleanExpression node) {}
	
	
	public void visit(BreakInstruction node) {}
	
	public void visit(BuiltinVariable node) {}
	
	public void visit(Case node) {
		visit(node.expression);
		recurse(node);
	}
	
	public void visit(Comment node) {}
	
	public void visit(ComparisonExpression node) {
		visit(node.left);
		visit(node.right);
	}
	
	public void visit(CompressedBlock node) {
		recurse(node);
	}
	
	public void visit(ConditionalBlock node) {
		visit(node.condition);
		recurse(node);
	}
	
	public void visit(DefaultToExpression node) {
		visit(node.lhs);
		if (node.rhs != null) {
			visit(node.rhs);
		}
	}
	
	public void visit(DollarVariable node) {
		visit(node.expression); // Or do we visit escapedExpression ???
	}
	
	public void visit(Dot node) {
		visit(node.target);
	}
	
	public void visit(DynamicKeyName node) {
		visit(node.target);
		visit(node.nameExpression);
	}
	
	public void visit(EscapeBlock node) {
		visit(node.expr);
		visit(node.getEscapedExpression());
		recurse(node);
	}
	
	public void visit(ExistsExpression node) {
		visit(node.exp);
	}
	
	public void visit(FallbackInstruction node) {}
	
	public void visit(FlushInstruction node) {}
	
	public void visit(HashLiteral node) {
		List<Expression> keys = node.getKeys();
		List<Expression> values = node.getValues();
		for (int i=0; i< keys.size(); i++) {
			visit(keys.get(i));
			visit(values.get(i));
		}
	}
	
	public void visit(Identifier node) {}
	
	public void visit(IfBlock node) {
		recurse(node);
	}
	
	public void visit(Include node) {
		visit(node.getIncludedTemplateExpression());
		if (node.getParseExp() != null) {
			visit(node.getParseExp());
		}
	}

	public void visit(InvalidExpression node) {		
	}
	
	public void visit(IteratorBlock node) {
		visit(node.listExpression);
		recurse(node);
	}
	
	public void visit(LibraryLoad node) {
		visit(node.templateName);
	}
	
	public void visit(ListLiteral node) {
		for (Expression exp : node.getElements()) {
			visit(exp);
		}
	}
	
	
	public void visit(Macro node) {
		visit(node.getParams());
		recurse(node);
	}
	
	public void visit(MethodCall node) {
		visit(node.target);
		visit(node.getArgs());
	}
	
	public void visit(MixedContent node) {
		recurse(node);
	}
	
	public void visit(NamedArgsList node) {
		for (Expression exp : node.getArgs().values()) {
			visit(exp);
		}
	}
	
	public void visit(NoEscapeBlock node) {
		recurse(node);
	}
	
	public void visit(NotExpression node) {
		visit(node.target);
	}
	
	public void visit(NullLiteral node) {	}
	
	public void visit(NumberLiteral node) {}
	
	public void visit(NumericalOutput node) {
		visit(node.expression);
	}
	
	public void visit(OrExpression node) {
		visit(node.left);
		visit(node.right);
	}
	
	public void visit(ParameterList node) {
		for (String paramName : node.getParamNames()) {
			Expression defaultExp = node.getDefaultExpression(paramName);
			if (defaultExp != null) {
				visit(defaultExp);
			}
		}
	}
	
	public void visit(ParentheticalExpression node) {
		visit(node.nested);
	}
	
	
	public void visit(PositionalArgsList node) {
		for (Expression exp : node.args) {
			visit(exp);
		}
	}
	
	public void visit(PropertySetting node) {
		visit(node.value);
	}
	
	public void visit(Range node) {
		visit(node.left);
		if (node.right != null) visit(node.right);
	}
	
	public void visit(RecoveryBlock node) {
		recurse(node);
	}
	
	public void visit(RecurseNode node) {
		visit(node.targetNode);
		visit(node.namespaces);
	}
	
	public void visit(ReturnInstruction node) {
		visit(node.returnExp);
	}
	
	public void visit(VarDirective node) {
		for (Expression value : node.getVariables().values()) {
			visit(value);
		}
	}
	
	public void visit(StopInstruction node) {}
	
	public void visit(StringLiteral node) {}
	
	public void visit(SwitchBlock node) {
		visit(node.testExpression);
		recurse(node);
	}
	
	public void visit(TextBlock node) {}
	
	public void visit(TransformBlock node) {
		visit(node.transformExpression);
		if (node.namedArgs != null) {
			for (Expression exp : node.namedArgs.values()) {
				visit(exp);
			}
		}
		recurse(node);
	}
	
	public void visit(TrimInstruction node) {}
	
	public void visit(UnaryPlusMinusExpression node) {
		visit(node.target);
	}
	
	public void visit(UnifiedCall node) {
		visit(node.getNameExp());
		if (node.getArgs() != null) {
			visit(node.getArgs());
		}
		if (node.getBodyParameters() != null) {
			visit(node.getBodyParameters());
		}
		recurse(node);
	}
	
	public void visit(VisitNode node) {
		visit(node.targetNode);
		if (node.namespaces != null) visit(node.namespaces);
	}
	
	protected void recurse(TemplateElement node) {
        if (node.nestedElements != null) {
        	for (TemplateElement te : node.nestedElements) {
        		visit(te);
        	}
        } else {
        	visit(node.nestedBlock);
        }
	}
}
