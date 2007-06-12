<#set foo="set in namespace">
<#list 1..2 as i>
   <#scoped foo>
   ${foo!"foo not available here"}
   <#set foo = "set in loop">
   ${foo}
</#list>
---------
${foo}
---------
<#macro foobar>
   <#scoped foo>
   <#set foo="set in macro">
   ${foo}
   -----
   <#nested>
</#macro>

<@foobar>
   <#scoped foo="set in body">
   ${foo}
   ${foobar?scope["foo"]}
</@foobar>
------
${foo}
   
