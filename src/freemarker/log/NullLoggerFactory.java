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

/**
 * @version $Id: NullLoggerFactory.java,v 1.9 2003/01/27 20:36:45 szegedia Exp $
 * @author Attila Szegedi
 */
class NullLoggerFactory implements LoggerFactory
{
    NullLoggerFactory()
    {
    }
    
    public Logger getLogger(String category)
    {
        return INSTANCE;
    }

    private static final Logger INSTANCE = new Logger()
    {
        public void debug(String message)
        {
        }

        public void debug(String message, Throwable t)
        {
        }

        public void error(String message)
        {
        }

        public void error(String message, Throwable t)
        {
        }

        public void info(String message)
        {
        }

        public void info(String message, Throwable t)
        {
        }

        public void warn(String message)
        {
        }

        public void warn(String message, Throwable t)
        {
        }

        public boolean isDebugEnabled()
        {
            return false;
        }

        public boolean isInfoEnabled()
        {
            return false;
        }

        public boolean isWarnEnabled()
        {
            return false;
        }

        public boolean isErrorEnabled()
        {
            return false;
        }

        public boolean isFatalEnabled()
        {
            return false;
        }
    };
}
