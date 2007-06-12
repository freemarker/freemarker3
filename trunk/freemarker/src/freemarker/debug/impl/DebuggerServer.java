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

package freemarker.debug.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import freemarker.debug.Debugger;
import freemarker.log.Logger;
import freemarker.template.utility.SecurityUtilities;
import freemarker.template.utility.UndeclaredThrowableException;

/**
 * @author Attila Szegedi
 * @version $Id: DebuggerServer.java,v 1.3 2004/09/09 15:34:38 szegedia Exp $
 */
class DebuggerServer
{
    private static final Logger logger = Logger.getLogger("freemarker.debug.server");
    // TODO: Eventually replace with Yarrow    
    private static final Random R = new SecureRandom();
    
    private final byte[] password;
    private final int port;
    private final Serializable debuggerStub;
    
    public DebuggerServer(Serializable debuggerStub)
    {
        port = SecurityUtilities.getSystemProperty("freemarker.debug.port", Debugger.DEFAULT_PORT).intValue();
        try
        {
            password = SecurityUtilities.getSystemProperty("freemarker.debug.password", "").getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new UndeclaredThrowableException(e);
        }
        this.debuggerStub = debuggerStub;
    }
    
    public void start()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                startInternal();
            }
        }, "FreeMarker Debugger Server Acceptor").start();
    }
    
    private void startInternal()
    {
        try
        {
            ServerSocket ss = new ServerSocket(port);
            for(;;)
            {
                Socket s = ss.accept();
                new Thread(new DebuggerAuthProtocol(s)).start();
            }
        }
        catch(IOException e)
        {
            logger.error("Debugger server shut down.", e);
        }
    }
    
    private class DebuggerAuthProtocol implements Runnable
    {
        private final Socket s;
        
        DebuggerAuthProtocol(Socket s)
        {
            this.s = s;
        }
        
        public void run()
        {
            try
            {
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                byte[] challenge = new byte[512];
                R.nextBytes(challenge);
                out.writeInt(220); // protocol version
                out.writeObject(challenge);
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(password);
                md.update(challenge);
                byte[] response = (byte[])in.readObject();
                if(Arrays.equals(response, md.digest()))
                {
                    out.writeObject(debuggerStub);
                }
                else
                {
                    out.writeObject(null);
                }
            }
            catch(Exception e)
            {
                logger.warn("Connection to " + s.getInetAddress().getHostAddress() + " abruply broke", e);
            }
        }

    }
}
