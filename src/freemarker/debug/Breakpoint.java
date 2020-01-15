/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package freemarker.debug;

import java.io.Serializable;

/**
 * Represents a breakpoint location consisting of a template name and a line number.
 * @author Attila Szegedi
 * @version $Id: Breakpoint.java,v 1.1 2003/05/02 15:55:47 szegedia Exp $
 */
public class Breakpoint implements Serializable, Comparable
{
    private final String templateName;
    private final int line;
    
    /**
     * Creates a new breakpoint.
     * @param templateName the name of the template
     * @param line the line number in the template where to put the breakpoint
     */
    public Breakpoint(String templateName, int line)
    {
        this.templateName = templateName;
        this.line = line;
    }

    /**
     * Returns the line number of the breakpoint
     */
    public int getLine()
    {
        return line;
    }
    /**
     * Returns the template name of the breakpoint
     */
    public String getTemplateName()
    {
        return templateName;
    }

    public int hashCode()
    {
        return templateName.hashCode() + 31 * line;
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof Breakpoint)
        {
            Breakpoint b = (Breakpoint)o;
            return b.templateName.equals(templateName) && b.line == line;
        }
        return false;
    }
    
    public int compareTo(Object o)
    {
        Breakpoint b = (Breakpoint)o;
        int r = templateName.compareTo(b.templateName);
        return r == 0 ? line - b.line : r;
    }
    
    /**
     * Returns the template name and the line number separated with a colon
     */
    public String getLocationString()
    {
        return templateName + ":" + line;
    }
}
