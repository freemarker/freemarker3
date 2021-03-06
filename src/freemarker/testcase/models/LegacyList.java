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

package freemarker.testcase.models;

import freemarker.template.*;
import java.util.*;

/**
 * A little bridge class that subclasses the new SimpleList
 * and still implements the deprecated TemplateListModel
 */
public class LegacyList extends SimpleSequence {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5113695891943970389L;
	private Iterator iterator;

    /**
     * Resets the cursor to the beginning of the list.
     */
    public synchronized void rewind() {
        iterator = null;
    }

    /**
     * @return true if the cursor is at the beginning of the list.
     */
    public synchronized boolean isRewound() {
        return (iterator == null);
    }

    /**
     * @return true if there is a next element.
     */
    public synchronized boolean hasNext() {
        if (iterator == null) {
            iterator = list.listIterator();
        }
        return iterator.hasNext();
    }

    /**
     * @return the next element in the list.
     */
    public synchronized TemplateModel next() throws TemplateModelException {
        if (iterator == null) {
            iterator = list.listIterator();
        }
        if (iterator.hasNext()) {
            return (TemplateModel)iterator.next();
        } else {
            throw new TemplateModelException("No more elements.");
        }
    }
}
