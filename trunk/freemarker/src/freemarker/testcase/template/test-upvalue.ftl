<#set color = "red">

<#macro tr color="blue">
   ${color} <#-- blue when unqualified, since this was passed in as a local param -->
   
   <#set color = "green"> <#-- only sets the local var, of course -->
   
   ${.namespace["color"]} <#-- still red in the enclosing namespace -->
   ----
   <@td />
   
   ----
   ${color} <#-- yellow, since it was changed in the td macro we called -->
</#macro>

<#macro td>
   ${color} <#-- unqualifed, we get the color var from the namespace, red -->
   ${tr?scope["color"]} <#-- now we get the upvalue "color" from the tr scope, green -->
   <#set color = "yellow" in tr?scope> <#-- We can set it in the scope, though it would rarely be good practice. -->
</#macro>   

<@tr/>
--
${color} <#-- red, of course, since it was never changed at the namespace level -->
   
