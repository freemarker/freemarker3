[#set sectionNestingLevel = 1]

[#macro section][#scoped title anchor]
   [#if sectionNestingLevel>2]
      [#stop 'Too deep nesting of "section" elements.']
   [/#if]
   [#set anchor = .current_node.@anchor!]
   [#set title = .current_node.@title!]
   [#if anchor?has_content]
      <a name="${anchor?html}"></a>[#lt]
   [/#if]
   [#if title?has_content]
      <h${sectionNestingLevel+1}>${.current_node.@title?html}</h${sectionNestingLevel+1}>[#lt]
   [/#if]
   [#set sectionNestingLevel = sectionNestingLevel+1]
   [#recurse ]
   [#set sectionNestingLevel = sectionNestingLevel-1]
[/#macro]

[#macro _recurse][#recurse ][/#macro]

[#set headsection = _recurse, sections = _recurse, page = _recurse]

[#macro online]
  [#if !properties["site.offline"]??]
    [#recurse ][#t]
  [/#if]
[/#macro]

[#macro offline]
  [#if properties["site.offline"]??]
    [#recurse ][#t]
  [/#if]
[/#macro]

[#macro downloadWarnings]
  [#if properties["site.offline"]??]
    [@_noteBox]
      <b>This page is possibly outdated!</b> This is an offline snapshot of the Web page
      (made on ${properties.timeStamp}), so the downloads above are possibly outdated.
      You may visit the live Web page: <a href="http://freemarker.org/">http://freemarker.org/</a>
    [/@_noteBox]
  [/#if]
  [#-- @_noteBox>
    <b>If you get "A lone zero block at ..." error when extracting a tar file:</b>
    It's certainly <a href="http://bugs.debian.org/cgi-bin/bugreport.cgi?bug=235820">a bug in GNU tar</a>,
    introduced with its 1.13.93 version (or a bit earlier). Try another version. Earlier tar versions
    should work.
  </@ --]
[/#macro]

[#macro _noteBox]
  <p style="background-color: ${menuBgColor}; padding: 4px"><i>
    [#nested]
  </i></p>
[/#macro]

[#macro para]
  <p>[#recurse ]</p>[#t]
[/#macro]  

[#macro code]
  [#if .node?ancestors("para")?has_content]
     <tt>[#recurse ]</tt>[#t]
  [#else]
     <xmp>[#recurse ]</xmp>[#lt]
  [/#if]
[/#macro]

[#macro resources]
  <table border="0" cellpadding="4">
    [#recurse ]
  </table>   
[/#macro]

[#macro resource]
  <tr valign="top">
    <td><a href="${.node.url}">${.node.name}</a></td>
    <td>[#recurse .node.description]</td>
  </tr>
[/#macro]

[#macro @text]${.node?html}[/#macro]

[#macro @element]${.node.@@markup}[/#macro]

