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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import freemarker.core.parser.ParseException;
import freemarker.template.Configuration;

/**
 * Test case for parser. The contents of the specified file is supposed to cause
 * a ParseException when parsed by the FMParser.
 * 
 * @version $Id$
 */
public class ParserTestCase extends TestCase {

    private String filename;

    private Configuration conf = new Configuration();

    public ParserTestCase(String name, String filename) throws IOException {
        super(name);
        this.filename = filename;

//        URL url = getClass().getResource("testcases.xml");
//        File parent = new File(url.getFile()).getParentFile();
//        File dir = new File(parent, "template");
        File dir = new File("src/freemarker/testcase/template");
        conf.setDirectoryForTemplateLoading(dir);
    }

    protected void runTest() throws Throwable {
        try {
            conf.getTemplate(filename);
            fail("ParseException expected.");
        } catch (ParseException pe) {
            return;
        }
    }

}
