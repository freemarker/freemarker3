package freemarker.xml;

import org.w3c.dom.*;

class CharacterDataNodeModel extends WrappedDomNode {
    
    public CharacterDataNodeModel(CharacterData text) {
        super(text);
    }
    
    public String toString() {
        return ((org.w3c.dom.CharacterData) node).getData();
    }
    
    public String getNodeName() {
        return (node instanceof Comment) ? "@comment" : "@text";
    }
    
    public boolean isEmpty() {
        return true;
    }
}