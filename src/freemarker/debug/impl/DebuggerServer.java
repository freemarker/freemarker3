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
