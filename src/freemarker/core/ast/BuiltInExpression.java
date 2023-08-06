package freemarker.core.ast;

import java.util.HashMap;

import freemarker.core.Environment;
import freemarker.core.builtins.BuiltIn;
import freemarker.core.builtins.DateTime;
import freemarker.core.builtins.ExistenceBuiltIn;
import freemarker.core.builtins.HashBuiltin;
import freemarker.core.builtins.MacroBuiltins;
import freemarker.core.builtins.NodeFunctions;
import freemarker.core.builtins.NumericalCast;
import freemarker.core.builtins.SequenceContainsBuiltIn;
import freemarker.core.builtins.SequenceFunctions;
import freemarker.core.builtins.StringFunctions;
import freemarker.core.builtins.StringTransformations;
import freemarker.core.builtins.TypeChecks;
import freemarker.core.builtins.cBI;
import freemarker.core.builtins.evalBI;
import freemarker.core.builtins.groupsBI;
import freemarker.core.builtins.interpretBI;
import freemarker.core.builtins.newBI;
import freemarker.core.builtins.numberBI;
import freemarker.core.builtins.resolveBI;
import freemarker.core.builtins.sizeBI;
import freemarker.core.builtins.sourceBI;
import freemarker.core.builtins.stringBI;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;



