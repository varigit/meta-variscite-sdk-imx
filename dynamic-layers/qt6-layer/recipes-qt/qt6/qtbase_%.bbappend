FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
    file://qtbase-fix-no-opengl-build.patch \
"
