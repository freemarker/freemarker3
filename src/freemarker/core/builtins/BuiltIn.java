package freemarker.core.builtins;

import freemarker.core.Environment;
import freemarker.core.ast.BuiltInExpression;

abstract public class BuiltIn {
    abstract public Object get(Environment env, BuiltInExpression caller);
    
    /**
     * True if the value this built-in returns from {@link #get(Environment, BuiltInExpression)} 
     * is free from side-effects - it does not depend on anything except its 
     * operand expression. Most built-ins are side-effect free and as such can 
     * be evaluated as literals at parse-time when their operands are also 
     * literals. Built-ins that depend on current environment settings are not
     * free of side-effects, i.e. ?contains, ?seq_index_of, ?seq_last_index_of,
     * ?sort, ?sort_by can depend on the environment's collator and/or 
     * arithmetic engine, ?string can depend on the environment's string 
     * formatting settings, and ?eval depends on variables defined in the 
     * scope. Most others are side-effect free though.  
     * @return true if the built-ins return value is free of side-effects.
     */
    public boolean isSideEffectFree() {
        return true;
    }
}
