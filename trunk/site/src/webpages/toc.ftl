[#set logoImage = "logo_e0e0e0.png"]

[#-- The following macros are used in recursive processing. --]

[#macro @element][#recurse ][/#macro]

[#macro group][#scoped title]
<tr><td>
  <table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr><td width="100%">
      [#if properties["site.offline"]?? && .node.@offlineName?has_content]
        [#set title = .node.@offlineName]
      [#else]
        [#set title = .node.@name]
      [/#if]
      <b>${title?html}</b>
    </td></tr>
  </table>
  <table border="0" cellpadding="0" cellspacing="0" width="100%">
    [#recurse ]
  </table>
</td></tr>
[/#macro]

[#macro item][#scoped title]
  <tr>
    <td align="left" valign="top"><img src="images/bullet_e0e0e0.png" width=7 height=10 alt="-">&nbsp;</td>
    <td width="93%" align="left">
      [#if properties["site.offline"]?? && .node.@offlineName?has_content]
        [#set title = .node.@offlineName]
      [#else]
        [#set title = .node.@name]
      [/#if]
      <a class="nav" href="${.node.@url?html}">${title?html}</a>
    </td>
  </tr>
[/#macro]

[#macro site]
  <table bgcolor="${menuBgColor}" cellspacing="0" cellpadding="0" border="0" width="100%">
    <tr valign="top">
      <td height="1" width="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"></td>
      <td height="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"></td>
      <td height="1" width="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"></td>
    </tr>
    <tr>
      <td width="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"/></td>
      <td>
        <table bgcolor="${menuBgColor}" cellspacing="0" cellpadding="0" border="0" width="100%" style="margin: 0px">
          <tr><td>

  <table bgcolor="${menuBgColor}" border="0" cellspacing="10" cellpadding="0" width="100%">
    [#-- logo --]
    <tr><td colspan="2" align="center">
        <a href="${.node.@deployUrl?html}"><img border="0" src="images/${logoImage}" alt="FreeMarker logo"></a><br>&nbsp;
    </td></tr>
    [#-- navigation groups --]
    [#recurse ]
  </table>

          </td></tr>
        </table>
      </td>
      <td width="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"/></td>
    </tr>
    <tr valign="top">
      <td height="1" width="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"></td>
      <td height="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"></td>
      <td height="1" width="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"></td>
    </tr>
  </table>            
[/#macro]


