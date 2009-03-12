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

package freemarker.core.parser;

import java.util.Locale;

import freemarker.template.Template;

/**
 * A base class for all objects that encapsulate 
 * template location information.
 * @author revusky
 */

public class TemplateLocation {
	
	protected int beginLine, beginColumn, endLine, endColumn;
	protected Template template;
	
	static public int TAB_SIZE = 8;
	
	public final String getDescription(Locale locale) {
		return "";
	}

	public String getDescription() {
		return "";
	}
	
	public int getBeginColumnTabAdjusted() {
		return (template == null) ? beginColumn 
                : template.getTabAdjustedColumn(beginLine, beginColumn, TAB_SIZE);
	}
	
	public int getBeginColumn() {
		return beginColumn;
	}
	

	public int getBeginLine() {
		return beginLine;
	}
	
	public int getEndLine() {
		return endLine;
	}
	
	public int getEndColumn() {
		return endColumn;
	}
	
	public int getEndColumnTabAdjusted() {
		return template==null ? endColumn 
            : template.getTabAdjustedColumn(beginLine, endColumn, TAB_SIZE);
			
	}
	
	public String getSource() {
		return template.getSource(beginColumn, beginLine, endColumn, endLine);
	}
	
	public String toString(Locale locale) {
		return toString(); //TODO at some point
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
        setLocation(template, begin.beginColumn, begin.beginLine, end.endColumn, end.endLine);
    }
    
    public final void setLocation(Template template, Token begin, TemplateLocation end)
    {
        setLocation(template, begin.beginColumn, begin.beginLine, end.endColumn, end.endLine);
    }

    public final void setLocation(Template template, TemplateLocation begin, Token end)
    {
        setLocation(template, begin.beginColumn, begin.beginLine, end.endColumn, end.endLine);
    }

    public final void setLocation(Template template, TemplateLocation begin, TemplateLocation end)
    {
        setLocation(template, begin.beginColumn, begin.beginLine, end.endColumn, end.endLine);
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
