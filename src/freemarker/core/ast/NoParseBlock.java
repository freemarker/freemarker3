package freemarker.core.ast;

import java.util.List;

public class NoParseBlock extends MixedContent {
	
	private String startTag, endTag;

	public NoParseBlock(String startTag, String endTag, List<TextBlock> text) {
		this.startTag = startTag;
		this.endTag = endTag;
		for (TextBlock tb : text) addElement(tb);
	}
	
	public String getStartTag() {
		return startTag;
	}
	
	public String getEndTag() {
		return endTag;
	}

	public boolean isIgnorable() {
		for (TemplateElement elem : this.getNestedElements()) {
			if (!elem.isIgnorable()) return false;
		}
		return true;
	}
}
