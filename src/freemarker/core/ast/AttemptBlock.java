package freemarker.core.ast;

import java.io.IOException;
import java.util.*;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.core.parser.ParsingProblem;

public class AttemptBlock extends TemplateElement {
    
    private List<ParsingProblem> parsingProblems = new ArrayList<ParsingProblem>();
    
    public AttemptBlock(TemplateElement attemptBlock, TemplateElement recoveryBlock) {
        add(attemptBlock);
        add(recoveryBlock);
    }
    
    public TemplateElement getAttemptBlock() {
        return childrenOfType(TemplateElement.class).get(0);
    }
    
    public TemplateElement getRecoverBlock() {
        return childrenOfType(TemplateElement.class).get(1);
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
    	set(0, TextBlock.EMPTY_BLOCK);
    }
    
    public void setParsingProblems(List<ParsingProblem> parsingProblems) {
    	this.parsingProblems = parsingProblems;
    }
}
