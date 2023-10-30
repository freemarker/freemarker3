package freemarker.annotations;

import java.lang.annotation.*;

/**
 * An annotation that indicates that a class
 * should be exposed as a Pojo (Plain old java object)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface Pojo {}
