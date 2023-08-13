package freemarker.template;

import freemarker.ext.beans.ObjectWrapper;

/**
 * <p>This is a marker interface that indicates that an object
 * can be put in a template's data model.
 * 
 * @see TemplateHashModel
 * @see TemplateSequenceModel
 * @see Iterable
 * @see TemplateScalarModel
 * @see TemplateNumberModel
 * @see TemplateTransformModel
 *
 */
public interface TemplateModel {
   default Object unwrap() {
      return ObjectWrapper.instance().unwrap(this);
   }
}