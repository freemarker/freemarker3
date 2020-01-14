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

package freemarker.testcase;

import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

import freemarker.cache.StrongCacheStorage;
import freemarker.cache.TemplateCache;
import freemarker.cache.TemplateLoader;
import junit.framework.TestCase;

public class TemplateCacheTest extends TestCase
{
    public TemplateCacheTest(String name)
    {
        super(name);
    }

    public void testCachedException() throws Exception
    {
        MockTemplateLoader loader = new MockTemplateLoader();
        TemplateCache cache = new TemplateCache(loader, new StrongCacheStorage());
        cache.setDelay(1000L);
        loader.setThrowException(true);
        try
        {
            cache.getTemplate("t", Locale.getDefault(), "", true);
            fail();
        }
        catch(IOException e)
        {
            assertEquals("mock IO exception", e.getMessage());
            assertEquals(1, loader.getFindCount());
            try
            {
                cache.getTemplate("t", Locale.getDefault(), "", true);
                fail();
            }
            catch(IOException e2)
            {
                // Still 1 - returned cached exception
                assertEquals("There was an error loading the template on an " +
                        "earlier attempt; it is attached as a cause", e2.getMessage());
                assertSame(e, e2.getCause());
                assertEquals(1, loader.getFindCount());
                try
                {
                    Thread.sleep(1100L);
                    cache.getTemplate("t", Locale.getDefault(), "", true);
                    fail();
                }
                catch(IOException e3)
                {
                    // Cache had to retest
                    assertEquals("mock IO exception", e.getMessage());
                    assertEquals(2, loader.getFindCount());
                }
            }
        }
    }
    
    public void testCachedNotFound() throws Exception
    {
        MockTemplateLoader loader = new MockTemplateLoader();
        TemplateCache cache = new TemplateCache(loader, new StrongCacheStorage());
        cache.setDelay(1000L);
        cache.setLocalizedLookup(false);
        assertNull(cache.getTemplate("t", Locale.getDefault(), "", true));
        assertEquals(1, loader.getFindCount());
        assertNull(cache.getTemplate("t", Locale.getDefault(), "", true));
        // Still 1 - returned cached exception
        assertEquals(1, loader.getFindCount());
        Thread.sleep(1100L);
        assertNull(cache.getTemplate("t", Locale.getDefault(), "", true));
        // Cache had to retest
        assertEquals(2, loader.getFindCount());
    }

    private static class MockTemplateLoader implements TemplateLoader
    {
        private boolean throwException;
        private int findCount; 
        
        public void setThrowException(boolean throwException)
        {
           this.throwException = throwException;
        }
        
        public int getFindCount()
        {
            return findCount;
        }
        
        public void closeTemplateSource(Object templateSource)
                throws IOException
        {
            // TODO Auto-generated method stub
            
        }

        public Object findTemplateSource(String name) throws IOException
        {
            ++findCount;
            if(throwException)
            {
                throw new IOException("mock IO exception");
            }
            return null;
        }

        public long getLastModified(Object templateSource)
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public Reader getReader(Object templateSource, String encoding)
                throws IOException
        {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
}
