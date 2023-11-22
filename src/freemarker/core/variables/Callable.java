package freemarker.core.variables;

@FunctionalInterface
public interface Callable {
   Object call(Object... args);
}