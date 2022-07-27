DESCRIPTION = "Add packages for AI/ML build"

inherit packagegroup

ML_PKGS ?= ""
ML_PKGS:mx8-nxp-bsp = " \
    pytorch \
    tensorflow-lite \
    torchvision \
"
ML_PKGS:append:mx8mq-nxp-bsp = " \
    deepview-rt \
    tvm \
"
ML_PKGS:append:mx8mp-nxp-bsp = " \
    deepview-rt \
    tvm \
"
ML_EXTRA_PKGS ?= ""
ML_EXTRA_PKGS:mx8-nxp-bsp = " \
    python3-opencv \
    python3-pip \
    python3-requests \
"
RDEPENDS:${PN} = " \
    ${ML_PKGS} \
    ${ML_EXTRA_PKGS} \
"
