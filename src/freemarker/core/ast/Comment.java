package freemarker.core.ast;

import freemarker.core.Environment;

/**
 * A template element where the content is ignored, a Comment.
 */
public class Comment extends TemplateElement {

    private String text;

    public Comment(String text) {
        this.text = text;
    }
    
    public String getText() {
    	return text;
    }

    public void execute(Environment env) {
    // do nothing, skip the body
    }

    public String getDescription() {
        String s = text.trim();
        if (s.length() > 20) {
            s = s.substring(0, 20) + "...";
        }
        return "comment (" + s + ")";
    }
}
