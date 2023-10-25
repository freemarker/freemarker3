package freemarker.template;

import freemarker.annotations.Parameters;

/**
 * A marker interface that tells the FreeMarker that the method arguments can
 * be lazily evaluated. Method models implementing this interface declare that
 * their arguments needn't be evaluated before the method is invoked, but that
 * each argument should be evaluated only when it is first retrieved from the
 * argument list by the model. While this interface extends 
 * {@link WrappedMethod} to reinforce the notion that it is to be applied
 * to method models, it can naturally be implemented by classes that implement
 * {@link WrappedMethod}. 
 * Note that for the time being, there is a limitation in the FreeMarker 
 * implementation that prevents lazy evaluation of arguments of method models
 * that have a {@link Parameters} annotation on them. If your class 
 * implementing {@link LazilyEvaluatableArguments} also has a 
 * {@link Parameters} annotation, its arguments will be eagerly evaluated. This
 * limitation may be lifted in a future version of FreeMarker. 
 * @author Attila Szegedi
 * @version $Id: LazilyEvaluatableArguments.java,v 1.2 2005/11/03 08:45:08 szegedia Exp $
 */
public interface LazilyEvaluatableArguments extends WrappedMethod {
}
