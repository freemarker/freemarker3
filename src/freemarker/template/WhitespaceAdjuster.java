package freemarker.template;

import freemarker.core.ast.*;
import freemarker.core.parser.Node;
import freemarker.core.parser.ast.AssignmentInstruction;
import freemarker.core.parser.ast.BlockAssignment;
import freemarker.core.parser.ast.Comment;
import freemarker.core.parser.ast.ImportDeclaration;
import freemarker.core.parser.ast.MixedContent;
import freemarker.core.parser.ast.PropertySetting;
import freemarker.core.parser.ast.TemplateElement;
import freemarker.core.parser.ast.VarDirective;
import java.util.List;
import java.util.ArrayList;

public class WhitespaceAdjuster extends Node.Visitor {
	
	private Template template;
	
	public WhitespaceAdjuster(Template template) {
		this.template = template;
	}

	boolean ignoresSandwichedWhitespace(TemplateElement elem) {
		return (elem instanceof Macro) 
		       || (elem instanceof AssignmentInstruction) 
		       || (elem instanceof BlockAssignment) 
		       || (elem instanceof VarDirective)
		       || (elem instanceof ImportDeclaration)
		       || (elem instanceof PropertySetting)
		       || (elem instanceof Comment);
 	}

	public void visit(MixedContent node) {
		boolean atTopLevel = node.getParent() == null;
		recurse(node);
		List<TemplateElement> childElements = new ArrayList<TemplateElement>();
		TemplateElement prev = null;
		for (TemplateElement elem : node.childrenOfType(TemplateElement.class)) {
			if (!elem.isIgnorable()) {
				childElements.add(elem);
			}
		}
		for (int i=0; i<childElements.size(); i++) {
			TemplateElement elem = childElements.get(i);
			TemplateElement previous = (i==0) ? null : childElements.get(i-1);
			TemplateElement next = (i == childElements.size() -1) ? null : childElements.get(i+1); 
			if (elem instanceof TextBlock) {
				TextBlock text = (TextBlock) elem;
				if (text.isWhitespace()) {
					if (ignoresSandwichedWhitespace(previous) && ignoresSandwichedWhitespace(next)) {
						text.setIgnore(true);
					}
					if (previous == null && atTopLevel  
							&& (node.getBeginColumn() != 1 || ignoresSandwichedWhitespace(next))) {
						text.setIgnore(true);
					}
					
					if (next == null && atTopLevel && ignoresSandwichedWhitespace(prev)) {
						text.setIgnore(true);
					}
				}
			}
		}
	}

	public void visit(TextBlock node) {
		int nodeType = node.getBlockType();
		if (nodeType !=  TextBlock.OPENING_WS && nodeType != TextBlock.TRAILING_WS) {
			return;
		}
		int lineNumber = node.getBeginLine();
		boolean inMacro = PostParseVisitor.getContainingMacro(node) != null;
		boolean ignorable = template.stripWhitespace && !template.isOutputtingLine(lineNumber, inMacro);
		if (nodeType == TextBlock.OPENING_WS) {
			boolean deliberateLeftTrim = template.lineSaysLeftTrim(lineNumber);
			if (ignorable || deliberateLeftTrim) {
				node.setIgnore(true);
			}
		}
		if (nodeType == TextBlock.TRAILING_WS) {
			boolean deliberateRightTrim = template.lineSaysRightTrim(lineNumber);
			if (ignorable || deliberateRightTrim) {
				node.setIgnore(true);
			}
		}
	}

}
