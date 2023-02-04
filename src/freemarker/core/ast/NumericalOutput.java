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
