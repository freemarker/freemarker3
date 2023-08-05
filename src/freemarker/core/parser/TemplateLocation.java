package freemarker.core.parser;

import freemarker.core.parser.ast.BaseNode;
import freemarker.template.Template;

/**
 * A base class for all objects that encapsulate 
 * template location information.
 * @author revusky
 */

public class TemplateLocation extends BaseNode {
	
	protected int beginLine, beginColumn, endLine, endColumn;
	protected Template template;
	
	static public int TAB_SIZE = 8;
	
	public String getDescription() {
		return "";
	}
	
	public int getBeginColumnTabAdjusted() {
		return (template == null) ? beginColumn 
                : template.getTabAdjustedColumn(beginLine, beginColumn, TAB_SIZE);
	}

    @Override
	public int getBeginColumn() {
		return beginColumn;
	}
	

    @Override
	public int getBeginLine() {
		return beginLine;
	}
	
    @Override
	public int getEndLine() {
		return endLine;
	}
	
    @Override
	public int getEndColumn() {
		return endColumn;
	}
	
	public int getEndColumnTabAdjusted() {
		return template==null ? endColumn 
            : template.getTabAdjustedColumn(beginLine, endColumn, TAB_SIZE);
			
	}
	
	public String source() {
		return template.source(beginColumn, beginLine, endColumn, endLine);
	}
	
	public String toString() {
		String templateName = template == null ? "input" : template.getName();
        return "on line " + getBeginLine() + ", column " + beginColumn 
        	+ " in " + templateName;
	}
	
    /**
     * Returns a string that indicates
     * where in the template source, this object is.
     */
    public String getStartLocation() {
        String templateName = template != null ? template.getName() : "input";
        return "on line " 
              + beginLine 
              + ", column " 
              + getBeginColumn()
              + " in "
              + templateName;
    }

    public String getEndLocation() {
        String templateName = template != null ? template.getName() : "input";
        return "on line " 
              + endLine
              + ", column "
              + getEndColumn()
              + " in "
              + templateName;
    }
    
    /**
     * @return whether the point in the template file specified by the 
     * column and line numbers is contained within this template object.
     */
    public boolean contains(int column, int line) {
        if (line < beginLine || line > endLine) {
            return false;
        }
        if (line == beginLine) {
            if (column < beginColumn) {
                return false;
            }
        }
        if (line == endLine) {
            if (column > endColumn) {
                return false;
            }
        }
        return true;
    }

    public Template getTemplate()
    {
        return template;
    }

    public TemplateLocation copyLocationFrom(TemplateLocation from)
    {
        template = from.template;
        beginColumn = from.beginColumn;
        beginLine = from.beginLine;
        endColumn = from.endColumn;
        endLine = from.endLine;
        return this;
    }
    
    public final void setLocation(Template template, Token begin, Token end)
    {
        setLocation(template, begin.getBeginColumn(), begin.getBeginLine(), end.getEndColumn(), end.getEndLine());
    }
    
    public final void setLocation(Template template, Token begin, TemplateLocation end)
    {
        setLocation(template, begin.getBeginColumn(), begin.getBeginLine(), end.getEndColumn(), end.getEndLine());
    }

    public final void setLocation(Template template, TemplateLocation begin, Token end)
    {
        setLocation(template, begin.getBeginColumn(), begin.getBeginLine(), end.getEndColumn(), end.getEndLine());
    }

    public final void setLocation(Template template, TemplateLocation begin, TemplateLocation end)
    {
        setLocation(template, begin.getBeginColumn(), begin.getBeginLine(), end.getEndColumn(), end.getEndLine());
    }

    public void setLocation(Template template, int beginColumn, int beginLine, int endColumn, int endLine)
    {
        this.template = template;
        this.beginColumn = beginColumn;
        this.beginLine = beginLine;
        this.endColumn = endColumn;
        this.endLine = endLine;
    }
}
