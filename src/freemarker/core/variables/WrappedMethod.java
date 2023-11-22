package freemarker.core.variables;

@FunctionalInterface
public interface WrappedMethod {
   Object exec(Object... args);
}