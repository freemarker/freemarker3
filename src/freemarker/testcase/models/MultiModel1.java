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
 * Testcase to see how FreeMarker deals with multiple Template models.
 *
 * @version $Id: MultiModel1.java,v 1.17 2004/01/06 17:06:44 szegedia Exp $
 */
public class MultiModel1 implements TemplateHashModel,
        TemplateSequenceModel, TemplateScalarModel {

    private TemplateModel m_cSubModel = new MultiModel2();
    private TemplateModel m_cListHashModel1 = new MultiModel4();
    private TemplateModel m_cListHashModel2 = new MultiModel5();
    private TemplateSequenceModel m_cListModel = new SimpleSequence();
    private TemplateHashModel m_cHashModel = new SimpleHash();

    /** Creates new MultiModel1 */
    public MultiModel1() {
        for( int i = 0; i < 10; i++ ) {
            ((SimpleSequence)m_cListModel).add( "Model1 value: " + Integer.toString( i ));
        }
        ((SimpleSequence)m_cListModel).add( new MultiModel3() );
        ((SimpleHash)m_cHashModel).put( "nested", new MultiModel3() );
    }

    /**
     * Gets a <tt>TemplateModel</tt> from the hash.
     *
     * @param key the name by which the <tt>TemplateModel</tt>
     * is identified in the template.
     * @return the <tt>TemplateModel</tt> referred to by the key,
     * or null if not found.
     */
    public TemplateModel get(String key) {
        if( key.equals( "model2" )) {
            return m_cSubModel;
        } else if( key.equals( "modellist" )) {
            return m_cListModel;
        } else if( key.equals( "selftest" )) {
            return new SimpleScalar( "Selftest of a hash from MultiModel1" );
        } else if( key.equals( "one" )) {
            return m_cListHashModel1;
        } else if( key.equals( "two" )) {
            return m_cListHashModel2;
        } else if( key.equals( "size" )) {
            return new SimpleScalar( "Nasty!" );
        } else if( key.equals( "nesting1" )) {
            return m_cHashModel;
        } else {
            return null;
        }
    }

    /**
     * @return true if this object is empty.
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * @return the specified index in the list
     */
    public TemplateModel get(int i) throws TemplateModelException {
        return m_cListModel.get( i );
    }

    /**
     * Returns the scalar's value as a String.
     *
     * @return the String value of this scalar.
     */
    public String getAsString() {
        return "MultiModel1 as a string!";
    }

    public int size() throws TemplateModelException {
        return m_cListModel.size();
    }
}
