[#import "htmloutput.ftl" as htmloutput]
[#import "docbook-html.ftl" as default]
[#import "customizations.ftl" as customizations]

[@htmloutput.Html "Glossary"]

<div class="glossary">
   <div class="titlepage">
        <div>
            <h2 class="title"><a name="gloss"></a>Glossary</h2>
        </div>
   </div>

   [#set ges = .node.glossentry?sort_by("glossterm")]

   [#-- Print alphabetical index links --]
   [#set lgtl = ""]
   <p>
   [#list ges as ge]
      [#set fullgt = ge.glossterm!]
      [#if fullgt?has_content]
         [#set gtl = fullgt.@@text[0]?upper_case]
         [#if gtl != lgtl]
            [#if lgtl != ""]&nbsp;| [/#if]<a href="#${ge.@id?html}">${gtl?html}</a>[#t]
            [#set lgtl = gtl]
         [/#if]
      [/#if]
   [/#list]
   </p>

   <dl>

   [#-- Print glossentry-es --]
   [#list ges as ge]
      [#visit ge using [customizations, .namespace, htmloutput, default]]
   [/#list]
   
   </dl>
</div>

[/@]