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

import org.slf4j.spi.LocationAwareLogger;

public class SLF4JLoggerFactory implements LoggerFactory {
	
	public Logger getLogger(String category) {
		org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(category);
		if (slf4jLogger instanceof LocationAwareLogger) {
			return new LocationAwareSLF4JLogger((LocationAwareLogger) slf4jLogger);
		} else {
			return new LocationUnawareSLF4JLogger(slf4jLogger);
		}
	}

	/**
	 * Logger where the log entry issuer (class, method) will be correctly
	 * shown to be the caller of <tt>LocationAwareSLF4JLogger</tt> methods.
	 */
	private static final class LocationAwareSLF4JLogger extends Logger {
		
		private static final String ADAPTER_FQCN
				= LocationAwareSLF4JLogger.class.getName();
		
		private final LocationAwareLogger logger;
		
		LocationAwareSLF4JLogger(LocationAwareLogger logger) {
			this.logger = logger;
		}

		public void debug(String message) {
			debug(message, null);
		}

		public void debug(String message, Throwable t) {
			logger.log(null, ADAPTER_FQCN,
					LocationAwareLogger.DEBUG_INT, message, null, t);
		}

		public void info(String message) {
			info(message, null);
		}

		public void info(String message, Throwable t) {
			logger.log(null, ADAPTER_FQCN,
					LocationAwareLogger.INFO_INT, message, null, t);
		}

		public void warn(String message) {
			warn(message, null);
		}

		public void warn(String message, Throwable t) {
			logger.log(null, ADAPTER_FQCN,
					LocationAwareLogger.WARN_INT, message, null, t);
		}

		public void error(String message) {
			error(message, null);
		}

		public void error(String message, Throwable t) {
			logger.log(null, ADAPTER_FQCN,
					LocationAwareLogger.ERROR_INT, message, null, t);
		}

		public boolean isDebugEnabled() {
			return logger.isDebugEnabled();
		}

		public boolean isInfoEnabled() {
			return logger.isInfoEnabled();
		}

		public boolean isWarnEnabled() {
			return logger.isWarnEnabled();
		}

		public boolean isErrorEnabled() {
			return logger.isErrorEnabled();
		}

		public boolean isFatalEnabled() {
			return logger.isErrorEnabled();
		}
		
	}
	
	/**
	 * Logger where the log entry issuer (class, method) will be incorrectly
	 * shown to be a method of this class.
	 */
	private static class LocationUnawareSLF4JLogger extends Logger {
		
		private final org.slf4j.Logger logger;

		LocationUnawareSLF4JLogger(org.slf4j.Logger logger) {
			this.logger = logger;
		}

		public void debug(String message) {
			logger.debug(message);
		}

		public void debug(String message, Throwable t) {
			logger.debug(message, t);
		}

		public void info(String message) {
			logger.info(message);
		}

		public void info(String message, Throwable t) {
			logger.info(message, t);
		}

		public void warn(String message) {
			logger.warn(message);
		}

		public void warn(String message, Throwable t) {
			logger.warn(message, t);
		}

		public void error(String message) {
			logger.error(message);
		}

		public void error(String message, Throwable t) {
			logger.error(message, t);
		}

		public boolean isDebugEnabled() {
			return logger.isDebugEnabled();
		}

		public boolean isInfoEnabled() {
			return logger.isInfoEnabled();
		}

		public boolean isWarnEnabled() {
			return logger.isWarnEnabled();
		}

		public boolean isErrorEnabled() {
			return logger.isErrorEnabled();
		}

		public boolean isFatalEnabled() {
			return logger.isErrorEnabled();
		}
		
	}

}
