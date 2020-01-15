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

package freemarker.ext.jython;

import java.util.Iterator;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;

import freemarker.ext.util.ModelFactory;
import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Generic model for arbitrary Jython objects.
 * @version $Id: JythonModel.java,v 1.14 2005/06/12 19:03:06 szegedia Exp $
 * @author Attila Szegedi
 */
public class JythonModel
implements TemplateBooleanModel, TemplateScalarModel, TemplateHashModel, 
TemplateMethodModelEx, AdapterTemplateModel, WrapperTemplateModel
{
    protected final PyObject object;
    protected final JythonWrapper wrapper;
    
    static final ModelFactory FACTORY =
        new ModelFactory()
        {
            public TemplateModel create(Object object, ObjectWrapper wrapper)
            {
                return new JythonModel((PyObject)object, (JythonWrapper)wrapper);
            }
        };
        
    public JythonModel(PyObject object, JythonWrapper wrapper)
    {
        this.object = object;
        this.wrapper = wrapper;
    }

    /**
     * Returns the value of {@link PyObject#__nonzero__()}.
     */
    public boolean getAsBoolean() throws TemplateModelException
    {
        try
        {
            return object.__nonzero__();
        }
        catch(PyException e)
        {
            throw new TemplateModelException(e);
        }
    }

    /**
     * Returns the value of {@link Object#toString()}.
     */
    public String getAsString() throws TemplateModelException
    {
        try
        {
            return object.toString();
        }
        catch(PyException e)
        {
            throw new TemplateModelException(e);
        }
    }

    /**
     * Calls {@link PyObject#__findattr__(java.lang.String)}, then if it
     * returns null calls {@link PyObject#__finditem__(java.lang.String)}.
     * If {@link JythonWrapper#setAttributesShadowItems(boolean)} was called
     * with <code>false</code>, the order of calls is reversed (that is, item
     * lookup takes precedence over attribute lookup).
     */
    public TemplateModel get(String key)
    throws
        TemplateModelException
    {
        if(key != null)
        {
            key = key.intern();
        }
        
        PyObject obj = null;
        
        try
        {
            if(wrapper.isAttributesShadowItems())
            {
                obj = object.__findattr__(key);
                if(obj == null)
                {
                    obj = object.__finditem__(key);
                }
            }
            else
            {
                obj = object.__finditem__(key);
                if(obj == null)
                {
                    obj = object.__findattr__(key);
                }
            }
        }
        catch(PyException e)
        {
            throw new TemplateModelException(e);
        }

        return wrapper.wrap(obj);
    }
    
    /**
     * Returns {@link PyObject#__len__()}<code> == 0</code>.
     */
    public boolean isEmpty() throws TemplateModelException
    {
        try
        {
            return object.__len__() == 0;
        }
        catch(PyException e)
        {
            throw new TemplateModelException(e);
        }
    }

    /**
     * @see freemarker.template.TemplateMethodModel#exec(List)
     */
    public Object exec(List arguments) throws TemplateModelException
    {
        int size = arguments.size();
        try
        {
            switch(size)
            {
                case 0:
                {
                    return wrapper.wrap(object.__call__());
                }
                case 1:
                {
                    return wrapper.wrap(object.__call__(wrapper.unwrap(
                        (TemplateModel)arguments.get(0))));
                }
                default:
                {
                    PyObject[] pyargs = new PyObject[size];
                    int i = 0;
                    for (Iterator arg = arguments.iterator(); arg.hasNext();)
                    {
                        pyargs[i++] = wrapper.unwrap(
                            (TemplateModel) arg.next());
                    }
                    return wrapper.wrap(object.__call__(pyargs));
                }
            }
        }
        catch(PyException e)
        {
            throw new TemplateModelException(e);
        }
    }

    public Object getAdaptedObject(Class hint) {
        if(object == null) {
            return null;
        }
        Object view = object.__tojava__(hint);
        if(view == Py.NoConversion) {
            view = object.__tojava__(Object.class);
        }
        return view;
    }
    
    public Object getWrappedObject() {
        return object == null ? null : object.__tojava__(Object.class);
    }
}
