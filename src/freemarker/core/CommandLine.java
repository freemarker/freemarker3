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