DESCRIPTION = "Add packages for security build"

inherit packagegroup

SEC_PKGS = " \
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
