[#import "htmloutput.ftl" as htmloutput]

[#macro part][#scoped chapters appendices titleText]
   [#set titleText = .node.title?string]
   <dt>${htmloutput.romanNumerals[romanNumeralsIndex]}.
   [#set romanNumeralsIndex = romanNumeralsIndex+1]
   <a href="${CreateLinkFromID(.node.@id)}">${titleText}</a>
   [#set chapters = .node.chapter]
   [#set appendices = .node.appendix]
   [#if chapters?has_content]
   <dd><dl>
     [#list chapters as chapter]
         <dt>${chapter_index+1}. [#visit chapter][#--/dt--][#t]
     [/#list]
   </dl></dd>
   [/#if]
   [#if appendices?has_content]
   <dd><dl>
     [#list appendices as appendix]
         <dt>${htmloutput.capitalLetters[appendix_index]}. [#visit appendix][#t]
     [/#list]
   </dl></dd>
   [/#if]
[/#macro]

[#macro chapterOrAppendix][#scoped title sections]
   [#set title = .node.title!]
   [#if !title?has_content]
      [#set title = .node?node_name?cap_first]
   [/#if]
   <a href="${CreateLinkFromID(.node.@id)}">${title}</a></dt>
   [#set sections = .node.sect1]
   [#if sections?has_content]
    <dd><dl>
    [#list sections as sect]
       <dt>[#visit sect]</dt>[#t]
    [/#list]
    </dl></dd>
   [/#if]
[/#macro]

[#macro specialPart][#scoped title]
   [#set title = .node.title!]
   [#if !title?has_content]
      [#set title = .node?node_name?cap_first]
   [/#if]
   <dt><a href="${CreateLinkFromID(.node.@id)}">${title}</a>
   <dd>
[/#macro]

[#macro sect1][#scoped titleElement]
   [#set titleElement = .node.title]
   <a href="${CreateLinkFromID(.node.@id)}">[#visit titleElement]</a>[#t]
[/#macro]

[#set chapter = chapterOrAppendix, appendix = chapterOrAppendix, preface = specialPart, glossary = specialPart, index = specialPart]

[#set partIndex = 0]
[#set chapterIndex = 0]

[#macro title][#scoped parentTag]
   [#set parentTag = .node?parent?node_name]
   [#if parentTag = "book" || parentTag = "part" || parentTag = "chapter" || parentTag = "appendix"]
       [#fallback]
   [#else]
       [#recurse ]
   [/#if]
[/#macro]