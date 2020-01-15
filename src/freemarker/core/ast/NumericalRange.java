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

package freemarker.core.ast;

import freemarker.template.*;

/**
 * A class that represents a Range between two integers.
 * inclusive of the end-points. It can be ascending or
 * descending. 
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */
class NumericalRange implements TemplateSequenceModel, java.io.Serializable {
    private static final long serialVersionUID = 8329795189999011437L;

    private int lower, upper;
    private boolean descending, norhs; // if norhs is true, then we have a half-range, like n..
    
    
    /**
     * Constructor for half-range, i.e. n..
     */
    public NumericalRange(int lower) {
        this.norhs = true;
        this.lower = lower;
    }

    public NumericalRange(int left, int right) {
        lower = Math.min(left, right);
        upper = Math.max(left, right);
        descending = (left != lower);
    }

    public TemplateModel get(int i) throws TemplateModelException {
        int index = descending ? (upper -i) : (lower + i);
        if ((norhs && index > upper) || index <lower) {
            throw new TemplateModelException("out of bounds of range");
        }
        return new SimpleNumber(index);
    }

    public int size() {
        return 1 + upper - lower;
    }
    
    boolean hasRhs() {
        return !norhs;
    }
}

