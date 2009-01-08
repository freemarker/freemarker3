[#import "htmloutput.ftl" as htmloutput]
[@htmloutput.Html "Index"]

[#macro entryText node]
   [#list 1..100 as i]
      [#if node?node_type != "element"]
         entry[#t]
         [#return]
      [/#if]
      [#if node.title?has_content]
         [#var title=node.title]
         [#if !node.@id!?starts_with("autoid_")]
            ${title?trim?html}[#t]
            [#return]
         [/#if]
      [/#if]
      [#set node = node?parent]
   [/#list]
   No title[#t]
[/#macro]

<div class="index">
   <div class="titlepage">
         <div>
            <h2 class="title"><a name="alphaidx"></a>Index</h2>
         </div>
   </div>
   
   [#-- ABC links --]
   [#set lastLetter = ""]
   <p>
   [#list indexEntries as key]
      [#set letter = key[0]?upper_case]
      [#if lastLetter != letter]
         [#if lastLetter != ""]&nbsp;| [/#if]<a href="#${letter?html}">${letter?html}</a>[#t]
         [#set lastLetter = letter]
      [/#if]
   [/#list]
   </p>

   [#-- Index list --]
   <div class="index">
   [#set lastLetter = ""]
   [#list indexEntries as key]
      [#set letter = key[0]?upper_case]
      [#if letter != lastLetter]
        [#if lastLetter != ""]
           </dl></div>[#lt]
        [/#if]
        <div class="indexdiv">[#lt]
        <a name="${letter?html}"></a>[#lt]
        <h3>${letter?html}</h3>[#lt]
        <dl>[#lt]
        [#set lastLetter = letter]
      [/#if]
      [#set entryNodes = primaryIndexTermLookup[key]]
      <dt>
         ${key?html}[#if entryNodes?has_content],&nbsp;&nbsp;[/#if][#rt]
         [#list entryNodes as entryNode]
           <a href="${CreateLinkFromNode(entryNode)}">[#t][@entryText entryNode/]</a>[#t]
           [#if entryNode_has_next],[/#if][#lt]
         [/#list]
      </dt>
      [#if secondaryIndexTermLookup[key]?has_content]
         [#set secondaryTerms = secondaryIndexTermLookup[key]]
         <dd><dl>
         [#list secondaryTerms?keys?sort as secondary]
            <dt>[#rt]
            ${secondary?html}, [#t]
            [#list secondaryTerms[secondary] as secondaryNode]
               <a href="${CreateLinkFromNode(secondaryNode)}">[#t]
                  [@entryText secondaryNode/][#t]
               </a>[#if secondaryNode_has_next], [/#if][#t]
            [/#list]
            </dt>[#lt]
         [/#list]
         </dl></dd>
      [/#if]
      [#if !key_has_next]
         </dl></div>[#lt]
      [/#if]
   [/#list]
   </div>
</div>

[/@]
