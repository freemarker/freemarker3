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

/**
 * Model for testing the impact of isEmpty() on template list models. Every
 * other method simply delegates to a SimpleList model.
 *
 * @author  <a href="mailto:run2000@users.sourceforge.net">Nicholas Cull</a>
 * @version $Id: BooleanList1.java,v 1.16 2004/01/06 17:06:44 szegedia Exp $
 */
public class BooleanList1 implements TemplateSequenceModel {

    private LegacyList  cList;

    /** Creates new BooleanList1 */
    public BooleanList1() {
        cList = new LegacyList();
        cList.add( "false" );
        cList.add( "0" );
        cList.add(TemplateBooleanModel.FALSE);
        cList.add(TemplateBooleanModel.TRUE);
        cList.add(TemplateBooleanModel.TRUE);
        cList.add(TemplateBooleanModel.TRUE);
        cList.add(TemplateBooleanModel.FALSE);
    }

    /**
     * @return true if there is a next element.
     */
    public boolean hasNext() {
        return cList.hasNext();
    }

    /**
     * @return the next element in the list.
     */
    public TemplateModel next() throws TemplateModelException {
        return cList.next();
    }

    /**
     * @return true if the cursor is at the beginning of the list.
     */
    public boolean isRewound() {
        return cList.isRewound();
    }

    /**
     * @return the specified index in the list
     */
    public TemplateModel get(int i) throws TemplateModelException {
        return cList.get(i);
    }

    /**
     * Resets the cursor to the beginning of the list.
     */
    public void rewind() {
        cList.rewind();
    }

    public int size() {
        return cList.size();
    }

}
