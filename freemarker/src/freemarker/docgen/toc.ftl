[#import "htmloutput.ftl" as htmloutput]
[#import "customizations.ftl" as customizations]

[#macro part]
   [#var titleText=.node.title?string]
   <dt>${htmloutput.romanNumerals[romanNumeralsIndex]}.
   [#set romanNumeralsIndex = romanNumeralsIndex+1]
   <a href="${CreateLinkFromID(.node.@id)}">${titleText}</a>
   [#var chapters=.node.chapter]
   [#var appendices=.node.appendix]
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

[#macro chapterOrAppendix]
   [#var title=.node.title!]
   [#if !title?has_content]
      [#set title = .node?node_name?cap_first]
   [/#if]
   <a href="${CreateLinkFromID(.node.@id)}">${title}</a></dt>
   [#var sections=.node.sect1]
   [#if sections?has_content]
    <dd><dl>
    [#list sections as sect]
       <dt>[#visit sect]</dt>[#t]
    [/#list]
    </dl></dd>
   [/#if]
[/#macro]

[#macro specialPart]
   [#var title=.node.title!]
   [#if !title?has_content]
      [#set title = .node?node_name?cap_first]
   [/#if]
   <dt><a href="${CreateLinkFromID(.node.@id)}">${title}</a>
   <dd>
[/#macro]

[#macro sect1]
   [#var titleElement=.node.title]
   <a href="${CreateLinkFromID(.node.@id)}">[#visit titleElement]</a>[#t]
[/#macro]

[#set chapter = chapterOrAppendix, appendix = chapterOrAppendix, preface = specialPart, glossary = specialPart, index = specialPart]

[#set partIndex = 0]
[#set chapterIndex = 0]

[#macro title]
   [#var parentTag=.node?parent?node_name]
   [#if parentTag = "book" || parentTag = "part" || parentTag = "chapter" || parentTag = "appendix"]
       [#fallback]
   [#else]
       [#recurse ]
   [/#if]
[/#macro]
