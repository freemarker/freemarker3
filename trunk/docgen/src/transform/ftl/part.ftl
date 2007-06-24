[#import "htmloutput.ftl" as htmloutput]
[#import "toc.ftl" as toc]
[#set namespaces = [toc, htmloutput]] 
[#set titleElement = .node.title]
[@htmloutput.Html titleElement]

[#visit titleElement using namespaces]


  <div class="toc">
   <p><b>Part Contents</b></p>
    <dl>
       [#list .node.chapter as chapter]
         <dt>${chapter_index+1}. [#visit chapter using namespaces]
       [/#list]
       [#list .node.appendix as appendix]
         <dt>${htmloutput.capitalLetters[appendix_index]}. [#visit appendix using namespaces]
       [/#list]
    </dl>
  </div>
  
  [#list .node?children as child]
     [#if child?node_name != "title" && child?node_name != "chapter" && child?node_name != "appendix"]
         [#visit child using namespaces]
     [/#if]
  [/#list]

[/@]