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

import freemarker.core.Environment;

import java.util.Locale;

/**
 * An abstract base class for scalars that vary by locale.
 * Here is a silly usage example.
 * <code>
 *    TemplateScalarModel localizedYes = new LocalizedString() {
 *        public String getLocalizedString(java.util.Locale locale) {
 *            String lang = locale.getLanguage();
 *            if "fr".equals(lang)
 *               return "oui";
 *            else if "de".equals(lang)
 *               return "s\u00ED";
 *            else
 *               return "yes";
 *        }
 *    };
 * </code>
 * @author Jonathan Revusky
 */

abstract public class LocalizedString implements TemplateScalarModel {


        public String getAsString() throws TemplateModelException {
                Environment env = Environment.getCurrentEnvironment();
                Locale locale = env.getLocale();
                return getLocalizedString(locale);
        }

        abstract public String getLocalizedString(Locale locale) throws TemplateModelException;
}
