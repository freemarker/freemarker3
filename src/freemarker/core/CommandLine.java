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

package freemarker.core;

import freemarker.template.*;
import java.io.*;

/**
 * FreeMarker command-line utility, the Main-Class of <tt>freemarker.jar</tt>.
 * If invoked with no parameters it just prints the version number.
 * If you invoke it with the filename, it reads int the file as a template
 * and processes it with an empty data model, sending the output to stdout.
 * 
 * Note that this command-line utility mostly exists as a convenient entry
 * point for debugging/testing when setting up a development environment.
 * For a serious freemarker-based command-line file processing tool we recommend  
 * <a href="http://fmpp.sourceforge.net">FMPP</a>.
 *
 */
public class CommandLine {
	
	
	public static void main(String[] args) {
		if (args.length == 0) {
		    info();
		}
		else try {
			processTemplate(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * processes template with an empty data model
	 * @param filename name of file
	 * @throws IOException
	 */
	public static void processTemplate(String filename) throws IOException, TemplateException {
		File file = new File(filename).getCanonicalFile();
		Configuration conf = new Configuration();
		conf.setDirectoryForTemplateLoading(file.getParentFile());
		Template template = conf.getTemplate(file.getName());
		Writer out = new OutputStreamWriter(System.out);
		template.process(null, out);
	}
    
    public static void info() {
        System.out.println();
        System.out.print("FreeMarker version ");
        System.out.println(Configuration.getVersionNumber());
        System.out.println();
        System.out.println("Copyright (c) 2020 Jonathan Revusky.");
        System.out.println("All rights reserved.");
        System.out.println();
        System.out.println("This is Free software. Please read the LICENSE.txt comes with ");
        System.out.println("the distribution for more details.");
        System.out.println();
        System.out.println("For more information and for updates visit our WWW site:");
        System.out.println("http://freemarker.es/");
        System.out.println();
    }
}