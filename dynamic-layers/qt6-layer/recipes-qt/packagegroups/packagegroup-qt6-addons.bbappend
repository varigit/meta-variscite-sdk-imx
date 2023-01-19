CONNMAN_DEPENDENT_PACKAGES = "qtdeviceutilities"

RDEPENDS:${PN}:remove = " \
    ${@bb.utils.contains('PREFERRED_CONNECTIVITY_MANAGER', 'networkmanager', '${CONNMAN_DEPENDENT_PACKAGES}', '', d)} \
"

RDEPENDS:${PN}:remove:imxpxp = " \
    qt3d \
    qtdatavis3d \
    qtquick3d \
    ${@bb.utils.contains('PREFERRED_CONNECTIVITY_MANAGER', 'networkmanager', '${CONNMAN_DEPENDENT_PACKAGES}', '', d)} \
"
