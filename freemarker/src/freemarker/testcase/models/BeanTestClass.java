/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
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

package freemarker.testcase.models;

import junit.framework.AssertionFailedError;

/**
 * @author Attila Szegedi, szegedia at freemail dot hu
 * @version $Id: BeanTestClass.java,v 1.6 2003/01/12 23:40:25 revusky Exp $
 */
public class BeanTestClass
{
    public static final String STATIC_FINAL_FIELD = "static-final-field";
    public static String STATIC_FIELD = "static-field";
    
	public String getFoo()
	{
	    return "foo-value";
	}
	
	public String getBar(int index)
	{
	    return "bar-value-" + index;
	}
    
	
	public String overloaded(int i)
	{
	    return "overloaded-int-" + i;
	}
	
	public String overloaded(String s)
	{
	    return "overloaded-String-" + s;
	}
    
    @freemarker.template.Parameters("arg = 'default'")
	
	public static String staticMethod(String arg)
	{
	    return "static-method received: " + arg;
	}
	
	public static String staticOverloaded(int i)
	{
	    return "static-overloaded-int-" + i;
	}

	public static String staticOverloaded(String s)
	{
	    return "static-overloaded-String-" + s;
	}
        
        public String varArgs(int... is)
        {
            return "varArgs-int...";
        }
        
        public String varArgs(int i1, int i2)
        {
            return "varArgs-int,int";
        }

        public String varArgs(String s, String... ss)
        {
            /* 
             * This isn't invoked, but it triggers the "Vararg unwrap spec with
             * exactly one parameter more than the current spec influences the 
             * types of the current spec" rule when the varArgs(String) method
             * is processed.
             */
            throw new AssertionFailedError("Not supposed to be called");
        }

        public String varArgs(String s)
        {
            /* 
             * This isn't invoked ever, but is here only to have a 1-arg fixed 
             * arglen method as well, so when varArgs(1) is invoked, the method
             * map will be forced to rewrap the last argument.
             */
            throw new AssertionFailedError("Not supposed to be called");
        }
        
        public String moreSpecific(String s)
        {
            return "moreSpecific-String";
        }
        
        public String moreSpecific(Object s)
        {
            return "moreSpecific-Object";
        }
}
