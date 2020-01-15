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

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * A concrete implementation of {@link LocalizedString} that gets 
 * a localized string from a {@link java.util.ResourceBundle}  
 * @author Jonathan Revusky
 */

public class ResourceBundleLocalizedString extends LocalizedString {
	
	private String resourceKey, resourceBundleLookupKey;
	
	/**
	 * @param resourceBundleLookupKey The lookup key for the resource bundle
	 * @param resourceKey the specific resource (assumed to be a string) to fish out of the resource bundle
	 */
	
	public ResourceBundleLocalizedString(String resourceBundleLookupKey, String resourceKey) { 
		this.resourceBundleLookupKey = resourceBundleLookupKey;
		this.resourceKey = resourceKey;
	}

	public String getLocalizedString(Locale locale) throws TemplateModelException {
		try {
			ResourceBundle rb = ResourceBundle.getBundle(resourceBundleLookupKey, locale);
			return rb.getString(resourceKey);
		}
		catch (MissingResourceException mre) {
			throw new TemplateModelException("missing resource", mre);
		}
	}
}
