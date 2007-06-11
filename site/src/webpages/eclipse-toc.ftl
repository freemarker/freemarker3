[#escape x as x?html]
<?xml version="1.0" encoding="iso-8859-1"?>
<?NLS TYPE="org.eclipse.help.toc"?>

[#-- Setup and do processing: --]
[#set doc_prefix = "docs"]
[#visit .node]

[#-- Node handlers: --]

[#macro @element][#recurse ][/#macro]

[#macro item]
[#compress]  
  [#set name = .node.@name]
  [#set url = .node.@url]
[#-- Replace the manual link with anchor filled in later 
  <#if name="Manual">
	<topic label="Manual" href="${doc_prefix}/docs/index.html">
      <anchor id="ManualLink" />
    </topic>
  <#else>  
    <topic label="${.node.@name}" href="<#if url?ends_with("html")>${doc_prefix}/</#if>${url}" />
  </#if>
--]  
    <topic label="${.node.@name}" href="[#if url?ends_with("html")]${doc_prefix}/[/#if]${url}" />
[/#compress]  
[/#macro]

[#macro site]
<toc label="Freemarker Web site" topic="${doc_prefix}/index.html" link_to="freemarker-toc.xml#WebLink">
[#recurse ][#t]
</toc>
[/#macro]

[/#escape]