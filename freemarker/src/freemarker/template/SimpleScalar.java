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

import java.io.Serializable;

/**
 * A simple implementation of the <tt>TemplateScalarModel</tt>
 * interface, using a <tt>String</tt>.
 * As of version 2.0 this object is immutable.
 *
 * <p>This class is thread-safe.
 *
 * @version $Id: SimpleScalar.java,v 1.38 2004/09/10 20:50:45 ddekany Exp $
 * @see SimpleSequence
 * @see SimpleHash
 */
public final class SimpleScalar 
implements TemplateScalarModel, Serializable {
    private static final long serialVersionUID = -5354606483655405575L;
    /**
     * @serial the value of this <tt>SimpleScalar</tt> if it wraps a
     * <tt>String</tt>.
     */
    private String value;

    /**
     * Constructs a <tt>SimpleScalar</tt> containing a string value.
     * @param value the string value.
     */
    public SimpleScalar(String value) {
        this.value = value;
    }

    public String getAsString() {
        return (value == null) ? "" : value;
    }

    public String toString() {
        return value;
    }
}
