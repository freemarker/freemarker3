<#set foo="set in namespace">
<#list 1..2 as i>
   <#var foo>
   ${foo!"foo not available here"}
   <#set foo = "set in loop">
   ${foo}
</#list>
---------
${foo}
---------
<#macro foobar>
   <#var foo>
   <#set foo="set in macro">
   ${foo}
   -----
   <#nested>
</#macro>

<@foobar>
   <#var foo="set in body">
   ${foo}
   ${foobar?scope["foo"]}
</@foobar>
------
${foo}
   
