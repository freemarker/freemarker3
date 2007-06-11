[#import "htmloutput.ftl" as htmloutput]
[#import "toc.ftl" as toc]
[#import "docbook-html.ftl" as default]
[#-- assign namespaces = ['toc.ftl', 'htmloutput.ftl', 'docbook-html.ftl'] --] [#-- This should work really... --]
[#set namespaces = [toc, htmloutput, default]]
[#set titleElement = .node.title]
[#set subTitleElement = .node.subtitle]
[@htmloutput.Html titleElement]

<div class="book">

<h1>${titleElement?html}<br><span style="font-size: 50%">${subTitleElement?html}</span></h1>

 <div class="toc">
 
  <p><b>Table of Contents</b></p>
  <dl>
  [#visit .node.preface using namespaces]
  
  [#if .node.part?size != 0]  
    [#set romanNumeralsIndex = 0 in toc]
    [#list .node.part as part]
      [#visit part using namespaces]
    [/#list]
  [#else]
    [#-- If we have no parts, then maybe we have chapters directly under "book" --]
    [#list .node.chapter as chapter]
      <dt>${chapter_index+1}. [#visit chapter using namespaces][#t]
    [/#list]
  [/#if]
  
  [#if .node.glossary?size != 0]
    [#visit .node.glossary using namespaces]
  [/#if]  
  [#if .node.index?size != 0]
    [#visit .node.index using namespaces]
  [/#if]  
  </dl>
 </div>
</div>

[/@]
