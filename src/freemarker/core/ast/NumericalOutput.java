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

package freemarker.core.ast;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import freemarker.core.Environment;
import freemarker.template.*;

/**
 * An instruction that outputs the value of a numerical expression.
 * @author <a href="mailto:jon@revusky.com">Jonathan Revusky</a>
 */
public class NumericalOutput extends TemplateElement {

	private Expression expression;
	private int minFracDigits;
	private int maxFracDigits;
	private String formatString;
	private volatile FormatHolder formatCache; // creating new NumberFormat is slow operation

	public NumericalOutput(Expression expression) {
		this.expression = expression;
	}

	public NumericalOutput(Expression expression, String format) {
		this.expression = expression;
		this.formatString = format;
	}
	
	public Expression getExpression() {
		return expression;
	}

	public void parseFormat() {
		if (formatString == null) return;
		int minFrac = -1;  // -1 indicates that the value has not been set
		int maxFrac = -1;

		StringTokenizer st = new StringTokenizer(formatString, "mM", true);
		char type = '-';
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (type != '-') {
				switch (type) {
				case 'm':
					if (minFrac != -1) throw new IllegalArgumentException("Invalid formatting string");
					minFrac = Integer.parseInt(token);
					break;
				case 'M':
					if (maxFrac != -1) throw new IllegalArgumentException("Invalid formatting string");
					maxFrac = Integer.parseInt(token);
					break;
				default:
					throw new IllegalArgumentException("Invalid formatting string.");
				}
				type = '-';
			} else if (token.equals("m")) {
				type = 'm';
			} else if (token.equals("M")) {
				type = 'M';
			} else {
				throw new IllegalArgumentException(formatString);
			}
		}

		if (maxFrac == -1) {
			if (minFrac == -1) {
				throw new IllegalArgumentException("Invalid formatting string.");
			}
			maxFrac = minFrac;
		} else if (minFrac == -1) {
			minFrac = 0;
		}
		if (minFrac > maxFrac) {
			throw new IllegalArgumentException("The minimum digits cannot be greater than maximum.");
		}
		if (minFrac > 50 || maxFrac > 50) {// sanity check
			throw new IllegalArgumentException("\nCannot specify more than 50 fraction digits");
		}
		this.minFracDigits = minFrac;
		this.maxFracDigits = maxFrac;
	}
	
	public String getFormatString() {
		return formatString;
	}



	public void execute(Environment env) throws TemplateException, IOException 
	{
		Number num = EvaluationUtil.getNumber(expression, env);

		FormatHolder fmth = formatCache;  // atomic sampling
		if (fmth == null || !fmth.locale.equals(env.getLocale())) {
			synchronized(this) {
				fmth = formatCache;
				if (fmth == null || !fmth.locale.equals(env.getLocale())) {
					NumberFormat fmt = NumberFormat.getNumberInstance(env.getLocale());
					if (formatString != null) {
						fmt.setMinimumFractionDigits(minFracDigits);
						fmt.setMaximumFractionDigits(maxFracDigits);
					} else {
						fmt.setMinimumFractionDigits(0);
						fmt.setMaximumFractionDigits(50);
					}
					fmt.setGroupingUsed(false);
					formatCache = new FormatHolder(fmt, env.getLocale());
					fmth = formatCache;
				}
			}
		}
		// We must use Format even if there is no formatString
		// Some locales may use non-Arabic digits, thus replacing the
		// decimal separator in the result of toString() is not enough.
		env.getOut().write(fmth.format.format(num));
	}

	public String getDescription() {
		return getSource();
	}

	private static class FormatHolder {
		final NumberFormat format;
		final Locale locale;

		FormatHolder(NumberFormat format, Locale locale) {
			this.format = format;
			this.locale = locale;
		}
	}
}
