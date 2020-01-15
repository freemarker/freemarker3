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

package freemarker.debug.impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import freemarker.debug.DebugModel;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.TemplateSequenceModel;

/**
 * @author Attila Szegedi
 * @version $Id: RmiDebugModelImpl.java,v 1.2 2004/01/06 17:06:42 szegedia Exp $
 */
class RmiDebugModelImpl extends UnicastRemoteObject implements DebugModel
{
    private final TemplateModel model;
    private final int type;
    
    RmiDebugModelImpl(TemplateModel model) throws RemoteException
    {
        super();
        this.model = model;
        type = calculateType(model);
    }

    private static DebugModel getDebugModel(TemplateModel tm) throws RemoteException
    {
        return (DebugModel)RmiDebuggedEnvironmentImpl.getCachedWrapperFor(tm);
    }
    public String getAsString() throws TemplateModelException
    {
        return ((TemplateScalarModel)model).getAsString();
    }

    public Number getAsNumber() throws TemplateModelException
    {
        return ((TemplateNumberModel)model).getAsNumber();
    }

    public Date getAsDate() throws TemplateModelException
    {
        return ((TemplateDateModel)model).getAsDate();
    }

    public int getDateType()
    {
        return ((TemplateDateModel)model).getDateType();
    }

    public boolean getAsBoolean() throws TemplateModelException
    {
        return ((TemplateBooleanModel)model).getAsBoolean();
    }

    public int size() throws TemplateModelException
    {
        if(model instanceof TemplateSequenceModel)
        {
            return ((TemplateSequenceModel)model).size();
        }
        return ((TemplateHashModelEx)model).size();
    }

    public DebugModel get(int index) throws TemplateModelException, RemoteException
    {
        return getDebugModel(((TemplateSequenceModel)model).get(index));
    }
    
    public DebugModel[] get(int fromIndex, int toIndex) throws TemplateModelException, RemoteException
    {
        DebugModel[] dm = new DebugModel[toIndex - fromIndex];
        TemplateSequenceModel s = (TemplateSequenceModel)model;
        for (int i = fromIndex; i < toIndex; i++)
        {
            dm[i - fromIndex] = getDebugModel(s.get(i));
        }
        return dm;
    }

    public DebugModel[] getCollection() throws TemplateModelException, RemoteException
    {
        List list = new ArrayList();
        TemplateModelIterator i = ((TemplateCollectionModel)model).iterator();
        while(i.hasNext())
        {
            list.add(getDebugModel(i.next()));
        }
        return (DebugModel[])list.toArray(new DebugModel[list.size()]);
    }
    
    public DebugModel get(String key) throws TemplateModelException, RemoteException
    {
        return getDebugModel(((TemplateHashModel)model).get(key));
    }
    
    public DebugModel[] get(String[] keys) throws TemplateModelException, RemoteException
    {
        DebugModel[] dm = new DebugModel[keys.length];
        TemplateHashModel h = (TemplateHashModel)model;
        for (int i = 0; i < keys.length; i++)
        {
            dm[i] = getDebugModel(h.get(keys[i]));
        }
        return dm;
    }

    public String[] keys() throws TemplateModelException
    {
        TemplateHashModelEx h = (TemplateHashModelEx)model;
        List list = new ArrayList();
        TemplateModelIterator i = h.keys().iterator();
        while(i.hasNext())
        {
            list.add(((TemplateScalarModel)i.next()).getAsString());
        }
        return (String[])list.toArray(new String[list.size()]);
    }

    public int getModelTypes()
    {
        return type;
    }
    
    private static int calculateType(TemplateModel model)
    {
        int type = 0;
        if(model instanceof TemplateScalarModel) type += TYPE_SCALAR;
        if(model instanceof TemplateNumberModel) type += TYPE_NUMBER;
        if(model instanceof TemplateDateModel) type += TYPE_DATE;
        if(model instanceof TemplateBooleanModel) type += TYPE_BOOLEAN;
        if(model instanceof TemplateSequenceModel) type += TYPE_SEQUENCE;
        if(model instanceof TemplateCollectionModel) type += TYPE_COLLECTION;
        if(model instanceof TemplateHashModelEx) type += TYPE_HASH_EX;
        else if(model instanceof TemplateHashModel) type += TYPE_HASH;
        if(model instanceof TemplateMethodModelEx) type += TYPE_METHOD_EX;
        else if(model instanceof TemplateMethodModel) type += TYPE_METHOD;
        return type;
    }
}
