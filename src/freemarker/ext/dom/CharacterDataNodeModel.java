
 package freemarker.ext.dom;

import org.w3c.dom.*;
import freemarker.template.*;

class CharacterDataNodeModel extends NodeModel implements TemplateScalarModel {
    
    public CharacterDataNodeModel(CharacterData text) {
        super(text);
    }
    
    public String getAsString() {
        return ((org.w3c.dom.CharacterData) node).getData();
    }
    
    public String getNodeName() {
        return (node instanceof Comment) ? "@comment" : "@text";
    }
    
    public boolean isEmpty() {
        return true;
    }
}