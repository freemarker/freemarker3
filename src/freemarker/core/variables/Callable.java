package freemarker.core.variables;

@FunctionalInterface
public interface Callable {
   Object exec(Object... args);
}