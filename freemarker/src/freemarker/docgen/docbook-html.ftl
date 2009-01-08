
[#-- 
   A set of standard routines used for outputting docbook elements as html
--]

[#-- 
  This is the macro that, by default, will be used to output a text node.
--]

[#macro @text]${.node?html}[/#macro]

[#--
This is the fallback behavior for an element, we just recurse over the children.
--]

[#macro @element][#recurse ][/#macro]

[#macro emphasis]
   <i>[#recurse ]</i>[#t]
[/#macro]

[#macro graphic]
  <img src="${.node.@fileref}">[#t]
[/#macro]

[#macro itemizedlist]
   <ul>[#recurse ]</ul>[#t]
[/#macro]

[#macro listitem]
   <li>[#recurse ][#t]
[/#macro]   

[#macro literal]
    <code>[#recurse ]</code>[#t]
[/#macro]

[#set markup = literal]

[#macro note]
  <i>[#recurse ]</i>[#t]
[/#macro]  

[#macro caution]
  <i>[#recurse ]</i>[#t]
[/#macro]  

[#macro orderedlist]
   <ol>[#recurse ]</ol>[#t]
[/#macro]

[#macro para]
  <p>[#recurse ]</p>[#t]
[/#macro]

[#macro programlisting]
  <pre>[#recurse ]</pre>[#t]
[/#macro]

[#macro row]
  <tr>[#recurse ]</tr>  
[/#macro]

[#macro tbody]
  <tbody>[#recurse ]</tbody>
[/#macro]

[#macro thead]
   <thead>[#recurse ]</thead>
[/#macro]

[#macro title]
   <h1>[#recurse ]</h1>
[/#macro]

