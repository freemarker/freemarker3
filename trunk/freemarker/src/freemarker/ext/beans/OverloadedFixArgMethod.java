/*
 * Copyright (c) 2003 The Visigoth Software Society. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Visigoth Software Society (http://www.visigoths.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. Neither the name "FreeMarker", "Visigoth", nor any of the names of the 
 *    project contributors may be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact visigoths@visigoths.org.
 *
 * 5. Products derived from this software may not be called "FreeMarker" or "Visigoth"
 *    nor may "FreeMarker" or "Visigoth" appear in their names
 *    without prior written permission of the Visigoth Software Society.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE VISIGOTH SOFTWARE SOCIETY OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Visigoth Software Society. For more
 * information on the Visigoth Software Society, please see
 * http://www.visigoths.org/
 */
package freemarker.ext.beans;

import java.lang.reflect.Member;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * @author Attila Szegedi
 * @version $Id: $
 */
class OverloadedFixArgMethod<T extends Member> extends OverloadedMethod<T>
{
    void onAddSignature(T member, Class<?>[] argTypes) {
    };
    
    void updateSignature(int l) {
    };
    
    void afterSignatureAdded(int l) {
    };

    Object getMemberAndArguments(List<TemplateModel> arguments, BeansWrapper w) 
    throws TemplateModelException {
        if(arguments == null) {
            // null is treated as empty args
            arguments = Collections.emptyList();
        }
        int l = arguments.size();
	Class<?>[][] marshalTypes = getMarshalTypes();
        if(marshalTypes.length <= l) {
            return NO_SUCH_METHOD;
        }
        Class<?>[] types = marshalTypes[l];
        if(types == null) {
            return NO_SUCH_METHOD;
        }
        assert types.length == l;
        // Marshal the arguments
        Object[] args = new Object[l];
        Iterator<TemplateModel> it = arguments.iterator();
        for(int i = 0; i < l; ++i) {
            Object obj = w.unwrap(it.next(), types[i]);
            if(obj == BeansWrapper.CAN_NOT_UNWRAP) {
                return NO_SUCH_METHOD;
            }
            args[i] = obj;
        }
        
        Object objMember = getMemberForArgs(args, false);
        if(objMember instanceof Member) {
            T member = (T)objMember;
            BeansWrapper.coerceBigDecimals(getSignature(member), args);
            return new MemberAndArguments<T>(member, args);
        }
        return objMember; // either NO_SUCH_METHOD or AMBIGUOUS_METHOD
    }
}
