[#import "htmloutput.ftl" as htmloutput]
[#import "docbook-html.ftl" as default]
[@htmloutput.Html .node.title]

<div class="sect1">
   [#recurse  using [.namespace, htmloutput, default]]
</div>

[/@]

[#macro sect1]
   [#recurse ]
[/#macro]