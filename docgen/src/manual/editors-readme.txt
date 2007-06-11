Guide to FreeMarker Manual for Editors
======================================

Rules and guidelines
--------------------


Non-technical:

- Understand that the Designer's Guide is for designers. Assume that a
  designer is not a programmer, (s)he doesn't even know what is Java.
  Forget that FM is implemented in Java when you edit Designer's
  Guide. Try to avoid technical writing.

- In the Guide chapters, please be careful not to mention things that
  were not explained earlier. The Guide chapters should be understandable
  if you read them continuously.

- If you add a new topic or term, don't forget to add it to the Index.
  Also, consider adding entries for it to the Glossary.

- Don't use too sophisticated English. Use basic words and grammar.


Technical:

- For the editing please use XXE (XMLmind XML Editor), with its
  default XML *source* formatting settings (identation, max line
  length and like). You should install the "DocBook for Freemarker"
  addon, which you can find somwhere in the same SVN module as this
  file. (Tested with XXE 3.6.0.)

- Please understand all document conventions in the Preface chapter.
  Read its XML source. Note that all "programlisting"-s should have
  a "role" attribute with a value that is either: "template",
  "dataModel", "output", "metaTemplate" or "unspecified". (If you
  miss this, the XXE addon will show the "programlisting" in red.)

- Verbatim content in flow text:

  * In flow text, all data object names, class names, FTL fragments,
    HTML fragments, and all other verbatim content is inside "literal"
    element.
    
  * Use replaceable element inside markup element for replaceable
    parts and meta-variables like:
    <literal&lt;if <replaceable>condition</replaceable>></literal>
    <literal><replaceable>templateDir</replaceable>/copyright.fm</literal>

- Don't use deeper sectX than sect3.

- Lists:
  * When you have list where the list items are short (a few words),
    you should give spacing="compact" to the "itemizedlist" or
    "orderedlist" element.
  
  * Avoid putting listings inside "para"-s. The HTML transform can't
    impement them, and will break the "para" into two "para"-s anyway.
    
- Xrefs, id-s, links:
  * id-s of parts, chapters, sections and similar elements must
    contain US-ASCII lower case letters, US-ASCII nubers, and
    underscore only. id-s of parts and chapters are used as the
    filenames of HTML-s generated for that block.
    When you find out the id, deduce it from the positin in the ToC
    hirearchy. The underscore is used as the separator between the path
    steps.
    
  * All other id-s must use prefix:
    - example: E.g.: id="example.foreach"
    - ref: Reference information...
      * directive: about a directive. E.g.: "ref.directive.foreach"
      * builtin
    - gloss: Term in the Glossary
    - topic: The recommended point of document in a certain topic
      * designer: for designers.
          E.g.: id="topic.designer.methodDataObject"
      * programmer: for programmers
      * or omit the secondary cathegoty if it is for everybody
    - misc: Anything doesn't fit in the above cathegories
    
  * When you refer to a part, chapter or section, often you should use
    xref, not link. The xreflabel attribute of the link-end must be
    set.
