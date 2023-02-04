package freemarker.core.helpers;
import freemarker.template.*;

import java.io.*; 

public class Canonicalize {
	static File outputDir = null;
	static String outputEncoding = "ISO-8859-1";
	static boolean verbose = true, angleBrackets, strictVars, assignmentConversion = true, existenceConversion = true;
	
    static public void main(String[] args) {
    	if (args.length == 0) {
    		info();
    		System.exit(-1);
    	}
		processOptions(args);
		if (!assignmentConversion && strictVars) {
			System.err.println("If you choose to convert to strictVars, you must also convert legacy assignment directives.");
			assignmentConversion = true;
		}
		if (outputDir != null && !outputDir.exists()) {
			boolean created = outputDir.mkdir();
			if (!created) {
				throw new IllegalArgumentException("Could not create directory " + outputDir);
			}
	    	if (verbose) {
	    		System.err.println("Created directory: " + outputDir.getAbsolutePath());
	    	}
		}
    	for (String arg : args) {
    		if (arg == null || arg.charAt(0) == '-') continue;
    		try {
    			String canonicalizedTemplate = canonicalizeTemplate(arg, angleBrackets);
    			String filename = new File(arg).getName();
    			File outFile = null;
    			if (outputDir != null) {
    				outFile = new File(outputDir, filename);
    			} else {
    				outFile = new File(filename + ".out");
    			}
    			if (verbose) {
    				System.err.println("outputting canonicalized template " + outFile);
    			}
    			FileOutputStream fos = new FileOutputStream(outFile);
    			OutputStreamWriter osw = new OutputStreamWriter(fos, outputEncoding);
    			osw.write(canonicalizedTemplate);
    			osw.flush();
    			osw.close();
    		}
    		catch (Exception e) {
    			System.err.println("error with template: " + arg);
    			e.printStackTrace();
    		}
    	}
    }
    
    static void processOptions(String[] args) {
    	for (int i = 0; i< args.length; i++) {
    		if (args[i] != null && args[i].charAt(0) == '-') {
    			if (args[i].length() == 1) {
    				args[i] = null;
    				continue;
    			}
    			String option = args[i].substring(1);
    			if (option.equals("d")) {
    				if (i + 1 == args.length) {
    					throw new IllegalArgumentException("Expecting directory after -d option");
    				}
    				outputDir = new File(args[i+1]);
					args[i+1] = null;
    				
    			}
    			else if (option.equals("e")) {
    				if (i+1 == args.length) {
    					throw new IllegalArgumentException("Expecting encoding after -e option");
    				}
    				outputEncoding = args[i+1];
    				args[i+1] = null;
    			}
    			else {
    				for (char c : option.toCharArray()) {
    					switch (c) {
    						case 'q' : verbose = false; break;
    						case 'o' : angleBrackets = true; break;
    						case 'a' : assignmentConversion = false; break;
    						case 'x' : existenceConversion = false; break;
    						case 's' : strictVars = true; break;
    						default : throw new IllegalArgumentException("Unknown option " + c);
    					}
    				}
    			}
    		}
    	}
    }
    
    static public String canonicalizeTemplate(String filename, boolean angleBrackets) 
    throws IOException {
        File file = new File(filename).getCanonicalFile();
        Configuration conf = new Configuration();
        conf.setWhitespaceStripping(false);
        conf.setDirectoryForTemplateLoading(file.getParentFile());
        if (verbose) {
        	System.err.println("Reading template: " + file.getName());
        }
        Template template = conf.getTemplate(file.getName());
        CanonicalizingTreeDumper dumper = new CanonicalizingTreeDumper(!angleBrackets);
        dumper.convertAssignments = assignmentConversion;
        dumper.convertExistence = existenceConversion;
        dumper.strictVars = strictVars;
        if (verbose) {
        	System.err.println("Canonicalizing template: ");
        }
        return dumper.render(template);
    }
    
    static void info() {
    	System.err.println();
    	System.err.println("FTL canonicalizer version 0.1pre Copyright Jonathan Revusky 2007-2023");
    	System.err.println();
    	System.err.println("Converts FTL templates to square bracket syntax and newer constructs in FM 2.4");
    	System.err.println("By default (unless you use -d option) outputs to filename.out");
    	System.err.println("This utility is still in development and has certain limitations, use at own risk.");
    	System.err.println();
    	System.err.println("Usage: java freemarker.core.helpers.Canonicalize <options> <files>");
    	System.err.println("Available options:");
    	System.err.println("   -q : quiet operation");
    	System.err.println("   -d <output directory> : output to directory");
    	System.err.println("   -e <encoding> : output encoding (default is ISO-8859-1)");
    	System.err.println("   -o : use older angle bracket syntax");
    	System.err.println("   -a : Do not convert to #set syntax");
    	System.err.println("   -x : Do not convert existence built-ins to shorter syntax");
    	System.err.println("   -s : Convert to strict_vars");
    	System.err.println();
    }
    
}
