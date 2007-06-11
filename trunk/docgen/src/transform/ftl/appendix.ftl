[#import "htmloutput.ftl" as htmloutput]
[#import "toc.ftl" as toc]
[#import "docbook-html.ftl" as default]
[#set titleElement = .node.title]
[@htmloutput.Html titleElement]

[#if .node.sect1?has_content]
  <div class="chapter">
  [#visit titleElement using [toc, htmloutput, default]]
  
  <div class="toc">
     <p><b>Table of Contents</b></p>
     <dl>
        [#list .node.sect1 as section]
           <dt>[#visit section using [toc, htmloutput, default]]</dt>
        [/#list]
     </dl>
  </div>
  
  [#list .node?children as child]
     [#if child?node_name != "title" && child?node_name != "sect1"]
         [#visit child using [toc, htmloutput, default]]
     [/#if]
  [/#list]
    
  </div>
[#else]
  [#visit .node using [.namespace, htmloutput, default]]
[/#if]

[/@]