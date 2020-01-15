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

package freemarker.debug;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;

import freemarker.template.utility.UndeclaredThrowableException;

/**
 * A utility class that allows you to connect to the FreeMarker debugger service
 * running on a specific host and port. 
 * @author Attila Szegedi
 * @version $Id: DebuggerClient.java,v 1.3 2003/06/08 00:58:16 herbyderby Exp $
 */
public class DebuggerClient
{
    private DebuggerClient()
    {
    }
    
    /**
     * Connects to the FreeMarker debugger service running on a specific host
     * and port. The Java VM to which the connection is made must have defined
     * the system property <tt>freemarker.debug.password</tt> in order to enable
     * the debugger service. Additionally, the <tt>freemarker.debug.port</tt>
     * system property can be set to specify the port where the debugger service
     * is listening. When not specified, it defaults to 
     * {@link Debugger#DEFAULT_PORT}.
     * @param host the host address of the machine where the debugger service is
     * running.
     * @param port the port of the debugger service
     * @param password the password required to connect to the debugger service
     * @return Debugger a debugger object. null is returned in case incorrect
     * password was supplied.
     * @throws IOException if an exception occurs.
     */
    public static Debugger getDebugger(InetAddress host, int port, String password)
    throws
        IOException
    {
        try
        {
            Socket s = new Socket(host, port);
            try
            {
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                int protocolVersion = in.readInt();
                if(protocolVersion > 220)
                {
                    throw new IOException(
                        "Incompatible protocol version " + protocolVersion + 
                        ". At most 220 was expected.");
                }
                byte[] challenge = (byte[])in.readObject();
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(password.getBytes("UTF-8"));
                md.update(challenge);
                out.writeObject(md.digest());
                return (Debugger)in.readObject();
            }
            finally
            {
                s.close();
            }
        }
        catch(IOException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new UndeclaredThrowableException(e); 
        }
    }
}
