<#ftl>

<#assign foo = "bar">

<#import "test-curry.txt" as currylib>

<@currylib.x foo, "bar", "baz"/>

<@currylib.x?use_defaults(a=4) b=5 c=6/>

<#assign y = currylib.x?use_defaults(a = "Hello" b="World")>

<@y c=foo /> <#-- Why does the positional invocation <@y foo /> not work??? (JR) -->

