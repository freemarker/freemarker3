[#ftl strict_vars="false"]
[#var x="set in main template"]
[#embed "embedded.ftl"]
${x}
[#include "embedded.ftl"]
${x}
