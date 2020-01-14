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
