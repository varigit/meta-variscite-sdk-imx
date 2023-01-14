QT6_IMAGE_INSTALL_common:remove = "${QT6_QTQUICK3D}"

QT6_IMAGE_INSTALL:imxpxp = " \
    ${QT6_IMAGE_INSTALL_common} \
"

QT6_IMAGE_INSTALL:imxgpu2d = " \
    ${QT6_IMAGE_INSTALL_common} \
"

QT6_IMAGE_INSTALL:imxgpu3d = " \
    ${QT6_IMAGE_INSTALL_common} \
    ${QT6_QTQUICK3D} \
"
