package freemarker.template;

import java.io.Serializable;

/**
 * <p>This is a marker interface that indicates that an object
 * can be put in a template's data model.
 * 
 * @see TemplateHashModel
 * @see TemplateSequenceModel
 * @see TemplateCollectionModel
 * @see TemplateScalarModel
 * @see TemplateNumberModel
 * @see TemplateTransformModel
 *
 * @version $Id: TemplateModel.java,v 1.19 2003/01/12 23:40:21 revusky Exp $
 */
public interface TemplateModel {    /**
     * A general-purpose object to represent nothing. It acts as
     * an empty string, false, empty sequence, empty hash, and
     * null-returning method model. It is useful if you want
     * to simulate typical loose scripting language sorts of 
     * behaviors in your templates. 
     * @deprecated Try not to use this.
     */
    TemplateModel NOTHING = GeneralPurposeNothing.getInstance();

    /**
     * A singleton value used to represent a java null
     * which comes from a wrapped Java API, for example, i.e.
     * is intentional. A null that comes from a generic container
     * like a map is assumed to be unintentional and a 
     * result of programming error.
     */
    TemplateModel JAVA_NULL = new JavaNull();
    
    /**
     * A singleton value used to represent the result of an 
     * invalid expression, such as 1 - "3"
     */
    
    TemplateModel INVALID_EXPRESSION = new InvalidExpressionModel();
    
    /**
     * The type of the {@link TemplateModel#JAVA_NULL} object. Using a named 
     * class instead of an anonymous one, as it is easier to figure out what's 
     * wrong from an error message when the reported class name is 
     * "TemplateModel$JavaNull" than when it is "TemplateModel$1", also 
     * implements serialization singleton.
     * @author Attila Szegedi
     * @version $Id: $
     */
    static class JavaNull implements TemplateModel, Serializable {
        private static final long serialVersionUID = 1L;

        JavaNull() {}
        
        private Object readResolve() {
            return JAVA_NULL;
        }
    };
    
    static class InvalidExpressionModel implements TemplateModel {
    	InvalidExpressionModel() {}
    }
}