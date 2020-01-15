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

import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.core.ast.DebugBreak;
import freemarker.core.ast.TemplateElement;
import freemarker.debug.Breakpoint;
import freemarker.debug.DebuggerListener;
import freemarker.debug.EnvironmentSuspendedEvent;
import freemarker.template.Template;
import freemarker.template.utility.UndeclaredThrowableException;

/**
 * @author Attila Szegedi
 * @version $Id
 */
class RmiDebuggerService
extends
    DebuggerService
{
    private final Map templateDebugInfos = new HashMap();
    private final HashSet suspendedEnvironments = new HashSet();
    private final Map listeners = new HashMap();
    private final ReferenceQueue refQueue = new ReferenceQueue();
     
    RmiDebuggerService()
    {
        try
        {
            new DebuggerServer((Serializable)RemoteObject.toStub(new RmiDebuggerImpl(this))).start();
        }
        catch(RemoteException e)
        {
            throw new UndeclaredThrowableException(e);
        }
    }
    
    List getBreakpointsSpi(String templateName)
    {
        synchronized(templateDebugInfos)
        {
            TemplateDebugInfo tdi = findTemplateDebugInfo(templateName);
            return tdi == null ? Collections.EMPTY_LIST : tdi.breakpoints;
        }
    }

    List getBreakpointsSpi()
    {
        List sumlist = new ArrayList();
        synchronized(templateDebugInfos)
        {
            for (Iterator iter = templateDebugInfos.values().iterator(); iter.hasNext();)
            {
                sumlist.addAll(((TemplateDebugInfo) iter.next()).breakpoints);
            }
        }
        Collections.sort(sumlist);
        return sumlist;
    }

    boolean suspendEnvironmentSpi(Environment env, int line)
    throws
        RemoteException
    {
        RmiDebuggedEnvironmentImpl denv = 
            (RmiDebuggedEnvironmentImpl)
                RmiDebuggedEnvironmentImpl.getCachedWrapperFor(env);
                
        synchronized(suspendedEnvironments)
        {
            suspendedEnvironments.add(denv);
        }
        try
        {
            EnvironmentSuspendedEvent breakpointEvent = 
                new EnvironmentSuspendedEvent(this, line, denv);
    
            synchronized(listeners)
            {
                for (Iterator iter = listeners.values().iterator(); iter.hasNext();)
                {
                    DebuggerListener listener = (DebuggerListener) iter.next();
                    listener.environmentSuspended(breakpointEvent);
                }
            }
            synchronized(denv)
            {
                try
                {
                    denv.wait();
                }
                catch(InterruptedException e)
                {
                    ;// Intentionally ignored
                }
            }
            return denv.isStopped();
        }
        finally
        {
            synchronized(suspendedEnvironments)
            {
                suspendedEnvironments.remove(denv);
            }
        }
    }
    
    void registerTemplateSpi(Template template)
    {
        String templateName = template.getName();
        synchronized(templateDebugInfos)
        {
            TemplateDebugInfo tdi = createTemplateDebugInfo(templateName);
            tdi.templates.add(new TemplateReference(templateName, template, refQueue));
            // Inject already defined breakpoints into the template
            for (Iterator iter = tdi.breakpoints.iterator(); iter.hasNext();)
            {
                Breakpoint breakpoint = (Breakpoint) iter.next();
                insertDebugBreak(template, breakpoint);
            }
        }
    }
    
    Collection getSuspendedEnvironments()
    {
        return (Collection)suspendedEnvironments.clone();
    }

    Object addDebuggerListener(DebuggerListener listener)
    {
        Object id; 
        synchronized(listeners)
        {
            id = Long.valueOf(System.currentTimeMillis());
            listeners.put(id, listener);
        }
        return id;
    }
    
    void removeDebuggerListener(Object id)
    {
        synchronized(listeners)
        {
            listeners.remove(id);
        }
    }

    void addBreakpoint(Breakpoint breakpoint)
    {
        String templateName = breakpoint.getTemplateName();
        synchronized(templateDebugInfos)
        {
            TemplateDebugInfo tdi = createTemplateDebugInfo(templateName);
            List breakpoints = tdi.breakpoints;
            int pos = Collections.binarySearch(breakpoints, breakpoint);
            if(pos < 0)
            {
                // Add to the list of breakpoints
                breakpoints.add(-pos - 1, breakpoint);
                // Inject the breakpoint into all templates with this name
                for (Iterator iter = tdi.templates.iterator(); iter.hasNext();)
                {
                    TemplateReference ref = (TemplateReference) iter.next();
                    Template t = ref.getTemplate();
                    if(t == null)
                    {
                        iter.remove();
                    }
                    else
                    {
                        insertDebugBreak(t, breakpoint);
                    }
                }
            }
        }
    }

    private static void insertDebugBreak(Template t, Breakpoint breakpoint)
    {
        TemplateElement te = findTemplateElement(t.getRootTreeNode(), breakpoint.getLine());
        if(te == null)
        {
            return;
        }
        TemplateElement parent = te.getParent();
        DebugBreak db = new DebugBreak(te);
        // TODO: Ensure there always is a parent by making sure
        // that the root element in the template is always a MixedContent
        // Also make sure it doesn't conflict with anyone's code.
        parent.setChildAt(parent.getIndex(te), db);
    }

    private static TemplateElement findTemplateElement(TemplateElement te, int line)
    {
        if(te.getBeginLine() > line || te.getEndLine() < line)
        {
            return null;
        }
        // Find the narrowest match
        for(Enumeration children = te.children(); children.hasMoreElements();)
        {
            TemplateElement child = (TemplateElement)children.nextElement();
            TemplateElement childmatch = findTemplateElement(child, line);
            if(childmatch != null)
            {
                return childmatch;
            }
        }
        // If no child provides narrower match, return this
        return te;
    }
    
    private TemplateDebugInfo findTemplateDebugInfo(String templateName)
    {
        processRefQueue();
        return (TemplateDebugInfo)templateDebugInfos.get(templateName); 
    }
    
    private TemplateDebugInfo createTemplateDebugInfo(String templateName)
    {
        TemplateDebugInfo tdi = findTemplateDebugInfo(templateName);
        if(tdi == null)
        {
            tdi = new TemplateDebugInfo();
            templateDebugInfos.put(templateName, tdi);
        }
        return tdi;
    }
    
    void removeBreakpoint(Breakpoint breakpoint)
    {
        String templateName = breakpoint.getTemplateName();
        synchronized(templateDebugInfos)
        {
            TemplateDebugInfo tdi = findTemplateDebugInfo(templateName);
            if(tdi != null)
            {
                List breakpoints = tdi.breakpoints;
                int pos = Collections.binarySearch(breakpoints, breakpoint);
                if(pos >= 0)
                { 
                    breakpoints.remove(pos);
                    for (Iterator iter = tdi.templates.iterator(); iter.hasNext();)
                    {
                        TemplateReference ref = (TemplateReference) iter.next();
                        Template t = ref.getTemplate();
                        if(t == null)
                        {
                            iter.remove();
                        }
                        else
                        {
                            removeDebugBreak(t, breakpoint);
                        }
                    }
                }
                if(tdi.isEmpty())
                {
                    templateDebugInfos.remove(templateName);
                }
            }
        }
    }

    private void removeDebugBreak(Template t, Breakpoint breakpoint)
    {
        TemplateElement te = findTemplateElement(t.getRootTreeNode(), breakpoint.getLine());
        if(te == null)
        {
            return;
        }
        DebugBreak db = null;
        while(te != null)
        {
            if(te instanceof DebugBreak)
            {
                db = (DebugBreak)te;
                break;
            }
            te = te.getParent();
        }
        if(db == null)
        {
            return;
        }
        TemplateElement parent = db.getParent(); 
        parent.setChildAt(parent.getIndex(db), db.getChildAt(0));
    }
    
    void removeBreakpoints(String templateName)
    {
        synchronized(templateDebugInfos)
        {
            TemplateDebugInfo tdi = findTemplateDebugInfo(templateName);
            if(tdi != null)
            {
                removeBreakpoints(tdi);
                if(tdi.isEmpty())
                {
                    templateDebugInfos.remove(templateName);
                }
            }
        }
    }

    void removeBreakpoints()
    {
        synchronized(templateDebugInfos)
        {
            for (Iterator iter = templateDebugInfos.values().iterator(); iter.hasNext();)
            {
                TemplateDebugInfo tdi = (TemplateDebugInfo) iter.next(); 
                removeBreakpoints(tdi);
                if(tdi.isEmpty())
                {
                    iter.remove();
                }
            }
        }
    }

    private void removeBreakpoints(TemplateDebugInfo tdi)
    {
        tdi.breakpoints.clear();
        for (Iterator iter = tdi.templates.iterator(); iter.hasNext();)
        {
            TemplateReference ref = (TemplateReference) iter.next();
            Template t = ref.getTemplate();
            if(t == null)
            {
                iter.remove();
            }
            else
            {
                removeDebugBreaks(t.getRootTreeNode());
            }
        }
    }
    
    private void removeDebugBreaks(TemplateElement te)
    {
        int count = te.getChildCount();
        for(int i = 0; i < count; ++i)
        {
            TemplateElement child = te.getChildAt(i);
            while(child instanceof DebugBreak)
            {
                TemplateElement dbchild = child.getChildAt(0); 
                te.setChildAt(i, dbchild);
                child = dbchild;
            }
            removeDebugBreaks(child);
        }
    }
    
    private static final class TemplateDebugInfo
    {
        final List templates = new ArrayList();
        final List breakpoints = new ArrayList();
        
        boolean isEmpty()
        {
            return templates.isEmpty() && breakpoints.isEmpty();
        }
    }
    
    private static final class TemplateReference extends WeakReference
    {
        final String templateName;
         
        TemplateReference(String templateName, Template template, ReferenceQueue queue)
        {
            super(template, queue);
            this.templateName = templateName;
        }
        
        Template getTemplate()
        {
            return (Template)get();
        }
    }
    
    private void processRefQueue()
    {
        for(;;)
        {
            TemplateReference ref = (TemplateReference)refQueue.poll();
            if(ref == null)
            {
                break;
            }
            TemplateDebugInfo tdi = findTemplateDebugInfo(ref.templateName);
            if(tdi != null)
            {
                tdi.templates.remove(ref);
                if(tdi.isEmpty())
                {
                    templateDebugInfos.remove(ref.templateName);
                }
            }
        }
    }
}
