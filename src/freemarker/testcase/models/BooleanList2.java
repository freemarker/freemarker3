package freemarker.testcase.models;

import java.util.List;

import freemarker.template.TemplateSequenceModel;

import java.util.ArrayList;

/**
 * Model for testing list models. Every
 * other method simply delegates to a SimpleList model.
 *
 * @author  <a href="mailto:run2000@users.sourceforge.net">Nicholas Cull</a>
 * @version $Id: BooleanList2.java,v 1.12 2003/01/12 23:40:25 revusky Exp $
 */
public class BooleanList2 implements TemplateSequenceModel {

    private List<Object>  cList = new ArrayList<>();

    /**
     * @return the specified index in the list
     */
    public Object get(int i) {
        return cList.get(i);
    }

    public int size() {
        return cList.size();
    }
}
