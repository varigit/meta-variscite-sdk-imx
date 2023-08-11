RDEPENDS:${PN}-ptest:remove = "tk"
RDEPENDS:${PN}-ptest:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'tk', '', d)}"
