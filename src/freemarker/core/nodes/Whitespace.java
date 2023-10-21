package freemarker.core.nodes;

import freemarker.core.parser.*;
import freemarker.core.parser.ast.Interpolation;
import freemarker.core.parser.ast.Macro;
import freemarker.core.parser.ast.Text;
import static freemarker.core.parser.Token.TokenType.*;


public class Whitespace extends Text {
    public Whitespace(TokenType type, FMLexer tokenSource, int beginOffset, int endOffset) {
        super(type, tokenSource, beginOffset, endOffset);
    }

    public boolean isIgnored() {
        return isNonOutputtingLine() 
               || getType() == TRAILING_WHITESPACE && checkForExplicitRightTrim() 
               || getType() == NON_TRAILING_WHITESPACE && getBeginColumn() == 1 && checkForExplicitLeftTrim();
    }

    private boolean checkForExplicitLeftTrim() {
        Token tok = nextCachedToken();
        while (tok != null && tok.getBeginLine() == this.getBeginLine()) {
            if (tok.getType() == TRIM || tok.getType() == LTRIM) {
                return true;
            }
            tok = tok.nextCachedToken();
        }
        return false;
    }

    private boolean checkForExplicitRightTrim() {
        Token tok = previousCachedToken();
        while (tok != null && tok.getBeginLine() == this.getBeginLine()) {
            if (tok.getType() == TRIM || tok.getType() == RTRIM) {
                return true;
            }
            tok = tok.previousCachedToken();
        }
        return false;
    }

    private boolean isNonOutputtingLine() {
        if (spansLine()) return false;
        Token tok = previousCachedToken();
        while (tok != null && tok.getEndLine() == getBeginLine()) {
            if (tok.firstAncestorOfType(Macro.class) != this.firstAncestorOfType(Macro.class)) {
                tok = tok.previousCachedToken();
                continue;
            }
            if (tok.getType() == CLOSE_BRACE && tok.getParent() instanceof Interpolation) {
                return false;
            }
            if (tok.getType() == REGULAR_PRINTABLE || tok.getType() == PROBLEMATIC_CHAR || tok.getType() == NOPARSE) {
                return false;
            }
            tok = tok.previousCachedToken();
        }
        tok = nextCachedToken();
        while (tok != null && tok.getBeginLine() == getBeginLine()) {
            if (tok.firstAncestorOfType(Macro.class) != this.firstAncestorOfType(Macro.class)) {
                tok = tok.nextCachedToken();
                continue;
            }
            if (tok.getType() == OUTPUT_ESCAPE) {
                return false;
            }
            if (tok.getType() == REGULAR_PRINTABLE || tok.getType() == PROBLEMATIC_CHAR || tok.getType() == NOPARSE) {
                return false;
            }
            tok = tok.nextCachedToken();
        }
        return true;
    }

    private boolean spansLine() {
        return getBeginColumn() == 1 && charAt(length() - 1) == '\n';
    }
}