public class BuiltInExpression extends Expression {
    private static final HashMap<String, BuiltIn> knownBuiltins = new HashMap<String, BuiltIn>();
    {
        knownBuiltins.put("c", new cBI());
        knownBuiltins.put("size", new sizeBI());
        knownBuiltins.put("string", new stringBI());
        knownBuiltins.put("eval", new evalBI());
        knownBuiltins.put("new", new newBI());
        knownBuiltins.put("interpret", new interpretBI());
        knownBuiltins.put("resolve", new resolveBI());
        NumericalCast numericalCast = new NumericalCast();
        knownBuiltins.put("byte", numericalCast);
        knownBuiltins.put("double", numericalCast);
        knownBuiltins.put("float", numericalCast);
        knownBuiltins.put("int", numericalCast);
        knownBuiltins.put("long", numericalCast);
        knownBuiltins.put("short", numericalCast);
        knownBuiltins.put("floor", numericalCast);
        knownBuiltins.put("ceiling", numericalCast);
        knownBuiltins.put("round", numericalCast);
        knownBuiltins.put("capitalize", new StringTransformations.Capitalize());
        knownBuiltins.put("lower_case", new StringTransformations.LowerCase());
        knownBuiltins.put("upper_case", new StringTransformations.UpperCase());
        knownBuiltins.put("cap_first", new StringTransformations.CapFirst(true));
        knownBuiltins.put("uncap_first", new StringTransformations.CapFirst(false));
        knownBuiltins.put("j_string", new StringTransformations.Java());
        knownBuiltins.put("js_string", new StringTransformations.JavaScript());
        knownBuiltins.put("chop_linebreak", new StringTransformations.Chomp());
        knownBuiltins.put("trim", new StringTransformations.Trim());
        knownBuiltins.put("html", new StringTransformations.Html());
        knownBuiltins.put("rtf", new StringTransformations.Rtf());
        knownBuiltins.put("xml", new StringTransformations.Xml());
        knownBuiltins.put("xhtml", new StringTransformations.Xhtml());
        knownBuiltins.put("web_safe", knownBuiltins.get("html"));
        TypeChecks typeChecks = new TypeChecks();
        knownBuiltins.put("is_boolean", typeChecks);
        knownBuiltins.put("is_collection", typeChecks);
        knownBuiltins.put("is_date", typeChecks);
        knownBuiltins.put("is_enumerable", typeChecks);
        knownBuiltins.put("is_hash", typeChecks);
        knownBuiltins.put("is_hash_ex", typeChecks);
        knownBuiltins.put("is_indexable", typeChecks);
        knownBuiltins.put("is_directive", typeChecks);
        knownBuiltins.put("is_method", typeChecks);
        knownBuiltins.put("is_null", typeChecks);
        knownBuiltins.put("is_number", typeChecks);
        knownBuiltins.put("is_macro", typeChecks);
        knownBuiltins.put("is_node", typeChecks);
        knownBuiltins.put("is_sequence", typeChecks);
        knownBuiltins.put("is_string", typeChecks);
        knownBuiltins.put("is_transform", typeChecks);
        knownBuiltins.put("index_of", new StringFunctions.IndexOf());
        knownBuiltins.put("last_index_of", new StringFunctions.LastIndexOf());
        knownBuiltins.put("contains", new StringFunctions.Contains());
        knownBuiltins.put("number", new numberBI());
        knownBuiltins.put("left_pad", new StringFunctions.LeftPad());
        knownBuiltins.put("right_pad", new StringFunctions.RightPad());
        knownBuiltins.put("length", new StringFunctions.Length());
        knownBuiltins.put("replace", new StringFunctions.Replace());
        knownBuiltins.put("split", new StringFunctions.Split());
        knownBuiltins.put("groups", new groupsBI());
        knownBuiltins.put("matches", new StringFunctions.Matches());
        knownBuiltins.put("starts_with", new StringFunctions.StartsWith());
        knownBuiltins.put("ends_with", new StringFunctions.EndsWith());
        knownBuiltins.put("substring", new StringFunctions.Substring());
        knownBuiltins.put("word_list", new StringFunctions.WordList());
        knownBuiltins.put("url", new StringFunctions.Url());
        knownBuiltins.put("parent", new NodeFunctions.Parent());
        knownBuiltins.put("children", new NodeFunctions.Children());
        knownBuiltins.put("node_name", new NodeFunctions.NodeName());
        knownBuiltins.put("node_type", new NodeFunctions.NodeType());
        knownBuiltins.put("node_namespace", new NodeFunctions.NodeNamespace());
        knownBuiltins.put("root", new NodeFunctions.Root());
        knownBuiltins.put("ancestors", new NodeFunctions.Ancestors());
        knownBuiltins.put("first", new SequenceFunctions.First());
        knownBuiltins.put("last", new SequenceFunctions.Last());
        knownBuiltins.put("reverse", new SequenceFunctions.Reverse());
        knownBuiltins.put("sort", new SequenceFunctions.Sort());
        knownBuiltins.put("sort_by", new SequenceFunctions.SortBy());
        knownBuiltins.put("chunk", new SequenceFunctions.Chunk());
        knownBuiltins.put("seq_contains", new SequenceContainsBuiltIn());
        knownBuiltins.put("seq_index_of", new SequenceFunctions.IndexOf());
        knownBuiltins.put("seq_last_index_of", new SequenceFunctions.LastIndexOf());
        knownBuiltins.put("scope", new MacroBuiltins.Scope());
        knownBuiltins.put("namespace", new MacroBuiltins.Namespace());
        knownBuiltins.put("keys", new HashBuiltin.Keys());
        knownBuiltins.put("values", new HashBuiltin.Values());
        knownBuiltins.put("date", new DateTime(TemplateDateModel.DATE));
        knownBuiltins.put("time", new DateTime(TemplateDateModel.TIME));
        knownBuiltins.put("datetime", new DateTime(TemplateDateModel.DATETIME));
        knownBuiltins.put("is_defined", new ExistenceBuiltIn.IsDefinedBuiltIn());
        knownBuiltins.put("if_exists", new ExistenceBuiltIn.IfExistsBuiltIn());
        knownBuiltins.put("exists", new ExistenceBuiltIn.ExistsBuiltIn());
        knownBuiltins.put("default", new ExistenceBuiltIn.DefaultBuiltIn());
        knownBuiltins.put("has_content", new ExistenceBuiltIn.HasContentBuiltIn());
        knownBuiltins.put("source", new sourceBI());
    }

    private final Expression target;
    private final String key;
    private final BuiltIn bi;

    public BuiltInExpression(Expression target, String key) {
        this.target = target;
        target.setParent(this);
        this.key = key.intern();
        this.bi = knownBuiltins.get(key);
    }

    public BuiltIn getBuiltIn() {
        return bi;
    }

    public Object _getAsTemplateModel(Environment env) {
        return bi.get(env, this);
    }

    public Expression getTarget() {
        return target;
    }

    public String getName() {
        return key;
    }

    public boolean isLiteral() {
        return target.isLiteral() && bi.isSideEffectFree();
    }

    /*	
	@Override
    Expression _deepClone(String name, Expression subst) {
		findImplementation();
    	try {
	    	BuiltInExpression clone = (BuiltInExpression) this.clone();
	    	clone.target = target.deepClone(name, subst);
	    	clone.bi = knownBuiltins.get(key);
	    	return clone;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }*/

    Expression _deepClone(String name, Expression subst) {
        return new BuiltInExpression(target.deepClone(name, subst), key);
    }

}
