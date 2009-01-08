[#import "htmloutput.ftl" as htmloutput]
[#import "docbook-html.ftl" as default]
[#import "customizations.ftl" as customizations]

[@htmloutput.Html .node.title]

<div class="sect1">
   [#recurse  using [customizations, .namespace, htmloutput, default]]
</div>

[/@]

[#macro sect1]
   [#recurse ]
[/#macro]
