FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " \
    file://qtwayland-fix-no-opengl-build.patch \
"
