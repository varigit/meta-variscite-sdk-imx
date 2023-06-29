FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGECONFIG_GRAPHICS:imxpxp = ""
PACKAGECONFIG_PLATFORM:imxpxp = "no-opengl linuxfb"

IMX_BACKEND_FB = "linuxfb"
IMX_BACKEND_FB:imxgpu3d = "eglfs"
IMX_BACKEND = \
    "${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'wayland',\
        bb.utils.contains('DISTRO_FEATURES',     'x11',     'x11', \
                                                             '${IMX_BACKEND_FB}', d), d)}"

SRC_URI:append = " \
    file://qt-${IMX_BACKEND}.sh \
"

FILES:${PN} += "${sysconfdir}/profile.d/qt.sh"
