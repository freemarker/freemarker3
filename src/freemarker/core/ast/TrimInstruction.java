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

import freemarker.core.Environment;

/**
 * An instruction that indicates that that opening
 * and trailing whitespace on this line should be trimmed.
 * @version $Id: TrimInstruction.java,v 1.2 2003/08/29 09:42:38 revusky Exp $
 */
public class TrimInstruction extends TemplateElement {

    private boolean left, right;

    public TrimInstruction(boolean left, boolean right) {
        this.left = left;
        this.right = right;
    }
    
    public boolean isLeft() {
    	return left;
    }
    
    public boolean isRight() {
    	return right;
    }

    public void execute(Environment env) {
        // This instruction does nothing at render-time, only parse-time.
    }

    public String getDescription() {
        String type = "";
        if (!right) type = "left ";
        if (!left) type = "right ";
        if (!left && !right) type = "no-";
        return type + "trim instruction";
    }

    public boolean isIgnorable() {
        return true;
    }
}
