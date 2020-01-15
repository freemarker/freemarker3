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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * This class executes a FreeMarker template repeatedly in an endless loop.
 * It is meant to run inside a profiler to identify potential bottlenecks.
 * It can process either into a local file, or into a special /dev/null
 * style output stream.
 * @version $Id: PerformanceTest.java,v 1.18 2004/11/28 12:58:34 ddekany Exp $
 */
public class PerformanceTest
{
    public static void main(String[] args)
    throws
        Exception
    {
        Configuration config = new Configuration();
        // config.setDebugMode(false);
        config.setClassForTemplateLoading(PerformanceTest.class, "/freemarker/testcase");
        Template template = config.getTemplate("PerformanceTest.fm");
        boolean toFile = args.length > 0 && args[0].equals("file");
        File f = File.createTempFile("fmPerfTest", ".txt");
        f.deleteOnExit();
        OutputStream nullStream = new NullStream();
        SimpleHash h = new SimpleHash();
        h.put("ii", new TestSequence());
        h.put("j", new TestHash());
        h.put("k", new TestMethod());
        
        for(;;)
        {
            OutputStream stream = toFile ? new BufferedOutputStream(new FileOutputStream(f)) : nullStream;
            Writer writer = new OutputStreamWriter(stream, "UTF-8");
            try
            {
                template.process(h, writer);
            }
            finally
            {
                writer.close();
            }
        }
    }
    
    private static class TestSequence implements TemplateCollectionModel
    {
        public TemplateModelIterator iterator()
        {
            return new TemplateModelIterator()
            {
                private int i = 0;
                
                public TemplateModel next()
                {
                    return new TestI(i++);
                }

                public boolean hasNext()
                {
                    return i < 1000;
                }
            };
        };
    }
    
    private static class TestHash implements TemplateHashModel, TemplateScalarModel
    {
        public TemplateModel get(String key)
        {
            return this;
        }
        
        public String getAsString()
        {
            return "j";
        }

        public boolean isEmpty()
        {
            return false;
        }
    }

    private static class TestMethod implements TemplateMethodModelEx
    {
        public Object exec(List arguments)
        {
            return arguments.get(0);
        }
    }
        
    private static class TestI implements TemplateHashModel, TemplateNumberModel
    {
        private final int i;
        
        TestI(int i)
        {
            this.i = i;
        }
        
        public TemplateModel get(String key)
        {
            return (i & 1) == 1 ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
        }

        public String getAsString(Locale locale)
        {
            return Integer.toString(i);
        }

        public Number getAsNumber()
        {
            return new Integer(i);
        }
        
        public boolean isEmpty()
        {
            return false;
        }
    }
    
    private static class NullStream extends OutputStream
    {
        public void close()
        {
        }

        public void flush()
        {
        }

        public void write(byte[] arg0, int arg1, int arg2)
        {
        }

        public void write(byte[] arg0)
        {
        }

        public void write(int arg0)
        {
        }
    }
}
