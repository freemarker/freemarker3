[#ftl strict_vars="false"]
#attempt
 #assign sequence = ["Hello, World"]
 ${sequence[0]}
#recover
  We should never get here.
/#
#attempt
 Let's try to output an undefined variable: ${undefined}
#recover
 Well, that did not work. Here is the error: ${.error}
 Now we nest another attempt/recover here:
 #attempt
   ${sequence[1]}
 #recover
   Oops: ${.error}
   Remember, freeMarker sequences are zero-based! ${sequence[0]}
 /#
 Now we output the current error message: ${.error}
/#
#attempt
  #include "nonexistent_template"
#recover
  The template is not currently available
/#attempt
#attempt
  #include "undefined.ftl"
#recover
  The included template has a problem: ${.error}
/#attempt
