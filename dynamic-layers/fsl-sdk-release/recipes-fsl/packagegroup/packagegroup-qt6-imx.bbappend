
RDEPENDS:${PN}:append = " \
    qtbase-examples \
"

RDEPENDS:${PN}:remove:imxpxp = " \
    ${QT6_IMAGE_INSTALL_QUICK3D} \
"
