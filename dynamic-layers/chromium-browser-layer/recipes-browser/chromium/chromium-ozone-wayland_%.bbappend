FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'x11', '', 'file://0001-Remove-DRI-dependency.patch' , d)}"
