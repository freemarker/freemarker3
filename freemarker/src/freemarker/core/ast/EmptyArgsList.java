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

import java.util.*;
import freemarker.core.Environment;
import freemarker.template.*;

/**
 * The abstract base class of both {@link NamedArgsList} and {@link PositionalArgsList}
 * @author Attila Szegedi
 */

public class EmptyArgsList extends ArgsList {

    @Override
    void addOOParamArg(OOParamElement param) {
        throw new AssertionError();
    }

    @Override
    ArgsList deepClone(String name, Expression subst) {
        return this;
    }

    @Override
    Map<String, TemplateModel> getParameterMap(TemplateModel tm,
            Environment env) throws TemplateException
    {
        ParameterList annotatedParameterList = ArgsList.getParameterList(tm);
        if (annotatedParameterList == null) {
            return new HashMap<String, TemplateModel>();
        }
        else {
            return annotatedParameterList.getParameterMapForEmptyArgs(env);
        }
    }

    @Override
    List getParameterSequence(TemplateModel target, Environment env)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    int size()
    {
        return 0;
    }
}