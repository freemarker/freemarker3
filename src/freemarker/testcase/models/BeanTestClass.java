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

package freemarker.testcase.models;

import junit.framework.AssertionFailedError;

/**
 * @author Attila Szegedi, szegedia at freemail dot hu
 * @version $Id: BeanTestClass.java,v 1.6 2003/01/12 23:40:25 revusky Exp $
 */
public class BeanTestClass extends BeanTestSuperclass implements BeanTestInterface<Integer>
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
