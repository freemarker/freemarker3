[#set romanNumerals = ["I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"]]
[#set capitalLetters = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"]]
[#set inHtmlP = false, compactPara = false, disableA = false]

[#set forProgrammersStyle = "color:#333399; font-style:italic"]

[#-- 
   A set of routines used for outputting docbook elements as html
--]

[#-- 
  This is the macro that, by default, will be used to output a text node.
--]

[#macro _plaintext]${.node}[/#macro]

[#macro Anchor node=.node]
  [#if !disableA&&node.@id[0]??]
    <a name="${node.@id[0]!}"></a>[#t]
  [/#if]  
[/#macro]

[#macro Html title]
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
  <link rel="stylesheet" href="fmdoc.css" type="text/css">
  <meta name="generator" content="FreeMarker-based XDocBook Stylesheet">
  [#var bookTitle=.node?root.book.title]
  <title>[#if bookTitle != title]${bookTitle?html} - [/#if]${title?html}</title>
</head>
<body>
[#include "nav.ftl"]
<div id="mainContent">[#nested]</div>
<br>
[#include "nav.ftl"]   
<table border=0 cellspacing=0 cellpadding=0 width="100%">
  <tr>
    <td colspan=2><img src="images/none.gif" width=1 height=4 alt=""></td>
  <tr>
    <td align="left" valign="top"><span class="footnote">
        Page generated: ${transformStartTime?string("yyyy-MM-dd HH:mm:ss z")}
    </span></td>
    <td align="right" valign="top"><span class="footnote">
        ${.node?root.book.title?html}[#if .node?root.book.subtitle?size != 0] -- ${.node?root.book.subtitle?html}[/#if]
    </span></td>
  </tr>
</table>
</body>
</html>
[/#macro]

[#macro anchor]
  [@Anchor/]
[/#macro]

[#macro answer]
<div class="answer">
  [#recurse ]
</div>
[/#macro]

[#macro emphasis]
    [#var role=.node.@role[0]!"none"]
    [#if role = "term" || role = "bold" || .node?ancestors("programlisting")?has_content]
      <b>[#recurse ]</b>[#t]
    [#else]
      [#fallback] 
    [/#if]
[/#macro]

[#macro entry]
    [#if .node?parent?parent?node_name = "thead"]
[#-- TODO: align should be parametrized, of course. --]    
       <th align="left">[#t]
          [#recurse ][#t]
       </th>[#lt]
    [#else]
       <td align="left">[#t]
          [#recurse ][#t]
       </td>[#lt]
    [/#if]
[/#macro]

[#macro glossentry]
   [#recurse ]
[/#macro]

[#macro glossdef]
   <dd>[#recurse ]
   [#var seealsos=.node.glossseealso]
   [#if seealsos?has_content]
    <p>See Also
     [#list seealsos as also]
       [#var otherTermID=also.@otherterm]
       [#var otherNode=NodeFromID(otherTermID)]
       [#var term=otherNode.glossterm]
       <a href="${CreateLinkFromID(also.@otherterm)}">${term}</a>[#if also_has_next],[/#if] 
     [/#list]
    </p>
   [/#if]
   </dd>
[/#macro]

[#macro glosssee]
    <dd><p>See
       [#var otherTermID=.node.@otherterm]
       [#var otherNode=NodeFromID(otherTermID)]
       [#var term=otherNode.glossterm]
       <a href="${CreateLinkFromID(otherTermID)}">${term}</a>
    </p></dd>
[/#macro]

[#macro glossseealso]
[#-- This is dealt with in the glossdef routine --]
[/#macro]

[#macro glossterm]
   <dt>[@Anchor .node?parent/][#recurse ]</dt>
[/#macro]

[#macro graphic]
  [#var alt role=.node.@role[0]!?string]
  [#if role?starts_with("alt:")]
    [#set alt = role[4.. .node.@role?length-1]?trim]
  [#else]  
    [#set alt = "figure"]
  [/#if]
  <img src="${.node.@fileref}" alt="${alt?html}">[#t]
[/#macro]

[#macro indexterm]
  [@Anchor/]
[/#macro]

[#macro informaltable]
   <div class="informaltable">
      <table border="1" cellpadding="4">
         [#recurse ]
      </table>
   </div>
[/#macro]

[#macro itemizedlist]
    [#var packed=.node.@spacing[0]! = "compact"] 
    [#var prevCompactPara=compactPara]
    [#if packed]
       [#set compactPara = true]
    [/#if]
    [@CantNestedIntoP]
    <div class="itemizedlist">
        [#var mark=.node.@mark[0]!]
        [#if mark = "bullet"]
            <ul type="disc">[#t]
        [#elseif mark = "box"]
            <ul type="square">[#t]
        [#elseif mark = "ring"]
            <ul type="circle">[#t]
        [#elseif mark = ""]
            <ul>[#t]
        [#else]
            <ul type="${mark?html}">[#t]
        [/#if]
        [#recurse ]
        </ul>[#t]
    </div>
    [/@CantNestedIntoP]
    [#set compactPara = prevCompactPara]
[/#macro]

[#macro link]
   <a href="${CreateLinkFromID(.node.@linkend)?html}">[#recurse ]</a>[#t]
[/#macro]

[#macro listitem]
   [#var mark=.node?parent.@mark[0]!]
   [#if mark != ""]
       <li style="list-style-type: ${mark?html}">[#t]
   [#else]
       <li>[#t]
   [/#if]
   [#recurse ]
   </li>[#t]
[/#macro]

[#macro _inline_monospaced]
    [#var moreStyle="" color="#A03D10"]
    [#if .node?ancestors("link")?has_content]
        [#-- If we are within a link, we don't change color, just use the regular link color --]   
        <tt>[#recurse ]</tt>[#t]
    [#else]
        [#if fontBgColor! != ""]
            [#set moreStyle = "; background-color:${fontBgColor}"]
        [/#if]
        <tt style="color: #A03D10${moreStyle}">[#recurse ]</tt>[#t]
    [/#if]
[/#macro]

[#set uri = _inline_monospaced]
[#set code = _inline_monospaced]
[#set constant = _inline_monospaced]
[#set envar = _inline_monospaced]
[#set markup = _inline_monospaced]
[#set prompt = _inline_monospaced]
[#set property = _inline_monospaced]
[#set sgmltag = _inline_monospaced]
[#set token = _inline_monospaced]
[#set type = _inline_monospaced]
[#set function = _inline_monospaced]
[#set parameter = _inline_monospaced]
[#set varname = _inline_monospaced]
[#set returnvalue = _inline_monospaced]
[#set errorcode = _inline_monospaced]
[#set errorname = _inline_monospaced]
[#set errortext = _inline_monospaced]
[#set errortype = _inline_monospaced]
[#set exceptionname = _inline_monospaced]
[#set interfacename = _inline_monospaced]
[#set structfield = _inline_monospaced]
[#set structname = _inline_monospaced]
[#set symbol = _inline_monospaced]
[#set classname = _inline_monospaced]
[#set methodname = _inline_monospaced]
[#set package = _inline_monospaced]
[#set literal = _inline_monospaced]

[#macro note]
<div style="margin-left: 0.5in; margin-right: 0.5in;">
   <h3>Note</h3>
   [#recurse ]
</div>
[/#macro]  

[#macro caution]
<div class="caution" style="margin-left: 0.5in; margin-right: 0.5in;">
   <h3>Caution</h3>
   [#recurse ]
</div>
[/#macro]  

[#macro olink]
    <a href="${olinks[.node.@targetdoc]}">[#recurse ]</a>[#t]
[/#macro]

[#macro orderedlist]
    [#var packed=(.node.@spacing[0]! = "compact")] 
    [#var prevCompactPara=compactPara]
    [#if packed]
       [#set compactPara = true]
    [/#if]
    [@CantNestedIntoP]
    <div class="orderedlist"><ol type="1">[#recurse ]</ol></div>[#t]
    [/@CantNestedIntoP]
    [#set compactPara = prevCompactPara]
[/#macro]

[#macro para]
  [#var style]
  [#if .node.@role[0]! = "forProgrammers"]
    [#set style = forProgrammersStyle]
  [/#if]
  [#if compactPara!]
    [#if style??]
      <span style="${style}">[#t]
    [/#if]
    [#recurse ]
    [#if style??]
      </span>[#t]
    [/#if]
  [#else]
    [#var content]
    [#set inHtmlP = true]
    <p[#if style??] style="${style}"[/#if]>[#t]
    [#set content][#recurse ][/#set][#t]
    [#-- Avoid empty p element when closing para directly after orderedlist or itemizedlist. --]
    [#if !content?matches(r".*<p>\s*$", "s")]
        ${content}</p>[#t]
    [#else]
        ${content?substring(0, content?last_index_of("<p>"))}[#t]
    [/#if]
    [#set inHtmlP = false]
  [/#if]
[/#macro]

[#macro CantNestedIntoP]
[#if inHtmlP]
  </p>[#t]
  [#set inHtmlP = false]
  [#nested]
  <p>[#t]
  [#set inHtmlP = true]
[#else]
  [#nested]
[/#if]
[/#macro]

[#macro phrase]
  [#var lastFontBgColor=fontBgColor]
  [#var moreStyle=""]
  [#var role=.node.@role[0]!]
  [#var bgcolors={"markedComment" : "#6af666", "markedTemplate" : "#D8D8D8", "markedDataModel" : "#99CCFF", "markedOutput" : "#CCFFCC", "markedText" : "#8acbfa", "markedInterpolation" : "#ffb85d", "markedFTLTag" : "#dbfe5e"}]
  [#if role != ""]
    [#if role = "homepage"]
      http://freemarker.org[#t]
    [#elseif role = "markedInvisibleText"]
      [#if fontBgColor! != ""]
        [#set moreStyle = ";background-color:${fontBgColor}"]
      [/#if]
      <i><span style="color: #999999 ${moreStyle}">[#recurse ]</span></i>[#t]
    [#elseif role = "forProgrammers"]
      [#if fontBgColor! != ""]
        [#set moreStyle = ";background-color:${fontBgColor}"]
      [/#if]
      <span style="${forProgrammersStyle}${moreStyle}">[#recurse ]</span>[#t]
    [#else]
      [#set lastFontBgColor = fontBgColor!]
      [#if !bgcolors[role]??]
        [#stop "Invalid role attribute value, \"" + role + "\""]
      [/#if]
      [#set fontBgColor = bgcolors[role]]
      <span style="background-color:${bgcolors[role]}">[#recurse ]</span>[#t]
      [#set fontBgColor = lastFontBgColor]
    [/#if]
  [/#if]
[/#macro]

[#macro programlisting]
  [@Anchor/]
  [#var content bgcolor]
  [#var role=.node.@role[0]!?string]
  [#var dotidx=role?index_of(".")]
  [#if dotidx != -1]
    [#set role = role[0..dotidx-1]]
  [/#if]
  [#switch role][#case "output"]
         [#set bgcolor = "#CCFFCC"]
         [#break]
      [#case "dataModel"]
         [#set bgcolor = "#99CCFF"]
         [#break]
      [#case "template"]
         [#set bgcolor = "#D8D8D8"]
         [#break]
      [#case "unspecified"]
         [#set bgcolor = "#F8F8F8"]
         [#break]
      [#case "metaTemplate"]
         <pre class="metaTemplate">[#t]
             [#recurse ]
         </pre>[#lt]
         [#return]
      [#default]
         [#set bgcolor = "#F8F8F8"]
  [/#switch]
  [@CantNestedIntoP]
  <div align="left">[#t]
    <table bgcolor="${bgcolor}" cellspacing="0" cellpadding="0" border="0">[#t]
      <tr valign="top">[#t]
        <td height="1" width="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"/></td>[#t]
        <td height="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"/></td>[#t]
        <td height="1" width="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"/></td>[#t]
      </tr>[#t]
      <tr>[#t]
        <td width="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"/></td>[#t]
        <td>[#t]
          <table bgcolor="${bgcolor}" cellspacing="0" cellpadding="4" border="0" width="100%" style="margin: 0px">[#t]
            <tr><td><pre style="margin: 0px">[#t]
            [#local content][#recurse ][/#local]
            ${content?chop_linebreak}&nbsp;<span style="font-size: 1pt"> </span></pre></td></tr>[#t]
          </table>[#t]
        </td>[#t]
        <td width="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"/></td>[#t]
      </tr>[#t]
      <tr valign="top">[#t]
        <td height="1" width="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"/></td>[#t]
        <td height="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"/></td>[#t]
        <td height="1" width="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"/></td>[#t]
      </tr>
    </table>[#t]
  </div>
  [/@CantNestedIntoP]
[/#macro]

[#macro qandaset]
<div class="qandaset">
[#var prevCompactPara=compactPara!]
[#set compactPara = true]
[#set qaIndex = 1]
<table border=0 cellpadding=0 cellspacing=4>
[#list .node.qandaentry as qandaentry]
  <tr align="left" valign="top">
    <td>${qaIndex}.&nbsp;&nbsp;
    [#var prevDisableA=disableA!]
    [#set disableA = true]
    <td>
    <a href="#${qandaentry.@id[0]!"faq_question_"+qaIndex}">
      [#recurse qandaentry.question]
    </a><br>
    [#set disableA = prevDisableA]
  [#set qaIndex = qaIndex+1]
[/#list]
</table>
[#set compactPara = prevCompactPara] 

[#set qaIndex = 1]
[#recurse]
  
</div>
[/#macro]

[#macro question]
[#var prevCompactPara=compactPara!]
[#set compactPara = true]
<div class="question">
  [@Anchor .node?parent/]<a name="faq_question_${qaIndex}"></a>
  ${qaIndex}.&nbsp;&nbsp;[#recurse]
</div>
[#set qaIndex = qaIndex+1]
[#set compactPara = prevCompactPara] 
[/#macro] 

[#macro remark]
  [#if showEditorNotes]
    <p style="background-color:#FFFF00">[[#recurse ]]</p>[#t]
  [/#if]
[/#macro] 

[#macro replaceable]
  [#var moreStyle=""]
  [#if .node?ancestors("markup")?has_content]
    [#if fontBgColor! != ""]
      [#set moreStyle = ";background-color:${fontBgColor}"]
    [/#if]
    <i style="color: #DD4400 ${moreStyle}">[#recurse ]</i>[#t]
  [#else]
    <i>[#recurse ]</i>[#t]
  [/#if]
[/#macro]

[#macro subtitle]
  [#-- We do nothing here because this is dealt with in the title macro --]
[/#macro]

[#macro simplesect]
  <div class="simplesect">[#recurse ]</div>[#t]
[/#macro]

[#macro title]
    [#var headingSize]
    [#var type=.node?parent?node_name]
    [#var headingSizeMap={"@document" : 1, "appendix" : 2, "book" : 1, "chapter" : 2, "part" : 1, "preface" : 2, "sect1" : 2, "sect2" : 3, "sect3" : 4, "simplesect" : 3}]
    [#var titleInitial=""]
    [#if .node?parent?node_name = "chapter"]
       [#set titleInitial = "Chapter "+chapterNumber+". "]
    [#elseif .node?parent?node_name = "appendix"]
       [#set titleInitial = "Appendix "+capitalLetters[appendixNumber-1]+". "]
    [#elseif .node?parent?node_name = "part"]
       [#set titleInitial = "Part "+romanNumerals[partNumber-1]+". "]
    [/#if]
    [#if !headingSizeMap[type]??]
       [#stop "Don't know how to render title of: "+type]
    [#else]
       [#set headingSize = headingSizeMap[type]]
<div class="titlepage">
   <div>
     <h${headingSize}>[@Anchor .node?parent/]${titleInitial}[#recurse ]</h${headingSize}>
   </div>
        [#var subtitle=.node?parent.subtitle]
        [#if subtitle?has_content]
           [#set headingSize = headingSize+1]
           <div>
              <h${headingSize}>[#visit subtitle]</h${headingSize}>
           </div>            
        [/#if]
</div>        
    [/#if]
[/#macro]

[#macro ulink]
    <a href="${.node.@url?html}">[#recurse ]</a>[#t]
[/#macro]

[#macro warning]
<div class="warning" style="margin-left: 0.5in; margin-right: 0.5in;">
<h3>Warning!</h3>
[#recurse ]
</div>            
[/#macro]

[#macro xref]
   [#var xrefID=.node.@linkend]
   [#var xrefLabel=xrefLabelLookup[xrefID]]
   <a href="${CreateLinkFromID(.node.@linkend)}">${xrefLabel}</a>[#t]
[/#macro]


