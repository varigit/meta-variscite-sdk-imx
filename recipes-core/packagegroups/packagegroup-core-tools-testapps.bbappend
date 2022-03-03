CONNMAN_PACKAGES = "\
    connman-tools \
    connman-tests \
    connman-client \
    "

RDEPENDS:${PN}:remove = "\
    ${@bb.utils.contains('DISTRO', 'b2qt', '', \
       bb.utils.contains('DISTRO_FEATURES', 'wayland', '${CONNMAN_PACKAGES}', \
                                                   '', d), d)} \
    "
