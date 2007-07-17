package freemarker.core.parser;

import freemarker.core.ast.*;

import java.util.*;

public class PostParseVisitor extends BaseASTVisitor {
	
	protected boolean stripWhitespace, limitNesting;
	private StringBuffer errors = new StringBuffer(); 
	
	
	public PostParseVisitor(boolean stripWhitespace) {
		this.stripWhitespace = stripWhitespace;
		
	}
	
	public void visit(AssignmentInstruction node) {
		super.visit(node);
        if (node.type == AssignmentInstruction.LOCAL) {
        	TemplateElement parent = node;
        	while(!(parent instanceof Macro)) {
        		parent = parent.getParent();
        	}
        	for (String varname : node.getVarNames()) {
        		if (!parent.declaresScopedVariable(varname)) {
       				parent.declareScopedVariable(varname);
        		}
        	}
        }
	}
	
	public void visit(BlockAssignment node) {
		super.visit(node);
        if (node.type == AssignmentInstruction.LOCAL) {
        	TemplateElement parent = node;
        	while(!(parent instanceof Macro)) {
        		parent = parent.getParent();
        	}
       		parent.declareScopedVariable(node.varName);
        }
	}
	
	public void visit(IfBlock node) {
        if (node.getChildCount() == 1) {
            ConditionalBlock cblock = (ConditionalBlock) node.getChildAt(0);
            cblock.setIsSimple(true);
            try {
            	cblock.setLocation(node.getTemplate(), cblock, node);
            } catch (ParseException pe) {
            	appendToErrors(pe.getMessage());
            }
            node.getParent().replace(node, cblock);
            visit(cblock);
        } else {
            super.visit(node);
        }
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
	
	public void visit(ScopedDirective node) {
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
	
	public void visit(TextBlock node) {
		node.postParseCleanup(stripWhitespace);
	}
	
	protected void recurse(TemplateElement node){
		super.recurse(node);
		if (stripWhitespace) {
			node.removeIgnorableChildren();
		}
	}
	
	protected void appendToErrors(String message) {
		errors.append(message);
	}
}
