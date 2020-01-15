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
import java.util.*;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.core.parser.ParsingProblem;

public class AttemptBlock extends TemplateElement {
    
    private List<ParsingProblem> parsingProblems = new ArrayList<ParsingProblem>();
    
    public AttemptBlock(TemplateElement attemptBlock, TemplateElement recoveryBlock) {
        nestedElements = new ArrayList<TemplateElement>(2);
        nestedElements.add(attemptBlock);
        nestedElements.add(recoveryBlock);
    }
    
    public TemplateElement getAttemptBlock() {
    	return nestedElements.get(0);
    }
    
    public TemplateElement getRecoverBlock() {
        return nestedElements.get(1);
    }

    public void execute(Environment env) throws TemplateException, IOException 
    {
        env.render(getAttemptBlock(), getRecoverBlock(), parsingProblems);
    }

    public String getDescription() {
        return "attempt block";
    }
    
    public boolean hasParsingProblems() {
    	return !parsingProblems.isEmpty();
    }
    
    public List<ParsingProblem> getParsingProblems() {
    	return Collections.unmodifiableList(parsingProblems);
    }
    
    public void addParsingProblem(ParsingProblem problem) {
    	parsingProblems.add(problem);
    	nestedElements.set(0, TextBlock.EMPTY_BLOCK);
    }
    
    public void setParsingProblems(List<ParsingProblem> parsingProblems) {
    	this.parsingProblems = parsingProblems;
    }
}
