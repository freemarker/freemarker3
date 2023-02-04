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
