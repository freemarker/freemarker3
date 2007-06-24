
<table class="navigation" cellspacing="0" cellpadding="0" width="100%" border="0" bgcolor="#E0E0E0">
    <tr valign="top">
        <td height="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"></td>
    </tr>
    <tr>
        <td align="left" valign="middle">
            <table cellspacing="0" cellpadding="4" width="100%" border="0" bgcolor="#E0E0E0">
                <tr>
                    <td align="left">[#rt]
                    [#if !previousFilename?has_content]
                        <img border="0" hspace="0" vspace="0" alt="Prev" src="images/nav/prev_disabled.gif">[#t]
                    [#else]
                        <a accesskey="p" href="${previousFilename}"><img border="0" hspace="0" vspace="0" alt="Prev" src="images/nav/prev.gif"></a>[#t]
                    [/#if]
                    <img src="images/none.gif" width="4" height="1" alt="" hspace="0" vspace="0" border="0">[#t]
                    [#if !parentFilename?has_content]
                        <img border="0" hspace="0" vspace="0" alt="Up" src="images/nav/up_disabled.gif">[#t]
                    [#else]
                        <a accesskey="u" href="${parentFilename}"><img border="0" hspace="0" vspace="0" alt="Up" src="images/nav/up.gif"></a>[#t]
                    [/#if]
                    <img src="images/none.gif" width="4" height="1" alt="" hspace="0" vspace="0" border="0">[#t]
                    [#if !nextFilename?has_content]
                        <img border="0" hspace="0" vspace="0" alt="Prev" src="images/nav/next_disabled.gif">[#t]
                    [#else]
                        <a accesskey="n" href="${nextFilename}"><img border="0" hspace="0" vspace="0" alt="Next" src="images/nav/next.gif"></a>[#t]
                    [/#if]
                    <img src="images/none.gif" width="4" height="1" alt="" hspace="0" vspace="0" border="0"></td>[#lt]
                    <td align="right" valign="middle">
                        <span style="font-size: 16px; font-family: Arial, sans-serif">Bookmarks:
                        <a accesskey="h" href="index.html">Full ToC</a>, <a accesskey="i" href="alphaidx.html">Index</a>,
                        <a accesskey="g" href="gloss.html">Glossary</a>,
                        <a href="ref.html">Reference</a>,
                        <a href="app_faq.html">FAQ</a>
                        | External:
                        <a href="api/index.html">API</a>,
                        <a href="../index.html">Home</a>
                        </span>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr valign="top">
        <td height="1" bgcolor="black"><img src="images/none.gif" width="1" height="1" alt="" hspace="0" vspace="0" border="0"></td>
    </tr>
</table>