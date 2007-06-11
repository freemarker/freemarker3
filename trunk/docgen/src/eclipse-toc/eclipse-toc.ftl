<#ftl strict_syntax="true" >
<?xml version="1.0" encoding="iso-8859-1"?>
<?NLS TYPE="org.eclipse.help.toc"?>

<#-- Setup node handlers: -->
<#assign chapter = chapterOrAppendix,
         appendix = chapterOrAppendix,
         preface = specialPart,
         glossary = specialPart,
         index = specialPart
>

<#-- Misc. setup: -->
<#assign doc_prefix="docs">
<#assign romanNumerals = ["I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"]>
<#assign capitalLetters = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"]>

<#-- Do processing: -->
<#assign book=.node.book>
<toc label="Freemarker Manual" link_to="freemarker-toc.xml#ManualLink">
  <#visit book.preface>
  <#assign romanNumeralsIndex = 0>
  <#list book.part as part>
     <#visit part>
  </#list>
  <#visit book.glossary>
  <#visit book.index>
</toc>

<#--
  Node handler and other utility definitions:
-->

<#escape x as x?html>

<#-- We need to figure it how to replace CreateLinkFromID() function -->
<#function CreateLinkFromID id>
  <#return "${id}.html">
</#function>

<#macro part>
   <#local titleText = .node.title?string>
   <#assign glPartNumber=romanNumerals[romanNumeralsIndex] >
   <topic label="${glPartNumber}. ${titleText}" href="${doc_prefix}/docs/${CreateLinkFromID(.node.@id)}">
   <#assign romanNumeralsIndex = romanNumeralsIndex+1>
   <#local chapters = .node.chapter>
   <#local appendices = .node.appendix>
   <#if chapters?has_content>
     <#list chapters as chapter>
         <#assign glChapterOrAppendixNumber=chapter_index+1>
         <#visit chapter>         
     </#list>
   </#if>
   <#if appendices?has_content>
     <#list appendices as appendix>
         <#assign glChapterOrAppendixNumber=capitalLetters[appendix_index]>
         <#visit appendix>         
     </#list>
   </#if>
   </topic>
</#macro>

<#macro chapterOrAppendix>
   <#local title = .node.title?if_exists>
   <#if !title?has_content>
      <#local title = .node?node_name?cap_first>
   </#if>
   <topic label="${glChapterOrAppendixNumber}. ${title}" href="${doc_prefix}/docs/${CreateLinkFromID(.node.@id)}">
   <#local sections = .node.sect1>
   <#if sections?has_content>
    <#list sections as sect>
       <#visit sect>       
    </#list>
   </#if>
   </topic>
</#macro>

<#macro specialPart>
   <#local title = .node.title?if_exists>
   <#if !title?has_content>
      <#local title = .node?node_name?cap_first>
   </#if>
   <topic label="${title}" href="${doc_prefix}/docs/${CreateLinkFromID(.node.@id)}"/>
</#macro>

<#macro sect1>
   <#local titleElement = .node.title>
   <topic label="${titleElement}" href="${doc_prefix}/docs/${CreateLinkFromID(.node.@id)}"/><#t>
</#macro>

<#--
<#assign partIndex = 0>
<#assign chapterIndex = 0>

<#macro title>
   <#local parentTag = .node?parent?node_name>
   <#if parentTag == "book" || parentTag == "part" || parentTag == "chapter" || parentTag == "appendix">
       <#fallback>
   <#else>
       <#recurse>
   </#if>
</#macro>
-->
</#escape>