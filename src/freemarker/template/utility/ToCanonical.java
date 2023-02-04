package freemarker.template.utility;

import freemarker.template.*;
import java.io.*;

/**
 * Read in a template and convert it to a canonical format.
 */

public class ToCanonical {

    static Configuration config = new Configuration();

    static public void main(String[] args) {
        config.setWhitespaceStripping(false);
        if (args.length == 0) {
            usage();
        }
        for (int i=0; i<args.length; i++) {
            File f = new File(args[i]);
            if (!f.exists()) {
                System.err.println("File " + f + " doesn't exist.");
            }
            try {
                convertFile(f);
            } catch (Exception e) {
                System.err.println("Error converting file: " + f);
                e.printStackTrace();
            }
        }
    }

    static void convertFile(File f) throws IOException {
        File fullPath = f.getAbsoluteFile();
        File dir = fullPath.getParentFile();
        String filename = fullPath.getName();
        File convertedFile = new File(dir, filename + ".canonical");
        config.setDirectoryForTemplateLoading(dir);
        Template template = config.getTemplate(filename);
        FileWriter output = new FileWriter(convertedFile);
        try {
            template.dump(output);
        } finally {
            output.close();
        }
    }

    static void usage() {
        System.err.println("Usage: java freemarker.template.utility.ToCanonical <filename(s)>");
    }
}
