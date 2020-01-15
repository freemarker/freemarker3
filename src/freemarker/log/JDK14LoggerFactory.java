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

package freemarker.log;

import java.util.logging.Level;

/**
 * @version $Id: JDK14LoggerFactory.java,v 1.8 2003/01/27 20:36:44 szegedia Exp $
 * @author Attila Szegedi
 */
class JDK14LoggerFactory implements LoggerFactory
{
    public Logger getLogger(String category)
    {
        return new JDK14Logger(java.util.logging.Logger.getLogger(category));
    }

    private static class JDK14Logger
    extends
        Logger
    {
        private final java.util.logging.Logger logger;
        
        JDK14Logger(java.util.logging.Logger logger)
        {
            this.logger = logger;
        }
        
        public void debug(String message)
        {
            logger.log(Level.FINE, message);
        }

        public void debug(String message, Throwable t)
        {
            logger.log(Level.FINE, message, t);
        }

        public void error(String message)
        {
            logger.log(Level.SEVERE, message);
        }

        public void error(String message, Throwable t)
        {
            logger.log(Level.SEVERE, message, t);
        }

        public void info(String message)
        {
            logger.log(Level.INFO, message);
        }

        public void info(String message, Throwable t)
        {
            logger.log(Level.INFO, message, t);
        }

        public void warn(String message)
        {
            logger.log(Level.WARNING, message);
        }

        public void warn(String message, Throwable t)
        {
            logger.log(Level.WARNING, message, t);
        }

        public boolean isDebugEnabled()
        {
            return logger.isLoggable(Level.FINE);
        }

        public boolean isInfoEnabled()
        {
            return logger.isLoggable(Level.INFO);
        }

        public boolean isWarnEnabled()
        {
            return logger.isLoggable(Level.WARNING);
        }

        public boolean isErrorEnabled()
        {
            return logger.isLoggable(Level.SEVERE);
        }

        public boolean isFatalEnabled()
        {
            return logger.isLoggable(Level.SEVERE);
        }
    }
}
