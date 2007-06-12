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
