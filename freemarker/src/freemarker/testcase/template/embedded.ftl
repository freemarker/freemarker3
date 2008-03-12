[#ftl strict_vars=false]
${x!'undefined in subtemplate'}
[#set x='set in subtemplate']
${x} ${.main.x}
