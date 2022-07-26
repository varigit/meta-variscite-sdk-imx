DESCRIPTION = "Add packages for security build"

inherit packagegroup

SEC_PKGS ?= ""
SEC_PKGS:imx-nxp-bsp = " \
    keyctl-caam \
"
RDEPENDS:${PN} = " \
    ${SEC_PKGS} \
    coreutils \
    keyutils \
    lvm2 \
    e2fsprogs-mke2fs \
    util-linux \
"
