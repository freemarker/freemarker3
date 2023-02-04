package freemarker.debug.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import freemarker.cache.CacheStorage;
import freemarker.cache.SoftCacheStorage;
import freemarker.debug.DebuggedEnvironment;
import freemarker.ext.util.IdentityHashMap;
import freemarker.template.Configuration;
import freemarker.core.Configurable;
import freemarker.core.Environment;
import freemarker.template.SimpleCollection;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.UndeclaredThrowableException;

/**
 * @author Attila Szegedi
 * @version $Id: RmiDebuggedEnvironmentImpl.java,v 1.3 2004/01/06 17:06:42 szegedia Exp $
 */
class RmiDebuggedEnvironmentImpl
extends
    RmiDebugModelImpl
implements
    DebuggedEnvironment
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -7553276041458835559L;
	private static final CacheStorage storage = new SoftCacheStorage(new IdentityHashMap());
    private static final Object idLock = new Object();
    private static long nextId = 1;
    
    private boolean stopped = false;
    private final long id;
    
    private RmiDebuggedEnvironmentImpl(Environment env) throws RemoteException
    {
        super(new DebugEnvironmentModel(env));
        synchronized(idLock)
        {
            id = nextId++;
        }
    }

    static synchronized Object getCachedWrapperFor(Object key)
    throws
        RemoteException
    {
        Object value = storage.get(key);
        if(value == null)
        {
            if(key instanceof Environment)
            {
                value = new RmiDebuggedEnvironmentImpl((Environment)key); 
            }
            else if(key instanceof TemplateModel)
            {
                value = new RmiDebugModelImpl((TemplateModel)key);
            }
            else if(key instanceof Template)
            {
                value = new DebugTemplateModel((Template)key);
            }
            else if(key instanceof Configuration)
            {
                value = new DebugConfigurationModel((Configuration)key);
            }
        }
        return value;
    }

    public void resume()
    {
        synchronized(this)
        {
            notify();
        }
    }

    public void stop()
    {
        stopped = true;
        resume();
    }

    public long getId()
    {
        return id;
    }
    
    boolean isStopped()
    {
        return stopped;
    }
    
    private abstract static class DebugMapModel implements TemplateHashModelEx
    {
        public int size()
        {
            return keySet().size();
        }

        public TemplateCollectionModel keys()
        {
            return new SimpleCollection(keySet());
        }

        public TemplateCollectionModel values() throws TemplateModelException
        {
            Collection keys = keySet();
            List list = new ArrayList(keys.size());
            
            for(Iterator it = keys.iterator(); it.hasNext();)
            {
                list.add(get((String)it.next()));
            }
            return new SimpleCollection(list);
        }

        public boolean isEmpty()
        {
            return size() == 0;
        }
        
        abstract Collection keySet();

        static List composeList(Collection c1, Collection c2)
        {
            List list = new ArrayList(c1);
            list.addAll(c2);
            Collections.sort(list);
            return list;
        }
    }
    
    private static class DebugConfigurableModel extends DebugMapModel
    {
        static final List KEYS = Arrays.asList(new String[]
        {
            Configurable.ARITHMETIC_ENGINE_KEY,
            Configurable.BOOLEAN_FORMAT_KEY,
            Configurable.LOCALE_KEY,
            Configurable.NUMBER_FORMAT_KEY,
            Configurable.OBJECT_WRAPPER_KEY,
            Configurable.TEMPLATE_EXCEPTION_HANDLER_KEY
        });

        final Configurable configurable;
        
        DebugConfigurableModel(Configurable configurable)
        {
            this.configurable = configurable;
        }
        
        Collection keySet()
        {
            return KEYS;
        }
        
        public TemplateModel get(String key) throws TemplateModelException
        {
            String s = configurable.getSetting(key);
            return s == null ? null : new SimpleScalar(s);
        }

    }
    
    private static class DebugConfigurationModel extends DebugConfigurableModel
    {
        private static final List KEYS = composeList(DebugConfigurableModel.KEYS, Collections.singleton("sharedVariables"));

        private TemplateModel sharedVariables = new DebugMapModel()
        {
            Collection keySet()
            {
                return ((Configuration)configurable).getSharedVariableNames();
            }
        
            public TemplateModel get(String key)
            {
                return ((Configuration)configurable).getSharedVariable(key);
            }
        };
        
        DebugConfigurationModel(Configuration config)
        {
            super(config);
        }
        
        Collection keySet()
        {
            return KEYS;
        }

        public TemplateModel get(String key) throws TemplateModelException
        {
            if("sharedVariables".equals(key))
            {
                return sharedVariables; 
            }
            else
            {
                return super.get(key);
            }
        }
    }
    
    private static class DebugTemplateModel extends DebugConfigurableModel
    {
        private static final List KEYS = composeList(DebugConfigurableModel.KEYS, 
            Arrays.asList(new String[] {
                "configuration", 
                "name",
                }));
    
        private final SimpleScalar name;

        DebugTemplateModel(Template template)
        {
            super(template);
            this.name = new SimpleScalar(template.getName());
        }

        Collection keySet()
        {
            return KEYS;
        }

        public TemplateModel get(String key) throws TemplateModelException
        {
            if("configuration".equals(key))
            {
                try
                {
                    return (TemplateModel)getCachedWrapperFor(((Template)configurable).getConfiguration());
                }
                catch (RemoteException e)
                {
                    throw new TemplateModelException(e);
                }
            }
            if("name".equals(key))
            {
                return name;
            }
            return super.get(key);
        }
    }

    private static class DebugEnvironmentModel extends DebugConfigurableModel
    {
        private static final List KEYS = composeList(DebugConfigurableModel.KEYS, 
            Arrays.asList(new String[] {
                "currentNamespace",
                "dataModel",
                "globalNamespace",
                "knownVariables",
                "mainNamespace",
                "template",
                 }));
    
        private TemplateModel knownVariables = new DebugMapModel()
        {
            Collection keySet()
            {
                try
                {
                    return ((Environment)configurable).getKnownVariableNames();
                }
                catch (TemplateModelException e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        
            public TemplateModel get(String key) throws TemplateModelException
            {
                return ((Environment)configurable).getVariable(key);
            }
        };
         
        DebugEnvironmentModel(Environment env)
        {
            super(env);
        }

        Collection keySet()
        {
            return KEYS;
        }

        public TemplateModel get(String key) throws TemplateModelException
        {
            if("currentNamespace".equals(key))
            {
                return ((Environment)configurable).getCurrentNamespace();
            }
            if("dataModel".equals(key))
            {
                return ((Environment)configurable).getDataModel();
            }
            if("globalNamespace".equals(key))
            {
                return ((Environment)configurable).getGlobalNamespace();
            }
            if("knownVariables".equals(key))
            {
                return knownVariables;
            }
            if("mainNamespace".equals(key))
            {
                return ((Environment)configurable).getMainNamespace();
            }
            if("template".equals(key))
            {
                try
                {
                    return (TemplateModel) getCachedWrapperFor(((Environment)configurable).getTemplate());
                }
                catch (RemoteException e)
                {
                    throw new TemplateModelException(e);
                }
            }
            return super.get(key);
        }
    }
}
