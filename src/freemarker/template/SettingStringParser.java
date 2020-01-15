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

package freemarker.template;

import java.util.ArrayList;
import java.util.HashMap;

import freemarker.core.parser.ParseException;
import freemarker.template.utility.StringUtil;

/**
 * Helper class for parsing setting values given with string.
 */
class SettingStringParser {
    private String text;
    private int p;
    private int ln;

    SettingStringParser(String text) {
        this.text = text;
        this.p = 0;
        this.ln = text.length();
    }

    ArrayList<String> parseAsList() throws ParseException {
        char c;
        ArrayList<String> seq = new ArrayList<String>();
        while (true) {
            c = skipWS();
            if (c == ' ') break;
            seq.add(fetchStringValue());
            c = skipWS();
            if (c == ' ') break;
            if (c != ',') throw new ParseException(
                    "Expected \",\" or the end of text but "
                    + "found \"" + c + "\"", 0, 0);
            p++;
        }
        return seq;
    }

    HashMap<String, String> parseAsImportList() throws ParseException {
        char c;
        HashMap<String, String> map = new HashMap<String, String>();
        while (true) {
            c = skipWS();
            if (c == ' ') break;
            String lib = fetchStringValue();

            c = skipWS();
            if (c == ' ') throw new ParseException(
                    "Unexpected end of text: expected \"as\"", 0, 0);
            String s = fetchKeyword();
            if (!s.equalsIgnoreCase("as")) throw new ParseException(
                    "Expected \"as\", but found " + StringUtil.jQuote(s), 0, 0);

            c = skipWS();
            if (c == ' ') throw new ParseException(
                    "Unexpected end of text: expected gate hash name", 0, 0);
            String ns = fetchStringValue();
            
            map.put(ns, lib);

            c = skipWS();
            if (c == ' ') break;
            if (c != ',') throw new ParseException(
                    "Expected \",\" or the end of text but "
                    + "found \"" + c + "\"", 0, 0);
            p++;
        }
        return map;
    }

    String fetchStringValue() throws ParseException {
        String w = fetchWord();
        if (w.startsWith("'") || w.startsWith("\"")) {
            w = w.substring(1, w.length() - 1);
        }
        return StringUtil.FTLStringLiteralDec(w);
    }

    String fetchKeyword() throws ParseException {
        String w = fetchWord();
        if (w.startsWith("'") || w.startsWith("\"")) {
            throw new ParseException(
                "Keyword expected, but a string value found: " + w, 0, 0);
        }
        return w;
    }

    char skipWS() {
        char c;
        while (p < ln) {
            c = text.charAt(p);
            if (!Character.isWhitespace(c)) return c;
            p++;
        }
        return ' ';
    }

    private String fetchWord() throws ParseException {
        if (p == ln) throw new ParseException(
                "Unexpeced end of text", 0, 0);

        char c = text.charAt(p);
        int b = p;
        if (c == '\'' || c == '"') {
            boolean escaped = false;
            char q = c;
            p++;
            while (p < ln) {
                c = text.charAt(p);
                if (!escaped) {
                    if (c == '\\') {
                        escaped = true;
                    } else if (c == q) {
                        break;
                    }
                } else {
                    escaped = false;
                }
                p++;
            }
            if (p == ln) {
                throw new ParseException("Missing " + q, 0, 0);
            }
            p++;
            return text.substring(b, p);
        } else {
            do {
                c = text.charAt(p);
                if (!(Character.isLetterOrDigit(c)
                        || c == '/' || c == '\\' || c == '_'
                        || c == '.' || c == '-' || c == '!'
                        || c == '*' || c == '?')) break;
                p++;
            } while (p < ln);
            if (b == p) {
                throw new ParseException("Unexpected character: " + c, 0, 0);
            } else {
                return text.substring(b, p);
            }
        }
    }
}