package freemarker.core.variables;

@FunctionalInterface
public interface Callable<T> {
   T call(Object... args);
}